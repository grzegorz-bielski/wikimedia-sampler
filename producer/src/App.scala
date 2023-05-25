package wikimediasampler.producer

import cats.effect.*
import sttp.client3.httpclient.fs2.HttpClientFs2Backend
import sttp.client3.*
import fs2.*
import cats.syntax.all.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.impl.fs2.Fs2ServerSentEvents
import sttp.model.sse.ServerSentEvent

object WikimediaSampler extends IOApp.Simple:
  def run =
    IO.println("starting") *>
      HttpClientFs2Backend
        .resource[IO]()
        .use:
          WikiMediaClient(_).recentChanges
            .evalTap(IO.println)
            .compile
            .drain
        .guarantee(IO.println("ending"))
        .onError:
          case WikiMediaClient.WikiMediaError.MalformedData(cause) =>
            IO.println(s"MalformedData: $cause")
          case _ => IO.unit