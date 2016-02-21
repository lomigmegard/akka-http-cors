package ch.megard.akka.http.cors

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpMethod, HttpResponse}
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.{Directive, Directive0, Rejection}

import scala.collection.immutable.Seq

/**
  * @author Lomig Mégard
  */
trait CorsDirectives {

  import BasicDirectives._
  import CorsDirectives._
  import HeaderDirectives._
  import RespondWithDirectives._
  import RouteDirectives._

  def cors(
    allowGenericHttpRequests: Boolean = true,
    allowCredentials: Boolean = true,
    allowOrigin: HttpOriginRange = HttpOriginRange.*,
    supportedMethods: Seq[HttpMethod] = Seq(GET, POST, HEAD, OPTIONS),
    supportedHeaders: Seq[String] = Seq.empty,
    exposedHeaders: Seq[String] = Seq.empty,
    maxAge: Option[Long] = None): Directive0 = {

    def accessControlExposeHeaders: Option[`Access-Control-Expose-Headers`] = {
      if (exposedHeaders.nonEmpty) Some(`Access-Control-Expose-Headers`(exposedHeaders))
      else None
    }

    def accessControlAllowCredentials: Option[`Access-Control-Allow-Credentials`] = {
      if (allowCredentials) Some(`Access-Control-Allow-Credentials`(true))
      else None
    }

    def accessControlMaxAge: Option[`Access-Control-Max-Age`] = maxAge.map(`Access-Control-Max-Age`.apply)

    def accessControlAllowMethods = `Access-Control-Allow-Methods`(supportedMethods)

    def accessControlAllowHeaders = `Access-Control-Allow-Headers`(supportedHeaders)

    def accessControlAllowOrigin(origins: Seq[HttpOrigin]): `Access-Control-Allow-Origin` = {
      if (allowOrigin == HttpOriginRange.* && !allowCredentials) {
        `Access-Control-Allow-Origin`.*
      } else {
        `Access-Control-Allow-Origin`.forRange(HttpOriginRange.Default(origins))
      }
    }

    def validOrigin(origins: Seq[HttpOrigin]): Directive0 = {
      if (origins.forall(allowOrigin.matches)) {
        pass
      } else {
        reject // 403
      }
    }

    def validMethod(method: HttpMethod): Directive0 = {
      if (supportedMethods.contains(method)) {
        pass
      } else {
        reject(CorsMethodRejection(method))
      }
    }

    def validRequestHeaders: Directive0 = optionalHeaderValueByType[`Access-Control-Request-Headers`](()).flatMap {
      case Some(h) ⇒
        val headers = h.headers.map(_.toLowerCase)
        if (headers.forall(supportedHeaders.contains)) {
          pass
        } else {
          reject
        }
      case None ⇒
        pass
    }

    def handleCorsRequest(origins: Seq[HttpOrigin]): Directive0 = {
      validOrigin(origins) & {
        val headers = Seq(accessControlAllowOrigin(origins)) ++ accessControlExposeHeaders ++ accessControlAllowCredentials
        respondWithHeaders(headers)
      }
    }

    def handleCorsPreflightRequest(origins: Seq[HttpOrigin], method: HttpMethod): Directive0 = {
      assert(origins.size == 1)

      validOrigin(origins) & validMethod(method) & validRequestHeaders & {
        val headers = Seq(
          accessControlAllowOrigin(origins),
          accessControlAllowMethods,
          accessControlAllowHeaders) ++ accessControlMaxAge ++ accessControlAllowCredentials

        complete(HttpResponse(headers = headers)).toDirective[Unit]
      }
    }

    _extractMethodAndOriginsAndRequestMethod.tflatMap {
      case (OPTIONS, Some(origins), Some(method)) if origins.size == 1 ⇒
        handleCorsPreflightRequest(origins, method)
      case (_, Some(origins), None) ⇒
        handleCorsRequest(origins)
      case (_, None, _) if allowGenericHttpRequests ⇒
        pass
      case _  ⇒
        reject(InvalidCorsRequestRejection)
    }
  }

}

object CorsDirectives extends CorsDirectives {

  case object InvalidCorsRequestRejection extends Rejection
  case class CorsMethodRejection(method: HttpMethod) extends Rejection
  case class CorsHeaderRejection(unsupportedHeaders: Seq[String]) extends Rejection

  private val _extractMethodAndOriginsAndRequestMethod: Directive[(HttpMethod, Option[Seq[HttpOrigin]], Option[HttpMethod])] = {
    import BasicDirectives._
    import HeaderDirectives._
    import MethodDirectives._

    extractMethod.flatMap { method ⇒
      optionalHeaderValueByType[Origin](()).flatMap { origin ⇒
        optionalHeaderValueByType[`Access-Control-Request-Method`](()).flatMap { requestMethodHeader ⇒
          tprovide((method, origin.map(_.origins), requestMethodHeader.map(_.method)))
        }
      }
    }
  }

}
