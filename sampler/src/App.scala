package wikimediasampler

import cats.effect.*
import cats.syntax.all.*
import cats.effect.syntax.*
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

  val producerOpts = (kafkaTopicName, kafkaBootstrapServers)
    .mapN(ProducerOptions.apply)

  val producerCmd =
    Opts.subcommand("produce", "Produce data from wikimedia stream")(producerOpts)

  val consumerOpts =
    (indexNameOpts, kafkaTopicName, kafkaBootstrapServers, groupIdOpts).mapN(ConsumerOptions.apply)

  val consumerCmd =
    Opts.subcommand("consume", "Consume data from wikimedia stream")(consumerOpts)

  val producerConsumerCmd = Opts.subcommand("produce-consume", "Produce and consume data from wikimedia stream"):
    (producerOpts, consumerOpts).tupled

  val allCmd = producerCmd orElse consumerCmd orElse producerConsumerCmd

  def main = allCmd.map: cmd =>
    Slf4jLogger
      .create[IO]
      .flatMap:
        case given Logger[IO] =>
          cmd match
            case cmd: ProducerOptions => produce(cmd)
            case cmd: ConsumerOptions => consume(cmd)
            case (producerCmd, consumerCmd) =>
              (produce[IO](producerCmd) both consume[IO](consumerCmd)).as(ExitCode.Success)
