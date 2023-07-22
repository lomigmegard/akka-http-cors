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

package ch.megard.akka.http.cors.scaladsl.model

import akka.http.scaladsl.model.headers.HttpOrigin
import org.scalatest.Inspectors
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HttpOriginMatcherSpec extends AnyWordSpec with Matchers with Inspectors {
  "The `*` matcher" should {
    "match any Origin" in {
      val origins = Seq(
        "http://localhost",
        "http://192.168.1.1",
        "http://test.com",
        "http://test.com:8080",
        "https://test.com",
        "https://test.com:4433"
      ).map(HttpOrigin.apply)

      forAll(origins) { o => HttpOriginMatcher.*.matches(o) shouldBe true }
    }

    "be printed as `*`" in {
      HttpOriginMatcher.*.toString shouldBe "*"
    }
  }

  "The strict() method" should {
    "build a strict matcher, comparing exactly the origins" in {
      val positives = Seq(
        "http://localhost",
        "http://test.com",
        "https://test.ch:12345",
        "https://*.test.uk.co"
      ).map(HttpOrigin.apply)

      val negatives = Seq(
        "http://localhost:80",
        "https://localhost",
        "http://test.com:8080",
        "https://test.ch",
        "https://abc.test.uk.co"
      ).map(HttpOrigin.apply)

      val matcher = HttpOriginMatcher.strict(positives: _*)

      forAll(positives) { o => matcher.matches(o) shouldBe true }

      forAll(negatives) { o => matcher.matches(o) shouldBe false }
    }

    "build a matcher with a toString() method that is a valid range" in {
      val matcher = HttpOriginMatcher(Seq("http://test.com", "https://test.ch:12345").map(HttpOrigin.apply): _*)
      matcher.toString shouldBe "http://test.com https://test.ch:12345"
    }
  }

  "The apply() method" should {
    "build a matcher accepting sub-domains with wildcards" in {
      val matcher = HttpOriginMatcher(
        Seq(
          "http://test.com",
          "https://test.ch:12345",
          "https://*.test.uk.co",
          "http://*.abc.com:8080",
          "http://*abc.com",        // Must start with `*.`
          "http://abc.*.middle.com" // The wildcard can't be in the middle
        ).map(HttpOrigin.apply): _*
      )

      val positives = Seq(
        "http://test.com",
        "https://test.ch:12345",
        "https://sub.test.uk.co",
        "https://sub1.sub2.test.uk.co",
        "http://sub.abc.com:8080"
      ).map(HttpOrigin.apply)

      val negatives = Seq(
        "http://test.com:8080",
        "http://sub.test.uk.co", // must compare the scheme
        "http://sub.abc.com",    // must compare the port
        "http://abc.test.com",   // no wildcard
        "http://sub.abc.com",
        "http://subabc.com",
        "http://abc.sub.middle.com",
        "http://abc.middle.com"
      ).map(HttpOrigin.apply)

      forAll(positives) { o => matcher.matches(o) shouldBe true }

      forAll(negatives) { o => matcher.matches(o) shouldBe false }
    }

    "build a matcher with a toString() method that is a valid range" in {
      val matcher = HttpOriginMatcher(Seq("http://test.com", "https://*.test.ch:12345").map(HttpOrigin.apply): _*)
      matcher.toString shouldBe "http://test.com https://*.test.ch:12345"
    }
  }
}
