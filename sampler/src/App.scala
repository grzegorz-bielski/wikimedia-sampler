package wikimediasampler

import cats.effect.*
import cats.syntax.all.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import wikimediasampler.producer.*
import wikimediasampler.consumer.*

object SamplerApp
    extends CommandIOApp(
      name = "sampler",
      header = "Sample data from WikiMedia stream through Kafka and index it in OpenSearch",
      version = "0.1.0"
    ):

  // TODO: use some opaque / newtypes for these

  val kafkaBootstrapServers = Opts
    .option[String]("bootstrap-servers", "Kafka bootstrap servers")
    .withDefault("localhost:9093")

  val kafkaTopicName = Opts
    .option[String]("topic", "Topic name")
    .withDefault("test-topic")

  val indexNameOpts = Opts
    .option[String]("index", "Index name")
    .withDefault("test-index")

  val groupIdOpts = Opts
    .option[String]("group-id", "Kafka consumer group id")
    .withDefault("test-group-ll")

  val producerOpts =
    Opts.subcommand("produce", "Produce data from wikimedia stream"):
      (kafkaTopicName, kafkaBootstrapServers)
        .mapN(ProducerOptions.apply)

  val consumerOpts =
    Opts.subcommand("consume", "Consume data from wikimedia stream"):
      (indexNameOpts, kafkaTopicName, kafkaBootstrapServers, groupIdOpts).mapN(ConsumerOptions.apply)

  val allOpts = producerOpts orElse consumerOpts

  def main = allOpts.map: opts =>
    Slf4jLogger
      .create[IO]
      .flatMap:
        case given Logger[IO] =>
          opts match
            case opts: ProducerOptions => produce(opts)
            case opts: ConsumerOptions => consume(opts)
