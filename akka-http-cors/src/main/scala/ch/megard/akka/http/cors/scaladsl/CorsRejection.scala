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

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Rejection
import ch.megard.akka.http.cors.javadsl

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

/** Rejection created by the CORS directives. Signal the CORS request was rejected. The reason of the rejection is
  * specified in the cause.
  */
final case class CorsRejection(cause: CorsRejection.Cause) extends javadsl.CorsRejection with Rejection

object CorsRejection {

  /** Signals the cause of the failed CORS request.
    */
  sealed trait Cause extends javadsl.CorsRejection.Cause

  /** Signals the CORS request was malformed.
    */
  case object Malformed extends javadsl.CorsRejection.Malformed with Cause {
    override def description: String = "malformed request"
  }

  /** Signals the CORS request was rejected because its origin was invalid. An empty list means the Origin header was
    * `null`.
    */
  final case class InvalidOrigin(origins: Seq[HttpOrigin]) extends javadsl.CorsRejection.InvalidOrigin with Cause {
    override def description: String = s"invalid origin '${if (origins.isEmpty) "null" else origins.mkString(" ")}'"
    override def getOrigins          = (origins: Seq[akka.http.javadsl.model.headers.HttpOrigin]).asJava
  }

  /** Signals the CORS request was rejected because its method was invalid.
    */
  final case class InvalidMethod(method: HttpMethod) extends javadsl.CorsRejection.InvalidMethod with Cause {
    override def description: String = s"invalid method '${method.value}'"
    override def getMethod           = method
  }

  /** Signals the CORS request was rejected because its headers were invalid.
    */
  final case class InvalidHeaders(headers: Seq[String]) extends javadsl.CorsRejection.InvalidHeaders with Cause {
    override def description: String = s"invalid headers '${headers.mkString(" ")}'"
    override def getHeaders          = headers.asJava
  }
}
