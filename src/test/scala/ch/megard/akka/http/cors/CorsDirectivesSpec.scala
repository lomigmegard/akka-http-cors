package ch.megard.akka.http.cors

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, StatusCodes}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ch.megard.akka.http.cors.CorsDirectives.InvalidCorsRequestRejection
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Lomig Mégard
  */
class CorsDirectivesSpec extends WordSpec with Matchers with Directives with ScalatestRouteTest {

  import CorsDirectives._

  val actual = "actual"
  val completeActual = complete(actual)
  val exampleOrigin = HttpOrigin("http://example.com")

  def route(settings: CorsSettings = CorsSettings.defaultSettings): Route = cors(settings) {
    complete(actual)
  }

  "The cors directive" should {

    "not affect actual requests when not strict" in {
      Get() ~> {
        route()
      } ~> check {
        responseAs[String] shouldBe actual
      }
    }

    "reject requests without Origin when strict" in {
      val settings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = false)
      Get() ~> {
        route(settings)
      } ~> check {
        rejection shouldBe InvalidCorsRequestRejection
      }
    }

    "accept actual requests with Origin when strict" in {
      val settings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = false)
      Get() ~> Origin(exampleOrigin) ~> {
        route(settings)
      } ~> check {
        responseAs[String] shouldBe actual
        response.headers shouldBe Seq(
          `Access-Control-Allow-Origin`(exampleOrigin),
          `Access-Control-Allow-Credentials`(true)
        )
      }
    }

    "accept valid pre-flight requests" in {
      val settings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = false)
      Options() ~> Origin(exampleOrigin) ~> `Access-Control-Request-Method`(HttpMethods.GET) ~> {
        route(settings)
      } ~> check {
        responseAs[String] shouldBe empty
        status shouldBe StatusCodes.OK
        response.headers should contain theSameElementsAs Seq(
          `Access-Control-Allow-Origin`(exampleOrigin),
          `Access-Control-Allow-Methods`(settings.allowedMethods),
          `Access-Control-Max-Age`(1800),
          `Access-Control-Allow-Credentials`(true)
        )
      }
    }

  }
}
