package wikimediasampler.producer

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*

final case class ProducerOptions(topicName: String, bootstrapServers: String)

def produce(opts: ProducerOptions): IO[ExitCode] =
  import opts.*

  IO.println("starting") *>
    (WikiMediaClient.resource, KafkaProducer.resource(bootstrapServers)).tupled
      .use: (client, producer) =>
        client.recentChanges
          .map: msg =>
            msg.user.getOrElse("no user") -> msg.asJson.noSpaces
          .through(producer.produce(topicName))
          .compile
          .drain
          .onError:
            case WikiMediaClient.WikiMediaError.MalformedData(cause) =>
              IO.println(s"MalformedData: $cause")
            case _ =>
              IO.unit
      .guarantee(IO.println("ending"))
      .as(ExitCode.Success)
