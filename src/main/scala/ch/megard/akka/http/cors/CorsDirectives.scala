package ch.megard.akka.http.cors

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpMethod, HttpResponse}
import akka.http.scaladsl.server.directives.HeaderDirectives._
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.{Directive1, Directive, Directive0, Rejection}

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

  def cors(implicit settings: CorsSettings): Directive0 = {
    corsDecorate(settings).map(_ => ())
  }

  def corsDecorate(implicit settings: CorsSettings): Directive1[CorsDecorate] = {
    // put all the settings in scope
    import settings._

    def accessControlExposeHeaders: Option[`Access-Control-Expose-Headers`] = {
      if (exposedHeaders.nonEmpty) Some(`Access-Control-Expose-Headers`(exposedHeaders))
      else None
    }

    def accessControlAllowCredentials: Option[`Access-Control-Allow-Credentials`] = {
      if (allowCredentials) Some(`Access-Control-Allow-Credentials`(true))
      else None
    }

    def accessControlMaxAge: Option[`Access-Control-Max-Age`] = maxAge.map(`Access-Control-Max-Age`.apply)

    def accessControlAllowHeaders(requestHeaders: Seq[String]): Option[`Access-Control-Allow-Headers`] = allowedHeaders match {
      case HttpHeaderRange.Default(headers) ⇒ Some(`Access-Control-Allow-Headers`(headers))
      case HttpHeaderRange.* if requestHeaders.nonEmpty ⇒ Some(`Access-Control-Allow-Headers`(requestHeaders))
      case _ ⇒ None
    }

    def accessControlAllowMethods = `Access-Control-Allow-Methods`(allowedMethods)

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

    def validHeaders(headers: Seq[String]): Directive0 = optionalHeaderValueByType[`Access-Control-Request-Headers`](()).flatMap {
      case Some(h) ⇒
        val unsupportedHeaders = h.headers.filterNot(allowedHeaders.matches)
        if (unsupportedHeaders.isEmpty) pass
        else reject(CorsHeaderRejection(unsupportedHeaders))
      case None ⇒
        pass
    }

    def handleCorsRequest(origins: Seq[HttpOrigin]): Directive1[CorsDecorate] = {
      validOrigin(origins) & {
        val headers = Seq(accessControlAllowOrigin(origins)) ++ accessControlExposeHeaders ++ accessControlAllowCredentials
        respondWithHeaders(headers) & provide(CorsDecorate.CorsRequest(origins): CorsDecorate)
      }
    }

    def handleCorsPreflightRequest(origins: Seq[HttpOrigin], method: HttpMethod, headers: Seq[String]): Directive1[CorsDecorate] = {
      assert(origins.size == 1)

      validOrigin(origins) & validMethod(method) & validHeaders(headers) & {
        val responseHeaders = Seq(accessControlAllowOrigin(origins), accessControlAllowMethods) ++
          accessControlAllowHeaders(headers) ++ accessControlMaxAge ++ accessControlAllowCredentials

        complete(HttpResponse(headers = responseHeaders)).toDirective[Tuple1[CorsDecorate]]
      }
    }

    _extractMethodAndOriginsAndRequestMethod.tflatMap {
      case (OPTIONS, Some(origins), Some(method)) if origins.size == 1 ⇒
        _extractRequestHeaders.flatMap { headers ⇒
          handleCorsPreflightRequest(origins, method, headers)
        }
      case (_, Some(origins), None) ⇒
        handleCorsRequest(origins)
      case (_, None, _) if allowGenericHttpRequests ⇒
        provide(CorsDecorate.NotCorsRequest)
      case _  ⇒
        reject(InvalidCorsRequestRejection)
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
    def allowedMethods: Seq[HttpMethod]
    def allowedHeaders: HttpHeaderRange
    def exposedHeaders: Seq[String]
    def maxAge: Option[Long]
  }

  object CorsSettings extends LowPriorityCorsSettingsImplicits {

    final case class Default(
        allowGenericHttpRequests: Boolean,
        allowCredentials: Boolean,
        allowedOrigins: HttpOriginRange,
        allowedMethods: Seq[HttpMethod],
        allowedHeaders: HttpHeaderRange,
        exposedHeaders: Seq[String],
        maxAge: Option[Long]
    ) extends CorsSettings

  }

  trait LowPriorityCorsSettingsImplicits {
    implicit val defaultSettings = CorsSettings.Default(
      allowGenericHttpRequests = true,
      allowCredentials = true,
      allowedOrigins = HttpOriginRange.*,
      allowedMethods = Seq(GET, POST, HEAD, OPTIONS),
      allowedHeaders = HttpHeaderRange.*,
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

  private val _extractRequestHeaders: Directive1[Seq[String]] = {
    optionalHeaderValueByType[`Access-Control-Request-Headers`](()).map(_.map(_.headers).getOrElse(Seq.empty))
  }

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
