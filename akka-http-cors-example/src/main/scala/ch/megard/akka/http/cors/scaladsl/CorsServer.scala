package ch.megard.akka.http.cors.scaladsl

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._

/**
  * Example of a Scala HTTP server using the CORS directive.
  */
object CorsServer extends HttpApp {

  def main(args: Array[String]): Unit = {
    CorsServer.startServer("127.0.0.1", 9000)
  }

  protected def routes: Route = {
    import CorsDirectives._

    // Your CORS settings are loaded from `application.conf`

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
      cors() {
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
