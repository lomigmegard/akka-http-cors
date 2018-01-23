package ch.megard.akka.http.cors

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.CorsDecorate._
import ch.megard.akka.http.cors.scaladsl.CorsRejection
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable.Seq

class CorsDirectivesSpec extends WordSpec with Matchers with Directives with ScalatestRouteTest {

  import HttpMethods._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  val actual = "actual"
  val exampleOrigin = HttpOrigin("http://example.com")
  val exampleStatus = StatusCodes.Created

  def route(settings: CorsSettings, responseHeaders: Seq[HttpHeader] = Nil): Route = cors(settings) {
    complete(HttpResponse(exampleStatus, responseHeaders, HttpEntity(actual)))
  }

  def routeDecorate(settings: CorsSettings): Route = corsDecorate(settings) {
    case CorsRequest(origins) ⇒ complete("actual cors from " + origins)
    case NotCorsRequest       ⇒ complete("not cors")
  }

  "The cors directive" should {

    "not affect actual requests when not strict" in {
      val settings = CorsSettings.defaultSettings
      val responseHeaders = Seq(Host("my-host"), `Access-Control-Max-Age`(60))
      Get() ~> {
        route(settings, responseHeaders)
      } ~> check {
        responseAs[String] shouldBe actual
        response.status shouldBe exampleStatus
        // response headers should be untouched, including the CORS-related ones
        response.headers shouldBe responseHeaders
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
        response.status shouldBe exampleStatus
        response.headers should contain theSameElementsAs Seq(
          `Access-Control-Allow-Origin`(exampleOrigin),
          `Access-Control-Allow-Credentials`(true)
        )
      }
    }

    "return `Access-Control-Allow-Origin: *` to actual request only when credentials are not allowed" in {
      val settings = CorsSettings.defaultSettings.copy(allowCredentials = false)
      Get() ~> Origin(exampleOrigin) ~> {
        route(settings)
      } ~> check {
        responseAs[String] shouldBe actual
        response.status shouldBe exampleStatus
        response.headers shouldBe Seq(
          `Access-Control-Allow-Origin`.*
        )
      }
    }

    "return `Access-Control-Expose-Headers` to actual request with all the exposed headers in the settings" in {
      val exposedHeaders = Seq("X-a", "X-b", "X-c")
      val settings = CorsSettings.defaultSettings.copy(exposedHeaders = exposedHeaders)
      Get() ~> Origin(exampleOrigin) ~> {
        route(settings)
      } ~> check {
        responseAs[String] shouldBe actual
        response.status shouldBe exampleStatus
        response.headers shouldBe Seq(
          `Access-Control-Allow-Origin`(exampleOrigin),
          `Access-Control-Expose-Headers`(exposedHeaders),
          `Access-Control-Allow-Credentials`(true)
        )
      }
    }

    "remove CORS-related headers from the original response before adding the new ones" in {
      val settings = CorsSettings.defaultSettings.copy(exposedHeaders = Seq("X-good"))
      val responseHeaders = Seq(
        Host("my-host"), // untouched
        `Access-Control-Allow-Origin`("http://bad.com"), // replaced
        `Access-Control-Expose-Headers`("X-bad"), // replaced
        `Access-Control-Allow-Credentials`(false), // replaced
        `Access-Control-Allow-Methods`(HttpMethods.POST), // removed
        `Access-Control-Allow-Headers`("X-bad"), // removed
        `Access-Control-Max-Age`(60) // removed
      )
      Get() ~> Origin(exampleOrigin) ~> {
        route(settings, responseHeaders)
      } ~> check {
        responseAs[String] shouldBe actual
        response.status shouldBe exampleStatus
        response.headers should contain theSameElementsAs Seq(
          `Access-Control-Allow-Origin`(exampleOrigin),
          `Access-Control-Expose-Headers`("X-good"),
          `Access-Control-Allow-Credentials`(true),
          Host("my-host")
        )
      }
    }

    "accept valid pre-flight requests" in {
      val settings = CorsSettings.defaultSettings
      Options() ~> Origin(exampleOrigin) ~> `Access-Control-Request-Method`(GET) ~> {
        route(settings)
      } ~> check {
        response.entity shouldBe HttpEntity.Empty
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
        response.status shouldBe exampleStatus
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
        rejection shouldBe CorsRejection(None, None, Some(Seq(invalidHeader)))
      }
    }

    "reject pre-flight requests with multiple origins" in {
      val settings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = false)
      Options() ~> Origin(exampleOrigin, exampleOrigin) ~> `Access-Control-Request-Method`(GET) ~> {
        route(settings)
      } ~> check {
        rejection shouldBe CorsRejection(None, None, None)
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
      val invalidHeaders = Seq("X-header", "Y-header")
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
