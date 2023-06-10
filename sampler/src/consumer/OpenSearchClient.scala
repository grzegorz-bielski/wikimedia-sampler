package wikimediasampler.consumer

import org.apache.hc.client5.http.auth.*
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.core5.http.HttpHost
import org.opensearch.client.transport.OpenSearchTransport
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder
import org.opensearch.client.opensearch.indices.*
import org.opensearch.client.opensearch.core.*
import org.opensearch.client.opensearch.OpenSearchAsyncClient
import org.opensearch.client.opensearch.core.bulk.*
import java.util.concurrent.CompletableFuture
import cats.effect.kernel.Resource
import cats.effect.*
import scala.jdk.CollectionConverters.*
import cats.syntax.all.*
import cats.effect.kernel.*
import cats.*
import org.opensearch.client.opensearch._types.ErrorCause
import org.opensearch.client.json.JsonpSerializer
import jakarta.json.Json
import jakarta.json.stream.JsonGenerator
import org.opensearch.client.json.{JsonpMapper, JsonpSerializable}
import java.io.StringReader
import jakarta.json.JsonValue
import scala.util.Try
import org.typelevel.log4cats.syntax.*
import cats.data.*
import org.typelevel.log4cats.Logger

import OpenSearchClient.{*, given}

final class OpenSearchClient[F[_]: Async: Logger](client: OpenSearchAsyncClient):
  def indexExists(name: String): F[Boolean] =
    suspendFuture:
      client.indices.exists(_.index(name))
    .map(_.value)

  def createIndex(name: String): F[CreateIndexResponse] =
    suspendFuture:
      client.indices.create(_.index(name))

  def bulkAdd(index: String, docs: Vector[String]): F[BulkResponse] =
    docs
      .map(stringSerializer)
      .collect:
        // ignoring serialization errors
        case Right(value) => value.pure[F]
      .traverse:
        _.map: doc =>
          BulkOperation
            .Builder()
            .index(_.index(index).document(doc))
            .build
      .flatMap: ops =>
        suspendFuture:
          client.bulk(_.operations(ops.asJava))
      .reject:
        case response if response.errors =>
          val errors = response.items.asScala.map(_.error).toVector
          val report =
            if errors.distinctBy(_.`type`).size == 1 then errors.head.show else errors.show

          RuntimeException(s"Failed to index documents:\n $report")

object OpenSearchClient:
  enum OpenSearchError extends Exception:
    case BulkSerializationError(causes: NonEmptyChain[String])

  inline given Show[ErrorCause] = Show.show: cause =>
    s"""ErrorCause(
    |type = ${cause.`type`},
    |reason = ${cause.reason}),
    |metadata = ${cause.metadata.asScala.view.mapValues(_.toString).toMap}
    """.stripMargin

  // TODO: parametrize host and credentials
  def resource[F[_]: Async: Logger]: Resource[F, OpenSearchClient[F]] =
    Resource
      .fromAutoCloseable:
        Sync[F].delay:
          val host = HttpHost("localhost", 9200)
          val credentialsProvider = BasicCredentialsProvider()
          credentialsProvider.setCredentials(
            AuthScope(host),
            UsernamePasswordCredentials("admin", "admin".toCharArray)
          )

          ApacheHttpClient5TransportBuilder
            .builder(host)
            .setHttpClientConfigCallback:
              _.setDefaultCredentialsProvider(credentialsProvider)
            .build
      .map(OpenSearchAsyncClient(_))
      .map(OpenSearchClient(_))

  private def suspendFuture[F[_]: Async, A](fut: => CompletableFuture[A]): F[A] =
    Async[F].fromCompletableFuture(Sync[F].delay(fut))

  // OpenSearch client doesn't provide a way to pass JSON string directly
  // see: https://github.com/yash025/opensearch-scala-demo/blob/master/src/main/scala/CirceToJava.scala
  private def stringSerializer(value: String): Either[Throwable, JsonpSerializable] =
    Try(Json.createReader(StringReader(value)).read()).toEither
      .map: value =>
        new JsonpSerializable:
          override def serialize(generator: JsonGenerator, _mapper: JsonpMapper): Unit =
            generator.write(value)
