//> using dep "com.github.fd4s::fs2-kafka:3.0.1"
//> using dep "com.softwaremill.sttp.client3::core:3.8.15"
//> using dep "com.softwaremill.sttp.client3::fs2:3.8.15"

//> using scala "3.3.0-RC6"

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

import WikiMediaClient.*

final class WikiMediaClient(backend: SttpBackend[IO, Fs2Streams[IO]]):
  def recentChanges: Stream[IO, ServerSentEvent] =
    for
      response <- Stream.eval:
        basicRequest
          .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
          .response(
            asStreamUnsafe(Fs2Streams[IO])
          ) // TODO: use safe `asStream` ?
          .send(backend)

      _ <- Stream.eval(IO.println(response.code))
      eventSource <- Stream.fromEither[IO]:
        response.body.leftMap(WikiMediaError.ConnectionError(_))
      event <- eventSource.through(Fs2ServerSentEvents.parse)
    yield event

object WikiMediaClient:
  enum WikiMediaError extends Exception:
    case ConnectionError(cause: String)
