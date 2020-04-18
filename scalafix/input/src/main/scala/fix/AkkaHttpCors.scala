/*
rule = AkkaHttpCors
*/
package fix

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

object AkkaHttpCors {

  import akka.http.scaladsl.server.Directives._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  implicit val system: ActorSystem = ???

  val corsSettings = CorsSettings.defaultSettings

  val route1: Route =
    cors() {
      complete(StatusCodes.OK)
    }

  val route2: Route =
    cors () {
      complete(StatusCodes.OK)
    }

  val route3: Route =
    cors(corsSettings) {
      complete(StatusCodes.OK)
    }

}
