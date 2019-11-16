package ch.megard.akka.http.cors.scaladsl.settings

import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.scalatest.{Matchers, WordSpec}

class CorsSettingsSpec extends WordSpec with Matchers {
  import HttpMethods._

  private val validConfigStr =
    """
      |akka-http-cors {
      |  allow-generic-http-requests = true
      |  allow-credentials = true
      |  allowed-origins = "*"
      |  allowed-headers = "*"
      |  allowed-methods = ["GET", "OPTIONS", "XXX"]
      |  exposed-headers = []
      |  max-age = 30 minutes
      |}
    """.stripMargin

  private val validConfig = ConfigFactory.parseString(validConfigStr)

  "The CorsSettings object" should {
    "return valid cors settings from a valid config object" in {
      val corsSettings = CorsSettings(validConfig)
      corsSettings.allowGenericHttpRequests shouldBe true
      corsSettings.allowCredentials shouldBe true
      corsSettings.allowedOrigins shouldBe HttpOriginMatcher.*
      corsSettings.allowedHeaders shouldBe HttpHeaderRange.*
      corsSettings.allowedMethods shouldBe List(GET, OPTIONS, HttpMethod.custom("XXX"))
      corsSettings.exposedHeaders shouldBe List.empty
      corsSettings.maxAge shouldBe Some(1800)
    }

    "support space separated list of origins" in {
      val config = validConfig.withValue(
        "akka-http-cors.allowed-origins",
        ConfigValueFactory.fromAnyRef("http://test.com http://any.com")
      )
      val corsSettings = CorsSettings(config)
      corsSettings.allowedOrigins shouldBe HttpOriginMatcher(
        HttpOrigin("http://test.com"),
        HttpOrigin("http://any.com")
      )
    }

    "support wildcard subdomains" in {
      val config = validConfig.withValue(
        "akka-http-cors.allowed-origins",
        ConfigValueFactory.fromAnyRef("http://*.test.com")
      )
      val corsSettings = CorsSettings(config)
      corsSettings.allowedOrigins.matches(HttpOrigin("http://sub.test.com")) shouldBe true
    }

    "support numeric values on max-age as seconds" in {
      val corsSettings = CorsSettings(
        validConfig.withValue("akka-http-cors.max-age", ConfigValueFactory.fromAnyRef(1800))
      )
      corsSettings.maxAge shouldBe Some(1800)
    }

    "support null value on max-age" in {
      val corsSettings = CorsSettings(
        validConfig.withValue("akka-http-cors.max-age", ConfigValueFactory.fromAnyRef(null))
      )
      corsSettings.maxAge shouldBe None
    }

    "support undefined on max-age" in {
      val corsSettings = CorsSettings(validConfig.withoutPath("akka-http-cors.max-age"))
      corsSettings.maxAge shouldBe None
    }
  }
}
