package wikimediasampler

import cats.effect.*
import com.monovore.decline.*
import com.monovore.decline.effect.*

import wikimediasampler.producer.*
import wikimediasampler.consumer.*

object SamplerApp
    extends CommandIOApp(
      name = "sampler",
      header = "Sample data from WikiMedia stream through Kafka and index it in OpenSearch",
      version = "0.1.0"
    ):

  val producerOpts: Opts[ProducerOptions] =
    Opts.subcommand("produce", "Produce data from wikimedia stream"):
      Opts
        .option[String]("topic", "Topic name")
        .withDefault("test-topic")
        .map(ProducerOptions(_))

  val consumerOpts: Opts[ConsumerOptions] =
    Opts.subcommand("consume", "Consume data from wikimedia stream"):
      Opts
        .option[String]("index", "Index name")
        .withDefault("test-index")
        .map(ConsumerOptions(_))

  val allOpts = producerOpts orElse consumerOpts

  def main = allOpts.map:
    case opts: ProducerOptions => produce(opts)
    case opts: ConsumerOptions => consume(opts)
