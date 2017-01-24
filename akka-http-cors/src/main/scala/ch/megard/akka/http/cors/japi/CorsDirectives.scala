package ch.megard.akka.http.cors.japi

import java.util.function.Supplier

import akka.http.javadsl.server.Route
import akka.http.javadsl.server.directives.RouteAdapter

object CorsDirectives {

  def cors(settings: CorsSettings, inner: Supplier[Route]): Route = RouteAdapter {
    // Currently the easiest way to go from Java models to their Scala equivalent is to cast.
    // See https://github.com/akka/akka-http/issues/661 for a potential opening of the JavaMapping API.
    val scalaSettings = settings.asInstanceOf[ch.megard.akka.http.cors.CorsSettings]
    ch.megard.akka.http.cors.CorsDirectives.cors(scalaSettings) {
      inner.get() match {
        case ra: RouteAdapter => ra.delegate
      }
    }
  }

}
