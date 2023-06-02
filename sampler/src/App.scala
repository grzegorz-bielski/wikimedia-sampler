package wikimediasampler

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*
import com.monovore.decline.*
import com.monovore.decline.effect.*

import wikimediasampler.producer.*

object SamplerApp
    extends CommandIOApp(
      name = "sampler",
      header = "Sample data from wikimedia stream and index it in opensearch",
      version = "0.1.0"
    ):
  final case class ProducerOptions(topicName: String)

  val producerOpts: Opts[ProducerOptions] =
    Opts.subcommand("produce", "Produce data from wikimedia stream"):
      Opts
        .option[String]("topic", "Topic name")
        .withDefault("test-topic")
        .map(ProducerOptions(_))

  override def main: Opts[IO[ExitCode]] = producerOpts.map:
    case ProducerOptions(topicName) => produce.as(ExitCode.Success)
