package wikimediasampler.producer

import cats.effect.*
import fs2.{io as _, *}
import cats.syntax.all.*
import scala.concurrent.duration.*
import fs2.kafka.{KafkaProducer as FS2KafkaProducer, *}

import KafkaProducer.*

final class KafkaProducer(producer: FS2KafkaProducer.PartitionsFor[IO, String, String]):
  def produce(topicName: String): Pipe[IO, (String, String), Unit] = 
    // format: off
    _.evalMap: (key, value) => 
        producer.produce(ProducerRecords.one(ProducerRecord(topicName, key, value)))   
    .groupWithin(500, 15.seconds)
    .evalMap:
      _.sequence.onError(err => IO.println(s"Producer error: ${err.getMessage}"))
    .void
    // format: on

object KafkaProducer:
  def resource(bootstrapServers: String): Resource[IO, KafkaProducer] =
    val settings = ProducerSettings[IO, String, String]
      .withBootstrapServers(bootstrapServers)

    FS2KafkaProducer
      .resource[IO, String, String](settings)
      .map(KafkaProducer(_))
