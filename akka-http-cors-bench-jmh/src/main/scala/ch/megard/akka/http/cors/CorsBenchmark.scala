package ch.megard.akka.http.cors

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.model.headers.{Origin, `Access-Control-Request-Method`}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Lomig Mégard
  */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class CorsBenchmark extends Directives with CorsDirectives {

  val config = ConfigFactory.parseString(
    """
      akka {
        loglevel = "ERROR"
      }""".stripMargin).withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("CorsBenchmark", config)
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  var binding: ServerBinding = _
  var request: HttpRequest = _
  var requestCors: HttpRequest = _
  var requestPreflight: HttpRequest = _

  @Setup
  def setup(): Unit = {
    val route = {
      path("baseline") {
        get {
          complete("ok")
        }
      } ~ path("cors") {
        cors() {
          get {
            complete("ok")
          }
        }
      }
    }
    val origin = Origin("http://example.com")

    binding = Await.result(Http().bindAndHandle(route, "127.0.0.1", 0), 1.second)
    val base = s"http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}"

    request = HttpRequest(uri = base + "/baseline")
    requestCors = HttpRequest(
      method = HttpMethods.GET,
      uri = base + "/cors",
      headers = List(origin)
    )
    requestPreflight = HttpRequest(
      method = HttpMethods.OPTIONS,
      uri = base + "/cors",
      headers = List(origin, `Access-Control-Request-Method`(HttpMethods.GET))
    )
  }

  @TearDown
  def shutdown(): Unit = {
    Await.ready(Http().shutdownAllConnectionPools(), 1.second)
    binding.unbind()
    Await.result(system.terminate(), 5.seconds)
  }

  @Benchmark
  def baseline(): Unit = {
    val response = Await.result(Http().singleRequest(request), 1.second)
    Await.result(Unmarshal(response.entity).to[String], 1.second)
  }

  @Benchmark
  def default_cors(): Unit = {
    val response = Await.result(Http().singleRequest(requestCors), 1.second)
    Await.result(Unmarshal(response.entity).to[String], 1.second)
  }

  @Benchmark
  def default_preflight(): Unit = {
    val response = Await.result(Http().singleRequest(requestPreflight), 1.second)
    Await.result(Unmarshal(response.entity).to[String], 1.second)
  }

}
