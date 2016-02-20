package ch.megard.akka.http.cors

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpResponse}
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.{Directive, Directive0, MethodRejection}

import scala.collection.immutable.Seq

/**
  *
  */
trait CorsDirectives {

  import BasicDirectives._
  import CorsDirectives._
  import HeaderDirectives._
  import RespondWithDirectives._
  import RouteDirectives._

  def cors(
    allowGenericHttpRequests: Boolean = true,
    allowOrigin: HttpOriginRange = HttpOriginRange.*,
    supportedMethods: Seq[HttpMethod] = Seq(HttpMethods.GET, HttpMethods.POST, HttpMethods.HEAD, HttpMethods.OPTIONS),
    supportedHeaders: Seq[String] = Seq.empty,
    exposedHeaders: Seq[String] = Seq.empty,
    supportsCredentials: Boolean = true,
    maxAge: Option[Long]): Directive0 = {

    def accessControlExposeHeaders: Option[`Access-Control-Expose-Headers`] = {
      if (exposedHeaders.nonEmpty) Some(`Access-Control-Expose-Headers`(exposedHeaders))
      else None
    }

    def accessControlAllowCredentials: Option[`Access-Control-Allow-Credentials`] = {
      if (supportsCredentials) Some(`Access-Control-Allow-Credentials`(true))
      else None
    }

    def accessControlMaxAge: Option[`Access-Control-Max-Age`] = maxAge.map(`Access-Control-Max-Age`.apply)

    def accessControlAllowMethods = `Access-Control-Allow-Methods`(supportedMethods)

    def accessControlAllowHeaders = `Access-Control-Allow-Headers`(supportedHeaders)

    def accessControlAllowOrigin(origins: Seq[HttpOrigin]): `Access-Control-Allow-Origin` = {
      if (allowOrigin == HttpOriginRange.* && !supportsCredentials) {
        `Access-Control-Allow-Origin`.*
      } else {
        `Access-Control-Allow-Origin`.forRange(HttpOriginRange.Default(origins))
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
      if (origins.forall(allowOrigin.matches)) {
        val headers = Seq(accessControlAllowOrigin(origins)) ++ accessControlExposeHeaders ++ accessControlAllowCredentials
        respondWithHeaders(headers)
      } else {
        reject // 403
      }
    }

    def handleCorsPreflightRequest(origin: HttpOrigin, method: HttpMethod): Directive0 = {
      if (allowOrigin.matches(origin)) {
        if (supportedMethods.contains(method)) {
          validRequestHeaders & {
            val headers = Seq(
              accessControlAllowOrigin(Seq(origin)),
              accessControlAllowMethods,
              accessControlAllowHeaders) ++ accessControlMaxAge ++ accessControlAllowCredentials

            complete(HttpResponse(headers = headers))
          }
        } else {
          reject(MethodRejection(method))
        }
      } else {
        reject // 403
      }
    }

    _extractMethodAndOriginAndRequestMethod.tflatMap {
      case (HttpMethods.OPTIONS, Some(Origin(Seq(origin))), Some(method)) ⇒
        handleCorsPreflightRequest(origin, method)
      case (_, Some(Origin(origins)), None) ⇒
        handleCorsRequest(origins)
      case (_, None, _) if allowGenericHttpRequests ⇒
        pass
      case _  ⇒
        reject // invalid cors request
    }
  }

}

object CorsDirectives extends CorsDirectives {

  private val _extractMethodAndOriginAndRequestMethod: Directive[(HttpMethod, Option[Origin], Option[HttpMethod])] = {
    import BasicDirectives._
    import HeaderDirectives._
    import MethodDirectives._

    extractMethod.flatMap { method ⇒
      optionalHeaderValueByType[Origin](()).flatMap { origin ⇒
        optionalHeaderValueByType[`Access-Control-Request-Method`](()).flatMap { requestMethodHeader ⇒
          tprovide((method, origin, requestMethodHeader.map(_.method)))
        }
      }
    }
  }

}
