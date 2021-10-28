/*
 * Copyright [2020] [Nauman Zubair]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.elasticsearch.plugin.ingest.entities.extractor;

import org.elasticsearch.common.Strings;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkType;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalList;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

public class EntitiesExtractorProcessor extends AbstractProcessor {

    public static final String TYPE = "entities_extractor";

    private final String sourceField;
    private final String targetField;

    private final Set<String> fields;

    public EntitiesExtractorProcessor(String tag, String description, String sourceField,
                                      String targetField, Set<String> fields) throws IOException {
        super(tag, description);
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.fields = fields;
    }


    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        String content = ingestDocument.getFieldValue(sourceField, String.class);

        if (Strings.hasLength(content)) {
            Map<String, List<String>> entities = new HashMap<>();

            //mergeExisting(entities, ingestDocument, targetField);


            LinkExtractor linkExtractor = LinkExtractor.builder().linkTypes(EnumSet.of(LinkType.EMAIL, LinkType.WWW, LinkType.URL)).build();

            Map<String, List<String>> groupedEntities = StreamSupport.stream(linkExtractor.extractLinks(content).spliterator(), false)
                    .collect(Collectors.groupingBy(map -> map.getType().name().toLowerCase(),
                            Collectors.mapping(link -> content.substring(link.getBeginIndex(), link.getEndIndex()),
                                    Collectors.toList())));

            List<String> urls = new ArrayList<>();
            urls.addAll(groupedEntities.get(LinkType.WWW.name().toLowerCase()));
            urls.addAll(groupedEntities.get(LinkType.URL.name().toLowerCase()));

            List<String> emails = new ArrayList<>(groupedEntities.get(LinkType.EMAIL.name().toLowerCase()));

            // Filter for only required
            for (String field : fields) {
                if (field.equals("urls")) {
                    entities.put(field, urls);
                } else if (field.equals("emails")) {
                    entities.put(field, emails);
                }
            }

            System.out.println("entities size " + entities.size() + " => " + entities.toString());

            ingestDocument.setFieldValue(targetField, entities);
        }

        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }



    public static final class Factory implements Processor.Factory {

        @Override
        public EntitiesExtractorProcessor create(Map<String, Processor.Factory> factories, String tag,
                                                 String description, Map<String, Object> config) throws Exception {
            String sourceField = readStringProperty(TYPE, tag, config, "field");
            String targetField = readStringProperty(TYPE, tag, config, "target_field", "entities");
            List<String> fields = readOptionalList(TYPE, tag, config, "fields");

            Set<String> defaultFeilds = new HashSet<>(Arrays.asList("urls", "emails"));

            final Set<String> requiredEntitiesFields = fields == null || fields.size() == 0 ? defaultFeilds : new HashSet<>(fields);

            return new EntitiesExtractorProcessor(tag, description, sourceField, targetField, requiredEntitiesFields);
        }
    }

    private static void mergeExisting(Map<String, Set<String>> entities, IngestDocument ingestDocument, String targetField) {
        if (ingestDocument.hasField(targetField)) {
            @SuppressWarnings("unchecked")
            Map<String, Set<String>> existing = ingestDocument.getFieldValue(targetField, Map.class);
            entities.putAll(existing);
        } else {
            ingestDocument.setFieldValue(targetField, entities);
        }
    }

    private static void merge(Map<String, Set<String>> map, String key, Set<String> values) {
        if (values.size() == 0) return;

        if (map.containsKey(key)) {
            values.addAll(map.get(key));
        }

        map.put(key, values);
    }
}
