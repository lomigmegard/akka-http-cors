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

import java.util.Locale

import ch.megard.akka.http.cors.javadsl

import scala.collection.immutable.Seq

abstract class HttpHeaderRange extends javadsl.model.HttpHeaderRange

object HttpHeaderRange {
  case object `*` extends HttpHeaderRange {
    def matches(header: String) = true
  }

  final case class Default(headers: Seq[String]) extends HttpHeaderRange {
    val lowercaseHeaders: Seq[String]    = headers.map(_.toLowerCase(Locale.ROOT))
    def matches(header: String): Boolean = lowercaseHeaders contains header.toLowerCase(Locale.ROOT)
  }

  def apply(headers: String*): Default = Default(Seq(headers: _*))
}
