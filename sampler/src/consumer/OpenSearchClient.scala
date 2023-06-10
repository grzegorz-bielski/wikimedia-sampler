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

import OpenSearchClient.*
import cats.effect.kernel.Async
import cats.effect.kernel.Sync

final class OpenSearchClient[F[_]: Async](client: OpenSearchAsyncClient):
  def indexExists(name: String): F[Boolean] =
    suspendFuture:
      client.indices.exists(_.index(name))
    .map(_.value)

  def createIndex(name: String): F[CreateIndexResponse] =
    suspendFuture:
      client.indices.create(_.index(name))

  def bulkAdd(index: String, docs: Vector[String]): F[BulkResponse] =
    lazy val ops = docs
      .map: doc =>
        BulkOperation
          .Builder()
          .index(_.index(index).document(doc))
          .build
      .asJava

    suspendFuture:
      client.bulk(_.operations(ops))

object OpenSearchClient:
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
