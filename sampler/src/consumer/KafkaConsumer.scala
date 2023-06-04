package wikimediasampler.consumer

import cats.effect.*
import fs2.{io as _, *}
import cats.syntax.all.*
import scala.concurrent.duration.*
import fs2.kafka.{KafkaConsumer as FS2KafkaConsumer, *}
import fs2.kafka.consumer.*

import KafkaConsumer.*

final class KafkaConsumer(consumer: FS2KafkaConsumer[IO, String, String]):
  def consume(topicName: String)(fn: String => IO[Unit]): Stream[IO, Unit] =
    Stream(consumer)
      .covary[IO]
      .subscribeTo(topicName)
      .partitionedRecords
      .map:
        _.evalMap: r =>
          fn(r.record.value).as(r.offset)
      .parJoinUnbounded
      .through(commitBatchWithin[IO](500, 15.seconds))

object KafkaConsumer:
  val consumerSettings =
    // TODO: parametrize group id and broker
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")

  def resource: Resource[IO, KafkaConsumer] =
    // TODO: graceful shutdown: https://fd4s.github.io/fs2-kafka/docs/consumers#graceful-shutdown
    FS2KafkaConsumer.resource[IO, String, String](consumerSettings).map(KafkaConsumer(_))
