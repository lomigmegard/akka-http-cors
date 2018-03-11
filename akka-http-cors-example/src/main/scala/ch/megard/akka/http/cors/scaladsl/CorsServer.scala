package ch.megard.akka.http.cors.scaladsl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{HttpOrigin, HttpOriginRange}
import akka.http.scaladsl.server.{Directives, ExceptionHandler, RejectionHandler, Route}
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

/**
  * Example of a Scala HTTP server using the CORS directive.
  */
object CorsServer {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()
    Http().bindAndHandle(route, "127.0.0.1", 9000)
  }

  def route: Route = {
    import CorsDirectives._
    import Directives._

    // Your CORS settings
    val corsSettings = CorsSettings.defaultSettings
      .withAllowedOrigins(HttpOriginRange(HttpOrigin("http://example.com")))

    // Your rejection handler
    val rejectionHandler = corsRejectionHandler withFallback RejectionHandler.default

    // Your exception handler
    val exceptionHandler = ExceptionHandler {
      case e: NoSuchElementException => complete(StatusCodes.NotFound -> e.getMessage)
    }

    // Combining the two handlers only for convenience
    val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

    // Note how rejections and exceptions are handled *before* the CORS directive (in the inner route).
    // This is required to have the correct CORS headers in the response even when an error occurs.
    handleErrors {
      cors(corsSettings) {
        handleErrors {
          path("ping") {
            complete("pong")
          } ~
          path("pong") {
            failWith(new NoSuchElementException("pong not found, try with ping"))
          }
        }
      }
    }
  }

}
