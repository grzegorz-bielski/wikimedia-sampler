package wikimediasampler.producer

import cats.effect.*
import sttp.client3.httpclient.fs2.HttpClientFs2Backend
import sttp.client3.*
import fs2.{io as _, *}
import cats.syntax.all.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.impl.fs2.Fs2ServerSentEvents
import sttp.model.sse.ServerSentEvent
import io.circe.parser.*

import WikiMediaClient.*

final class WikiMediaClient(backend: SttpBackend[IO, Fs2Streams[IO]]):
  def recentChanges: Stream[IO, WikiMediaMessage] =
    for
      response <- Stream.eval:
        // TODO: use safe `asStream` ?
        basicRequest
          .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
          .response(asStreamUnsafe(Fs2Streams[IO]))
          .send(backend)

      eventSource <- Stream.fromEither[IO]:
        response.body.leftMap(WikiMediaError.ConnectionError(_))

      msg <- eventSource
        .through(Fs2ServerSentEvents.parse)
        .through(toWikiMediaMessage)
    yield msg

  private val toWikiMediaMessage: Pipe[IO, ServerSentEvent, WikiMediaMessage] =
    _.collect:
      case ServerSentEvent(Some(data), _, _, _) =>
        decode[WikiMediaMessage](data).leftMap(err => WikiMediaError.MalformedData(err.getMessage))
    .flatMap(Stream.fromEither(_))

object WikiMediaClient:
  def resource: Resource[IO, WikiMediaClient] =
    HttpClientFs2Backend.resource[IO]().map(WikiMediaClient(_))

  enum WikiMediaError extends Exception:
    case ConnectionError(cause: String)
    case MalformedData(cause: String)
