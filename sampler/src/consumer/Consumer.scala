package wikimediasampler.consumer

import cats.effect.*
import cats.syntax.all.*
import cats.effect.syntax.monadCancel.*
import scala.jdk.CollectionConverters.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

final case class ConsumerOptions(indexName: String, topicName: String, bootstrapServers: String, groupId: String)

def consume[F[_]: Async: Logger](opts: ConsumerOptions): F[ExitCode] =
  import opts.*

  // [x] 1. check if index exists, create if not
  // [x] 2. consume from kafka in batches
  // 3. index batches in opensearch (has to be independent)
  // - fix failed to parse... see StringSerializer
  // 4. commit offsets

  info"starting" *>
    (OpenSearchClient.resource, KafkaConsumer.resource(bootstrapServers, groupId)).tupled
      .use: (client, consumer) =>
        for
          _ <- client
            .indexExists(indexName)
            .ifM(
              info"Index $indexName already exists",
              client.createIndex(indexName) *>
                info"Index $indexName created"
            )
          _ <-
            consumer
              .consume(topicName): chunk =>
                info"Adding chunk to index" *>
                  client
                    .bulkAdd(indexName, chunk)
                    // failed to parse...
                    // .flatTap(r => info"Indexed documents: ${r.items.asScala.map(_.error.reason())}")
                    .void
              .compile
              .drain
        yield ()
      .guarantee(info"ending")
      .as(ExitCode.Success)
