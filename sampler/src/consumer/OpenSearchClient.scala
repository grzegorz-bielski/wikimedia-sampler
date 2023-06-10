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
import cats.effect.IO
import scala.jdk.CollectionConverters.*
import cats.syntax.all.*
import cats.implicits.*
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.ApplicativeError
import cats.MonadThrow
import cats.Show
import org.opensearch.client.opensearch._types.ErrorCause
import OpenSearchClient.{*, given}
import org.opensearch.client.json.JsonpSerializer
import jakarta.json.Json
import jakarta.json.stream.JsonGenerator
import org.opensearch.client.json.{JsonpMapper, JsonpSerializable}
import java.io.StringReader

final class OpenSearchClient[F[_]: Async](client: OpenSearchAsyncClient):
  def indexExists(name: String): F[Boolean] =
    suspendFuture:
      client.indices.exists(_.index(name))
    .map(_.value)

  def createIndex(name: String): F[CreateIndexResponse] =
    suspendFuture:
      client.indices.create(_.index(name))

  def bulkAdd(index: String, docs: Vector[String]): F[BulkResponse] =
    suspendFuture:
      val ops = docs
        .map: doc =>
          BulkOperation
            .Builder()
            .index:
              _.index(index)
                  // TODO: handle serialisation errors in StringSerializer, remove root cause
                .document(StringSerializer(doc))
            .build
        .asJava
      client.bulk(_.operations(ops))
    .reject:
      case response if response.errors =>
        val errors = response.items.asScala.map(_.error).toVector
        val report =
          if errors.distinctBy(_.`type`).size == 1 then errors.head.show else errors.show

        RuntimeException(s"Failed to index documents:\n $report")

object OpenSearchClient:
  inline given Show[ErrorCause] = Show.show: cause =>
    s"""ErrorCause(
    |type = ${cause.`type`},
    |reason = ${cause.reason}),
    |metadata = ${cause.metadata.asScala.view.mapValues(_.toString).toMap}
    """.stripMargin

  // TODO: parametrize host and credentials
  def resource[F[_]: Async]: Resource[F, OpenSearchClient[F]] =
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

// see:
// - https://github.com/yash025/opensearch-scala-demo/blob/master/src/main/scala/CirceToJava.scala
// - https://github.com/opensearch-project/opensearch-java/issues/362
final class StringSerializer(value: String) extends JsonpSerializable:
  override def serialize(generator: JsonGenerator, mapper: JsonpMapper): Unit =
    val jsonReader = Json.createReader(StringReader(value))
    val messageAsJson = jsonReader.read()
    generator.write(messageAsJson)
