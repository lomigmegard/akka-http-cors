package ch.megard.akka.http.cors

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{StatusCodes, HttpHeader, HttpMethod, HttpResponse}
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

    def validOrigin(origins: Seq[HttpOrigin]): Directive0 = origins.find(!allowedOrigins.matches(_)) match {
      case Some(origin) ⇒ reject(CorsOriginRejection(origin))
      case None ⇒ pass
    }

    def validMethod(method: HttpMethod): Directive0 = {
      if (allowedMethods.contains(method)) {
        pass
      } else {
        reject(CorsMethodRejection(method))
      }
    }

    def validHeaders(headers: Seq[String]): Directive0 =  {
      val unsupportedHeaders = headers.filterNot(allowedHeaders.matches)
      if (unsupportedHeaders.isEmpty) {
        pass
      } else {
        reject(CorsHeaderRejection(unsupportedHeaders))
      }
    }

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

          validOrigin(origins) & validMethod(requestMethod) & validHeaders(headers) & completePreflight

        case (_, Some(origins), None) if origins.nonEmpty ⇒
          // Case 2: actual CORS request

          val decorate: CorsDecorate = CorsDecorate.CorsRequest(origins)
          val responseHeaders: Seq[HttpHeader] = Seq(accessControlAllowOrigin(origins)) ++
            accessControlExposeHeaders ++ accessControlAllowCredentials

          validOrigin(origins) & respondWithHeaders(responseHeaders) & provide(decorate)

        case _ if allowGenericHttpRequests ⇒
          // Case 3a: not a CORS request, but allowed

          provide(CorsDecorate.NotCorsRequest)

        case _ ⇒
          // Case 3b: not a CORS request, forbidden

          reject(InvalidCorsRequestRejection)
      }
    }
  }

}

object CorsDirectives extends CorsDirectives {

  case object InvalidCorsRequestRejection extends Rejection
  case class CorsOriginRejection(origin: HttpOrigin) extends Rejection
  case class CorsMethodRejection(method: HttpMethod) extends Rejection
  case class CorsHeaderRejection(unsupportedHeaders: Seq[String]) extends Rejection

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

  abstract class CorsSettings {
    def allowGenericHttpRequests: Boolean
    def allowCredentials: Boolean
    def allowedOrigins: HttpOriginRange
    def allowedHeaders: HttpHeaderRange
    def allowedMethods: Seq[HttpMethod]
    def exposedHeaders: Seq[String]
    def maxAge: Option[Long]
  }

  object CorsSettings {

    final case class Default(
        allowGenericHttpRequests: Boolean,
        allowCredentials: Boolean,
        allowedOrigins: HttpOriginRange,
        allowedHeaders: HttpHeaderRange,
        allowedMethods: Seq[HttpMethod],
        exposedHeaders: Seq[String],
        maxAge: Option[Long]
    ) extends CorsSettings

    val defaultSettings = CorsSettings.Default(
      allowGenericHttpRequests = true,
      allowCredentials = true,
      allowedOrigins = HttpOriginRange.*,
      allowedHeaders = HttpHeaderRange.*,
      allowedMethods = Seq(GET, POST, HEAD, OPTIONS),
      exposedHeaders = Seq.empty,
      maxAge = Some(30 * 60)
    )

  }

  sealed abstract class HttpHeaderRange {
    def matches(header: String): Boolean
  }

  object HttpHeaderRange {
    case object `*` extends HttpHeaderRange {
      def matches(header: String) = true
    }

    def apply(headers: String*): Default = Default(Seq(headers: _*))

    final case class Default(headers: Seq[String]) extends HttpHeaderRange {
      val lowercaseHeaders = headers.map(_.toLowerCase)
      def matches(header: String): Boolean = lowercaseHeaders contains header.toLowerCase
    }
  }
}
