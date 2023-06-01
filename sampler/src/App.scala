package wikimediasampler

import cats.effect.*
import cats.syntax.all.*
import io.circe.syntax.*

import wikimediasampler.producer.*

object WikimediaSampler extends IOApp.Simple:
  def run = IO.println("starting") *>
    (WikiMediaClient.resource, WikiMediaProducer.resource).tupled
      .use: (client, producer) =>
        client.recentChanges
          .map(msg => msg.user.getOrElse("no user") -> msg.asJson.noSpaces)
          .through(producer.produce)
          .compile
          .drain
          .onError:
            case WikiMediaClient.WikiMediaError.MalformedData(cause) =>
              IO.println(s"MalformedData: $cause")
            case _ =>
              IO.unit
      .guarantee(IO.println("ending"))
