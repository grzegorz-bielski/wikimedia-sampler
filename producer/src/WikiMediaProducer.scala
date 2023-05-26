package wikimediasampler.producer

import cats.effect.*
import fs2.{io as _, *}
import cats.syntax.all.*
import scala.concurrent.duration.*
import fs2.kafka.*

import WikiMediaProducer.*

final class WikiMediaProducer(producer: KafkaProducer.PartitionsFor[IO, String, String]):
  val produce: Pipe[IO, (String, String), Unit] = 
    // format: off
    _.evalMap: (key, value) => 
        producer.produce(ProducerRecords.one(ProducerRecord(topic, key, value)))   
    .groupWithin(500, 15.seconds)
    .evalMap(_.sequence)
    .void
    // format: on

object WikiMediaProducer:
  val topic = "test-topic"
  val bootstrapServers = "localhost:9093"
  val producerSettings =
    ProducerSettings[IO, String, String]
      .withBootstrapServers(bootstrapServers)

  def resource: Resource[IO, WikiMediaProducer] =
    KafkaProducer.resource[IO, String, String](producerSettings).map(WikiMediaProducer(_))
