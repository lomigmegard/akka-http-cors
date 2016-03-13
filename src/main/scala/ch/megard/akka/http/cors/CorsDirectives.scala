package ch.megard.akka.http.cors

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives._

import scala.collection.immutable.Seq

/**
  * Provides directives that implement the CORS mechanism, enabling cross origin requests.
  *
  * @see <a href="https://www.w3.org/TR/cors/">CORS W3C Recommendation</a>.
  * @see <a href="https://www.ietf.org/rfc/rfc6454.txt">RFC 6454</a>.
  *
  */
trait CorsDirectives {

  import BasicDirectives._
  import CorsDirectives._
  import RespondWithDirectives._
  import RouteDirectives._

  /**
    * Wraps its inner route with support for the CORS mechanism, enabling cross origin requests.
    *
    * In particular the recommendation written by the W3C in https://www.w3.org/TR/cors/ is
    * implemented by this directive.
    *
    * @param settings the settings used by the CORS filter
    */
  def cors(settings: CorsSettings = CorsSettings.defaultSettings): Directive0 = corsDecorate(settings).map(_ ⇒ ())

  def corsDecorate(settings: CorsSettings = CorsSettings.defaultSettings): Directive1[CorsDecorate] = {
    import settings._

    def accessControlExposeHeaders: Option[`Access-Control-Expose-Headers`] = {
      if (exposedHeaders.nonEmpty) Some(`Access-Control-Expose-Headers`(exposedHeaders))
      else None
    }

    def accessControlAllowCredentials: Option[`Access-Control-Allow-Credentials`] = {
      if (allowCredentials) Some(`Access-Control-Allow-Credentials`(true))
      else None
    }

    def accessControlMaxAge: Option[`Access-Control-Max-Age`] = {
      maxAge.map(`Access-Control-Max-Age`.apply)
    }

    def accessControlAllowHeaders(requestHeaders: Seq[String]): Option[`Access-Control-Allow-Headers`] = allowedHeaders match {
      case HttpHeaderRange.Default(headers) ⇒ Some(`Access-Control-Allow-Headers`(headers))
      case HttpHeaderRange.* if requestHeaders.nonEmpty ⇒ Some(`Access-Control-Allow-Headers`(requestHeaders))
      case _ ⇒ None
    }

    def accessControlAllowMethods = {
      `Access-Control-Allow-Methods`(allowedMethods)
    }

    def accessControlAllowOrigin(origins: Seq[HttpOrigin]): `Access-Control-Allow-Origin` = {
      if (allowedOrigins == HttpOriginRange.* && !allowCredentials) {
        `Access-Control-Allow-Origin`.*
      } else {
        `Access-Control-Allow-Origin`.forRange(HttpOriginRange.Default(origins))
      }
    }

    /** Return an invalid origin, or `None` if they are all valid. */
    def validateOrigin(origins: Seq[HttpOrigin]): Option[HttpOrigin] =
      origins.find(!allowedOrigins.matches(_))

    /** Return the method if invalid, `None` otherwise. */
    def validateMethod(method: HttpMethod): Option[HttpMethod] =
      Some(method).filterNot(allowedMethods.contains)

    /** Return the list of invalid headers, or `None` if they are all valid. */
    def validateHeaders(headers: Seq[String]): Option[Seq[String]] =
      Some(headers.filterNot(allowedHeaders.matches)).filter(_.nonEmpty)

    extractRequest.flatMap { request ⇒
      import request._

      (method, header[Origin].map(_.origins), header[`Access-Control-Request-Method`].map(_.method)) match {
        case (OPTIONS, Some(origins), Some(requestMethod)) if origins.size == 1 ⇒
          // Case 1: pre-flight CORS request

          val headers = header[`Access-Control-Request-Headers`].map(_.headers).getOrElse(Seq.empty)

          def completePreflight = {
            val responseHeaders = Seq(accessControlAllowOrigin(origins), accessControlAllowMethods) ++
              accessControlAllowHeaders(headers) ++ accessControlMaxAge ++ accessControlAllowCredentials
            complete(HttpResponse(StatusCodes.OK, responseHeaders))
          }

          (validateOrigin(origins), validateMethod(requestMethod), validateHeaders(headers)) match {
            case (None, None, None) ⇒
              completePreflight
            case (invalidOrigin, invalidMethod, invalidHeaders) ⇒
              reject(CorsRejection(invalidOrigin, invalidMethod, invalidHeaders))
          }

        case (_, Some(origins), None) if origins.nonEmpty ⇒
          // Case 2: actual CORS request

          val decorate: CorsDecorate = CorsDecorate.CorsRequest(origins)
          val responseHeaders: Seq[HttpHeader] = Seq(accessControlAllowOrigin(origins)) ++
            accessControlExposeHeaders ++ accessControlAllowCredentials

          validateOrigin(origins) match {
            case None ⇒
              respondWithHeaders(responseHeaders) & provide(decorate)
            case invalidOrigin ⇒
              reject(CorsRejection(invalidOrigin, None, None))
          }

        case _ if allowGenericHttpRequests ⇒
          // Case 3a: not a valid CORS request, but allowed

          provide(CorsDecorate.NotCorsRequest)

        case _ ⇒
          // Case 3b: not a valid CORS request, forbidden

          reject(CorsRejection(None, None, None))
      }
    }
  }

}

object CorsDirectives extends CorsDirectives {

  def corsRejectionHandler = RejectionHandler.newBuilder().handle {
    case CorsRejection(None, None, None) ⇒
      complete((StatusCodes.BadRequest, "The CORS request is malformed"))
    case CorsRejection(origin, method, headers) ⇒
      val messages = Seq(
        origin.map(s"invalid origin '" + _ + "'"),
        method.map(s"invalid method '" + _.value + "'"),
        headers.map(s"invalid headers '" + _.mkString(",") + "'")
      ).flatten
      complete((StatusCodes.BadRequest, "CORS: " + messages.mkString(", ")))
  }.result()

  sealed abstract class CorsDecorate {
    def isCorsRequest: Boolean
  }

  object CorsDecorate {

    case class CorsRequest(origins: Seq[HttpOrigin]) extends CorsDecorate {
      def isCorsRequest = true
    }

    case object NotCorsRequest extends CorsDecorate {
      def isCorsRequest = false
    }

  }

}
