package wikimediasampler.consumer

import cats.effect.*
import fs2.{io as _, *}
import cats.syntax.all.*
import scala.concurrent.duration.*
import fs2.kafka.{KafkaConsumer as FS2KafkaConsumer, *}
import fs2.kafka.consumer.*
import cats.Functor
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

import KafkaConsumer.*
final class KafkaConsumer[F[_]: Async: Logger](consumer: FS2KafkaConsumer[F, String, String]):
  def consume(topicName: String)(fn: Vector[String] => F[Unit]): Stream[F, Unit] =
    Stream(consumer)
      .covary[F]
      .subscribeTo(topicName)
      .evalTap(_ => info"Subscribed to $topicName topic")
      .partitionedRecords
      .map:
        _
          .evalTap(s => debug"Received: ${s.record.value}")
          .groupWithin(200, 1.seconds)
          .evalMap: chunk =>
            fn(chunk.map(_.record.value).toVector).as(chunk.map(_.offset))
          .unchunks
      .parJoinUnbounded
      .through(commitBatchWithin[F](500, 15.seconds))

object KafkaConsumer:
  def resource[F[_]: Async: Logger](bootstrapServers: String, groupId: String): Resource[F, KafkaConsumer[F]] =
    val settings =
      ConsumerSettings[F, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers(bootstrapServers)
        .withGroupId(groupId)

    // TODO: graceful shutdown: https://fd4s.github.io/fs2-kafka/docs/consumers#graceful-shutdown
    FS2KafkaConsumer.resource[F, String, String](settings).map(KafkaConsumer(_))
