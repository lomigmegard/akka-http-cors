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

package ch.megard.akka.http.cors.scaladsl.settings

import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CorsSettingsSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {
  import HttpMethods._

  // Override some configs loaded through the Actor system
  override def testConfigSource =
    """
      akka-http-cors {
        allow-credentials = false
      }
    """

  val validConfig = ConfigFactory.parseString(
    """
      akka-http-cors {
        allow-generic-http-requests = true
        allow-credentials = true
        allowed-origins = "*"
        allowed-headers = "*"
        allowed-methods = ["GET", "OPTIONS", "XXX"]
        exposed-headers = []
        max-age = 30 minutes
      }
    """
  )

  val referenceSettings = CorsSettings("")

  "CorsSettings" should {

    "load settings from the actor system by default" in {
      val settings1 = CorsSettings.default
      val settings2 = CorsSettings(system)

      settings1 should not be referenceSettings
      settings1 shouldBe settings2

      referenceSettings.allowCredentials shouldBe true
      settings1.allowCredentials shouldBe false
    }

    "cache the settings from the actor system" in {
      val settings1 = CorsSettings(system)
      val settings2 = CorsSettings(system)

      settings1 shouldBe theSameInstanceAs(settings2)
    }

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
