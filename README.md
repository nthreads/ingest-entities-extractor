# Elasticsearch entities-extractor Ingest Processor

Explain the use case of this processor in a TLDR fashion.

## Usage


```
PUT _ingest/pipeline/entities-extractor-pipeline
{
  "description": "Ingest processor to extract entities like emaials, urls, persons, locations and organizations and store them into array of different fields.",
  "processors": [
    {
      "entities_extractor" : {
        "field" : "my_field"
      }
    }
  ]
}

PUT /my-entities/_doc/1?pipeline=entities-extractor-pipeline
{
  "my_field" : "You can reach out to me at nauman@csms.ae or at nauman.zubair@gmail.com or commenting on www.nthreads.com"
}

GET /my-entities/_doc/1
{
  "my_field" : "Some content"
  "potentially_enriched_field": "potentially_enriched_value"
}
```

## Configuration

| Parameter | Use |
| --- | --- |
| some.setting   | Configure x |
| other.setting  | Configure y |

## Setup

In order to install this plugin, you need to create a zip distribution first by running

```bash
gradle clean check
```

This will produce a zip file in `build/distributions`.

After building the zip file, you can install it like this

```bash
bin/elasticsearch-plugin install file:///path/to/ingest-entities-extractor/build/distribution/ingest-entities-extractor-0.0.1-SNAPSHOT.zip
```

## Bugs & TODO

* There are always bugs
* and todos...

