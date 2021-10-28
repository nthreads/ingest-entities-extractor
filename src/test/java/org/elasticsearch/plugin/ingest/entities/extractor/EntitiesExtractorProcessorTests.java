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

import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;

import java.util.*;

import static com.carrotsearch.randomizedtesting.RandomizedTest.randomAsciiLettersOfLength;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class EntitiesExtractorProcessorTests extends ESTestCase {

    public void testThatExtractionsWork() throws Exception {
        EntitiesExtractorProcessor processor = new EntitiesExtractorProcessor(randomAsciiLettersOfLength(10),"description",  "source_field", "target_field",
                new HashSet<>(Arrays.asList("urls", "emails")));

        Map<String, Object> entityData = getIngestDocumentData(processor);

        assertThatHasElements(entityData, "urls", "www.nthreads.com", "https://nthreads.com", "http://nthreads.com");
        assertThatHasElements(entityData, "emails", "nauman.zubair@gmail.com");
    }

    public void testThatFieldsCanBeExcluded() throws Exception {
        EntitiesExtractorProcessor processor = new EntitiesExtractorProcessor(randomAsciiLettersOfLength(10),"description",  "source_field", "target_field",
                new HashSet<>(Arrays.asList("urls")));

        Map<String, Object> entityData = getIngestDocumentData(processor);

        assertThat(entityData, not(hasKey("emails")));
        assertThatHasElements(entityData, "urls", "www.nthreads.com", "https://nthreads.com", "http://nthreads.com");

    }

    private Map<String, Object> getIngestDocumentData(EntitiesExtractorProcessor processor) throws Exception {
        IngestDocument ingestDocument = getIngestDocument();
        return getIngestDocumentData(processor.execute(ingestDocument));
    }

    private IngestDocument getIngestDocument() throws Exception {
        return getIngestDocument("You can reachout to me via www.nthreads.com or nauman.zubair@gmail.com or https://nthreads.com http://nthreads.com");
    }

    private IngestDocument getIngestDocument(String content) throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", content);
        return RandomDocumentPicks.randomIngestDocument(random(), document);
    }

    private Map<String, Object> getIngestDocumentData(IngestDocument ingestDocument) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) ingestDocument.getSourceAndMetadata().get("target_field");
        return data;
    }

    private void assertThatHasElements(Map<String, Object> entityData, String field, String ... items) {
        List<String> values = getValues(entityData, field);
        assertThat(values, hasSize(items.length));
        assertThat(values, containsInAnyOrder(items));
    }

    private List<String> getValues(Map<String, Object> entityData, String field) {
        assertThat(entityData, hasKey(field));
        assertThat(entityData.get(field), instanceOf(List.class));
        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) entityData.get(field);
        return values;
    }
}

