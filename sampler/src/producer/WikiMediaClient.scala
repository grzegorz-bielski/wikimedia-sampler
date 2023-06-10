package wikimediasampler.producer

import cats.effect.*
import sttp.client3.httpclient.fs2.HttpClientFs2Backend
import sttp.client3.*
import fs2.{io as _, *}
import cats.syntax.all.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.impl.fs2.Fs2ServerSentEvents
import sttp.model.sse.ServerSentEvent
import cats.Monad
import org.typelevel.log4cats.Logger
import io.circe.parser.*

import WikiMediaClient.*

final class WikiMediaClient[F[_]: Async: Logger](backend: SttpBackend[F, Fs2Streams[F]]):
  def recentChanges: Stream[F, WikiMediaMessage] =
    Stream
      .eval:
        basicRequest
          .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
          .response(asStreamUnsafe(Fs2Streams[F]))
          .send(backend)
      .map(_.body.leftMap(WikiMediaError.ConnectionError(_)))
      .flatMap(Stream.fromEither[F](_))
      .flatten
      .through(Fs2ServerSentEvents.parse[F])
      .through(toWikiMediaMessage)

  private val toWikiMediaMessage: Pipe[F, ServerSentEvent, WikiMediaMessage] =
    _.collect:
      case ServerSentEvent(Some(data), _, _, _) =>
        decode[WikiMediaMessage](data).leftMap(err => WikiMediaError.MalformedData(err.getMessage))
    .flatMap(Stream.fromEither(_))

object WikiMediaClient:
  def resource[F[_]: Async: Logger]: Resource[F, WikiMediaClient[F]] =
    HttpClientFs2Backend.resource[F]().map(WikiMediaClient(_))

  enum WikiMediaError extends Exception:
    case ConnectionError(cause: String)
    case MalformedData(cause: String)
