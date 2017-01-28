package ch.megard.akka.http.cors

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.CorsDecorate._
import ch.megard.akka.http.cors.scaladsl.CorsRejection
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable

/**
  * @author Lomig Mégard
  */
class CorsDirectivesSpec extends WordSpec with Matchers with Directives with ScalatestRouteTest {

  import HttpMethods._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  val actual = "actual"
  val exampleOrigin = HttpOrigin("http://example.com")

  def route(settings: CorsSettings): Route = cors(settings) {
    complete(actual)
  }

  def routeDecorate(settings: CorsSettings): Route = corsDecorate(settings) {
    case CorsRequest(origins) ⇒ complete("actual cors")
    case NotCorsRequest       ⇒ complete("not cors")
  }

  "The cors directive" should {

    "not affect actual requests when not strict" in {
      val settings = CorsSettings.defaultSettings
      Get() ~> {
        route(settings)
      } ~> check {
        responseAs[String] shouldBe actual
      }
    }

    "reject requests without Origin when strict" in {
      val settings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = false)
      Get() ~> {
        route(settings)
      } ~> check {
        rejection shouldBe CorsRejection(None, None, None)
      }
    }

    "accept actual requests with Origin" in {
      val settings = CorsSettings.defaultSettings
      Get() ~> Origin(exampleOrigin) ~> {
        route(settings)
      } ~> check {
        responseAs[String] shouldBe actual
        response.headers should contain theSameElementsAs Seq(
          `Access-Control-Allow-Origin`(exampleOrigin),
          `Access-Control-Allow-Credentials`(true)
        )
      }
    }

    "accept valid pre-flight requests" in {
      val settings = CorsSettings.defaultSettings
      Options() ~> Origin(exampleOrigin) ~> `Access-Control-Request-Method`(GET) ~> {
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

    "accept actual requests with OPTION method" in {
      val settings = CorsSettings.defaultSettings
      Options() ~> Origin(exampleOrigin) ~> {
        route(settings)
      } ~> check {
        responseAs[String] shouldBe actual
        response.headers should contain theSameElementsAs Seq(
          `Access-Control-Allow-Origin`(exampleOrigin),
          `Access-Control-Allow-Credentials`(true)
        )
      }
    }

    "reject pre-flight requests with invalid origin" in {
      val settings = CorsSettings.defaultSettings.copy(allowedOrigins = HttpOriginRange(exampleOrigin))
      val invalidOrigin = HttpOrigin("http://invalid.com")
      Options() ~> Origin(invalidOrigin) ~> `Access-Control-Request-Method`(GET) ~> {
        route(settings)
      } ~> check {
        rejection shouldBe CorsRejection(Some(invalidOrigin), None, None)
      }
    }

    "reject pre-flight requests with invalid method" in {
      val settings = CorsSettings.defaultSettings
      val invalidMethod = PATCH
      Options() ~> Origin(exampleOrigin) ~> `Access-Control-Request-Method`(invalidMethod) ~> {
        route(settings)
      } ~> check {
        rejection shouldBe CorsRejection(None, Some(invalidMethod), None)
      }
    }

    "reject pre-flight requests with invalid header" in {
      val settings = CorsSettings.defaultSettings.copy(allowedHeaders = HttpHeaderRange())
      val invalidHeader = "X-header"
      Options() ~> Origin(exampleOrigin) ~> `Access-Control-Request-Method`(GET) ~>
        `Access-Control-Request-Headers`(invalidHeader) ~> {
        route(settings)
      } ~> check {
        rejection shouldBe CorsRejection(None, None, Some(immutable.Seq(invalidHeader)))
      }
    }

  }

  "the default rejection handler" should {
    val settings = CorsSettings.defaultSettings
      .copy(allowGenericHttpRequests = false)
      .copy(allowedOrigins = HttpOriginRange(exampleOrigin))
      .copy(allowedHeaders = HttpHeaderRange())
    val sealedRoute = handleRejections(corsRejectionHandler) { route(settings) }

    "handle the invalid CORS request rejection" in {
      Get() ~> {
        sealedRoute
      } ~> check {
        status shouldBe StatusCodes.BadRequest
        entityAs[String] shouldBe "The CORS request is malformed"
      }
    }

    "handle a pre-flight request with invalid origin, method and headers" in {
      val invalidOrigin = HttpOrigin("http://invalid.com")
      val invalidMethod = PATCH
      val invalidHeaders = immutable.Seq("X-header", "Y-header")
      Options() ~> Origin(invalidOrigin) ~> `Access-Control-Request-Method`(invalidMethod) ~>
        `Access-Control-Request-Headers`(invalidHeaders) ~> {
        sealedRoute
      } ~> check {
        status shouldBe StatusCodes.BadRequest
        entityAs[String] shouldBe "CORS: " +
          s"invalid origin '$invalidOrigin', " +
          s"invalid method '${invalidMethod.value}', " +
          s"invalid headers '${invalidHeaders.mkString(",")}'"
      }
    }

  }

}
