package ch.megard.akka.http.cors.javadsl

import java.util.function.Supplier

import akka.http.javadsl.server.{RejectionHandler, Route}
import akka.http.javadsl.server.directives.RouteAdapter
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl

object CorsDirectives {
  def cors(inner: Supplier[Route]): Route = RouteAdapter {
    scaladsl.CorsDirectives.cors() {
      inner.get() match {
        case ra: RouteAdapter => ra.delegate
      }
    }
  }

  def cors(settings: CorsSettings, inner: Supplier[Route]): Route = RouteAdapter {
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
