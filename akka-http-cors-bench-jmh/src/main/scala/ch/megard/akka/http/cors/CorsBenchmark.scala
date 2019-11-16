package ch.megard.akka.http.cors

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.headers.{Origin, `Access-Control-Request-Method`}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives
import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class CorsBenchmark extends Directives with CorsDirectives {

  private val config = ConfigFactory.parseString("akka.loglevel = ERROR").withFallback(ConfigFactory.load())

  private implicit val system: ActorSystem = ActorSystem("CorsBenchmark", config)
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val http = Http()

  private var binding: ServerBinding = _
  private var request: HttpRequest = _
  private var requestCors: HttpRequest = _
  private var requestPreflight: HttpRequest = _

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

    binding = Await.result(http.bindAndHandle(route, "127.0.0.1", 0), 1.second)
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
    val f = for {
      _ <- http.shutdownAllConnectionPools()
      _ <- binding.terminate(1.second)
      _ <- system.terminate()
    } yield ()
    Await.ready(f, 5.seconds)
  }

  @Benchmark
  def baseline(): Unit = {
    val f = http.singleRequest(request).flatMap(r => Unmarshal(r.entity).to[String])
    assert(Await.result(f, 1.second) == "ok")
  }

  @Benchmark
  def default_cors(): Unit = {
    val f = http.singleRequest(requestCors).flatMap(r => Unmarshal(r.entity).to[String])
    assert(Await.result(f, 1.second) == "ok")
  }

  @Benchmark
  def default_preflight(): Unit = {
    val f = http.singleRequest(requestPreflight).flatMap(r => Unmarshal(r.entity).to[String])
    assert(Await.result(f, 1.second) == "")
  }

}
