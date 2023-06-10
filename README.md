# wikimedia-sampler

A _toy_ example of a Kafka to OpenSearch / ElasticSearch pipeline for WikiMedia data written in pure FP Scala 3 with Cats Effect, fs2-kafka and opensearch-java client.

It consists of:

- `producer`: a Kafka producer that reads from the WikiMedia event stream and writes to a kafka topic
- `consumer`: a Kafka consumer that reads from a Kafka topic and writes to OpenSearch

## Setup

1. Install [scala-cli](https://scala-cli.virtuslab.org/) 
2. Start VM (optional)
    ```sh
    # MacOS only, not needed if you have Docker Desktop or similar
    ./colima.sh
    ```
2. Run local infra. 
   ```sh
   docker-compose -f ./docker-compose.yml up
   ```
3. Run the app
   ```sh
   # start producer process
   scala-cli ./sampler -- produce
   # start consumer process
   scala-cli ./sampler -- consume
   # run the producer & consumer processes concurrently
   scala-cli ./sampler -- produce-consume
   # for more options
   scala-cli ./sampler -- help
   ```
4. You can inspect processed data in:
    - Kafka UI available at http://localhost:8080
    - OpenSearch-Dashboards / Kibana console available at http://localhost:5601/app/dev_tools#/console

## Potential improvements
- parametrize more OpenSearch client options and move them to the CLI level
- use Avro or other format instead of JSON
- kafka consumer graceful shutdown
- use explicit mapping instead of dynamic one in OpenSearch 
- create Grafana / Kibana dashboards
- add more tests
