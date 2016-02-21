package ch.megard.akka.http.cors

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Origin`, Origin, HttpOrigin}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ch.megard.akka.http.cors.CorsDirectives.InvalidCorsRequestRejection
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Lomig Mégard
  */
class CorsDirectivesSpec extends WordSpec with Matchers with CorsDirectives with Directives with ScalatestRouteTest {

  // We don't use 200 Ok to distinguish with a CORS pre-flight response.
  val actualStatus = StatusCodes.NoContent
  val completeActual = complete(actualStatus)

  val exampleOrigin = HttpOrigin("http://example.com")

  "The cors directive" should {

    "not affect actual requests when not strict" in {
      Get() ~> {
        cors(allowGenericHttpRequests = true) & completeActual
      } ~> check {
        status shouldBe actualStatus
      }
    }

    "reject requests without Origin when strict" in {
      Get() ~> {
        cors(allowGenericHttpRequests = false) & completeActual
      } ~> check {
        rejection shouldBe InvalidCorsRequestRejection
      }
    }

    "accept actual requests with Origin when strict" in {
      Get() ~> Origin(exampleOrigin) ~> {
        cors(allowGenericHttpRequests = false) & completeActual
      } ~> check {
        status shouldBe actualStatus
      }
    }

  }
}
