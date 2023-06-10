package wikimediasampler.producer

import cats.effect.*
import fs2.{io as _, *}
import cats.syntax.all.*
import scala.concurrent.duration.*
import fs2.kafka.{KafkaProducer as FS2KafkaProducer, *}

import KafkaProducer.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

final class KafkaProducer[F[_]: Async: Logger](producer: FS2KafkaProducer.PartitionsFor[F, String, String]):
  def produce(topicName: String): Pipe[F, (String, String), Unit] = 
    // format: off
    _.evalMap: (key, value) => 
        producer.produce(ProducerRecords.one(ProducerRecord(topicName, key, value)))   
    .groupWithin(500, 15.seconds)
    .evalMap:
      _.sequence.onError(err => info"Producer error: ${err.getMessage}")
    .void
    // format: on

object KafkaProducer:
  def resource[F[_]: Async: Logger](bootstrapServers: String): Resource[F, KafkaProducer[F]] =
    val settings = ProducerSettings[F, String, String]
      .withBootstrapServers(bootstrapServers)

    FS2KafkaProducer
      .resource[F, String, String](settings)
      .map(KafkaProducer(_))
