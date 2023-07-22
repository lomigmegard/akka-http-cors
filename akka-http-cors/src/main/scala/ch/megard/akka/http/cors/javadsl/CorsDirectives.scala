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

import java.util.function.Supplier

import akka.http.javadsl.server.{RejectionHandler, Route}
import akka.http.javadsl.server.directives.RouteAdapter
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl

object CorsDirectives {

  def cors(inner: Supplier[Route]): Route =
    RouteAdapter {
      scaladsl.CorsDirectives.cors() {
        inner.get() match {
          case ra: RouteAdapter => ra.delegate
        }
      }
    }

  def cors(settings: CorsSettings, inner: Supplier[Route]): Route =
    RouteAdapter {
      // Currently the easiest way to go from Java models to their Scala equivalent is to cast.
      // See https://github.com/akka/akka-http/issues/661 for a potential opening of the JavaMapping API.
      val scalaSettings = settings.asInstanceOf[scaladsl.settings.CorsSettings]
      scaladsl.CorsDirectives.cors(scalaSettings) {
        inner.get() match {
          case ra: RouteAdapter => ra.delegate
        }
      }
    }

  def corsRejectionHandler: RejectionHandler =
    new RejectionHandler(scaladsl.CorsDirectives.corsRejectionHandler)
}
