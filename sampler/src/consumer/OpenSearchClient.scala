// create index
// bulk add

// https://opensearch.org/docs/latest/clients/java/#initializing-the-client-with-ssl-and-tls-enabled-using-apache-httpclient-5-transport

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

import OpenSearchClient.*

final class OpenSearchClient(client: OpenSearchAsyncClient):
  def indexExists(name: String): IO[Boolean] =
    suspendFuture:
      client.indices.exists(_.index(name))
    .map(_.value)

  def createIndex(name: String): IO[CreateIndexResponse] =
    suspendFuture:
      client.indices.create(_.index(name))

  def bulkAdd(index: String, docs: Vector[String]): IO[BulkResponse] =
    suspendFuture:
      client.bulk:
        _.operations:
          docs
            .map: doc =>
              BulkOperation
                .Builder()
                .index(_.index(index).document(doc))
                .build
            .asJava

object OpenSearchClient:
  // TODO: parametrize host and credentials
  def resource: Resource[IO, OpenSearchClient] =
    Resource
      .fromAutoCloseable:
        IO.delay:
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

  private def suspendFuture[A](fut: => CompletableFuture[A]): IO[A] =
    IO.fromCompletableFuture(IO.delay(fut))
