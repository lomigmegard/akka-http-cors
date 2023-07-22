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

package ch.megard.akka.http.cors.javadsl

import akka.http.javadsl.model.HttpMethod
import akka.http.javadsl.model.headers.HttpOrigin
import akka.http.javadsl.server.CustomRejection

/** Rejection created by the CORS directives. Signal the CORS request was rejected. The reason of the rejection is
  * specified in the cause.
  */
trait CorsRejection extends CustomRejection {
  def cause: CorsRejection.Cause
}

object CorsRejection {

  /** Signals the cause of the failed CORS request.
    */
  trait Cause {

    /** Description of this Cause in a human-readable format. Can be used for debugging or custom Rejection handlers.
      */
    def description: String
  }

  /** Signals the CORS request was malformed.
    */
  trait Malformed extends Cause

  /** Signals the CORS request was rejected because its origin was invalid. An empty list means the Origin header was
    * `null`.
    */
  trait InvalidOrigin extends Cause {
    def getOrigins: java.util.List[HttpOrigin]
  }

  /** Signals the CORS request was rejected because its method was invalid.
    */
  trait InvalidMethod extends Cause {
    def getMethod: HttpMethod
  }

  /** Signals the CORS request was rejected because its headers were invalid.
    */
  trait InvalidHeaders extends Cause {
    def getHeaders: java.util.List[String]
  }
}
