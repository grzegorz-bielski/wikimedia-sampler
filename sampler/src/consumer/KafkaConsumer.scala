package wikimediasampler.consumer

import cats.effect.*
import fs2.{io as _, *}
import cats.syntax.all.*
import scala.concurrent.duration.*
import fs2.kafka.{KafkaConsumer as FS2KafkaConsumer, *}
import fs2.kafka.consumer.*

import KafkaConsumer.*

final class KafkaConsumer(consumer: FS2KafkaConsumer[IO, String, String]):
  def consume(topicName: String)(fn: Vector[String] => IO[Unit]): Stream[IO, Unit] =
    Stream(consumer)
      .covary[IO]
      .subscribeTo(topicName)
      .evalTap(_ => IO.println(s"Subscribed to $topicName topic"))
      .partitionedRecords
      .map:
        _
          // .evalTap(s => IO.println(s.record.value))
          .groupWithin(200, 1.seconds)
          .evalMap: chunk =>
            fn(chunk.map(_.record.value).toVector).as(chunk.map(_.offset))
          .unchunks
      .parJoinUnbounded
      .through(commitBatchWithin[IO](500, 15.seconds))

object KafkaConsumer:
  def resource(bootstrapServers: String, groupId: String): Resource[IO, KafkaConsumer] =
    val settings =
      ConsumerSettings[IO, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers(bootstrapServers)
        .withGroupId(groupId)

    // TODO: graceful shutdown: https://fd4s.github.io/fs2-kafka/docs/consumers#graceful-shutdown
    FS2KafkaConsumer.resource[IO, String, String](settings).map(KafkaConsumer(_))
