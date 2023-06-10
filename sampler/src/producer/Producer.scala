package wikimediasampler.producer

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import cats.effect.syntax.monadCancel.*
import org.typelevel.log4cats.syntax.*

final case class ProducerOptions(topicName: String, bootstrapServers: String)

def produce[F[_]: Async: Logger](opts: ProducerOptions): F[ExitCode] =
  import opts.*

  info"starting" *>
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
              info"MalformedData: $cause"
            case _ =>
              ().pure[F]
      .guarantee(info"ending")
      .as(ExitCode.Success)
