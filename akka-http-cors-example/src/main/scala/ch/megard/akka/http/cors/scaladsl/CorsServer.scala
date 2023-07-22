/*
 * Copyright 2016 Lomig Mégard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.megard.akka.http.cors.scaladsl

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._

/** Example of a Scala HTTP server using the CORS directive.
  */
object CorsServer extends HttpApp {
  def main(args: Array[String]): Unit = {
    CorsServer.startServer("127.0.0.1", 9000)
  }

  protected def routes: Route = {
    import CorsDirectives._

    // Your CORS settings are loaded from `application.conf`

    // Your rejection handler
    val rejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)

    // Your exception handler
    val exceptionHandler = ExceptionHandler { case e: NoSuchElementException =>
      complete(StatusCodes.NotFound -> e.getMessage)
    }

    // Combining the two handlers only for convenience
    val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

    // Note how rejections and exceptions are handled *before* the CORS directive (in the inner route).
    // This is required to have the correct CORS headers in the response even when an error occurs.
    // format: off
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
    // format: on
  }
}
