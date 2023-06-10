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

  info"Starting consumer" *>
    (OpenSearchClient.resource, KafkaConsumer.resource(bootstrapServers, groupId)).tupled
      .use: (client, consumer) =>
        client
          .indexExists(indexName)
          .ifM(
            info"Index $indexName already exists",
            client.createIndex(indexName) *> info"Index $indexName created"
          ) *>
          consumer
            .consume(topicName): chunk =>
              info"Adding chunk to index" *> client.bulkAdd(indexName, chunk).void
            .compile
            .drain
      .guarantee(info"Shutting down consumer")
      .as(ExitCode.Success)
