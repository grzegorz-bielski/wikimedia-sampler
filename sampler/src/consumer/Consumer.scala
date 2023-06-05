package wikimediasampler.consumer

import cats.effect.*
import cats.syntax.all.*
import scala.jdk.CollectionConverters.*

final case class ConsumerOptions(indexName: String, topicName: String, bootstrapServers: String, groupId: String)

def consume(opts: ConsumerOptions): IO[ExitCode] =
  import opts.*

  // [x] 1. check if index exists, create if not
  // [x] 2. consume from kafka in batches
  // 3. index batches in opensearch (has to be independent)
  // 4. commit offsets

  IO.println("starting") *>
    (OpenSearchClient.resource, KafkaConsumer.resource(bootstrapServers, groupId)).tupled
      .use: (client, consumer) =>
        for
          _ <- client
            .indexExists(indexName)
            .ifM(
              IO.println(s"Index $indexName already exists"),
              client.createIndex(indexName) *> IO.println(s"Index $indexName created")
            )
          _ <-
            consumer
              .consume(topicName): chunk =>
                IO.println(s"Adding chunk to index") *>
                  client
                    .bulkAdd(indexName, chunk)
                    // failed to parse...
                    .flatTap(r => IO.println(s"Indexed documents: ${r.items.asScala.map(_.error.reason())}"))
                    .void
              .compile
              .drain
        yield ()
      .guarantee(IO.println("ending"))
      .as(ExitCode.Success)
