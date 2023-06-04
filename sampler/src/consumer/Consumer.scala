package wikimediasampler.consumer

import cats.effect.*
import cats.syntax.all.*

final case class ConsumerOptions(indexName: String)

def consume(opts: ConsumerOptions): IO[ExitCode] =
  import opts.*

  // 1. check if index exists, create if not
  // 2. consume from kafka in batches
  // 3. index batchs in opensearch (has to be independent)
  // 4. commit offsets

  IO.println("starting") *>
    (OpenSearchClient.resource, KafkaConsumer.resource).tupled
      .use: (client, consumer) =>
        for _ 
            <- client
                .indexExists(indexName)
                .ifM(
                IO.println(s"Index $indexName already exists"),
                client.createIndex(indexName) *> IO.println(s"Index $indexName created")
                )
            
        yield ()
      .guarantee(IO.println("ending"))
      .as(ExitCode.Success)
