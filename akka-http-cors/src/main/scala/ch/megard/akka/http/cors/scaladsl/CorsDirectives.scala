package ch.megard.akka.http.cors.scaladsl

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, HttpResponse, StatusCodes}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives._
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.collection.immutable.Seq

/**
  * Provides directives that implement the CORS mechanism, enabling cross origin requests.
  *
  * @see [[https://www.w3.org/TR/cors/ CORS W3C Recommendation]]
  * @see [[https://www.ietf.org/rfc/rfc6454.txt RFC 6454]]
  *
  */
trait CorsDirectives {

  import BasicDirectives._
  import RouteDirectives._

  /**
    * Wraps its inner route with support for the CORS mechanism, enabling cross origin requests.
    *
    * In particular the recommendation written by the W3C in https://www.w3.org/TR/cors/ is
    * implemented by this directive.
    *
    * @param settings the settings used by the CORS filter
    */
  def cors(settings: CorsSettings = CorsSettings.defaultSettings): Directive0 = {
    import settings._

    /** Return the invalid origins, or `None` if one is valid. */
    def validateOrigins(origins: Seq[HttpOrigin]): Option[CorsRejection.Cause] =
      if (allowedOrigins == HttpOriginMatcher.* || origins.exists(allowedOrigins.matches)) {
        None
      } else {
        Some(CorsRejection.InvalidOrigin(origins))
      }

    /** Return the method if invalid, `None` otherwise. */
    def validateMethod(method: HttpMethod): Option[CorsRejection.Cause] =
      if (allowedMethods.contains(method)) {
        None
      } else {
        Some(CorsRejection.InvalidMethod(method))
      }

    /** Return the list of invalid headers, or `None` if they are all valid. */
    def validateHeaders(headers: Seq[String]): Option[CorsRejection.Cause] = {
      val invalidHeaders = headers.filterNot(allowedHeaders.matches)
      if (invalidHeaders.isEmpty) {
        None
      } else {
        Some(CorsRejection.InvalidHeaders(invalidHeaders))
      }
    }

    extractRequest.flatMap { request ⇒
      import request._

      (method, header[Origin].map(_.origins.toSeq), header[`Access-Control-Request-Method`].map(_.method)) match {
        case (OPTIONS, Some(origins), Some(requestMethod)) if origins.lengthCompare(1) <= 0 ⇒
          // Case 1: pre-flight CORS request

          val headers = header[`Access-Control-Request-Headers`].map(_.headers.toSeq).getOrElse(Seq.empty)

          List(validateOrigins(origins), validateMethod(requestMethod), validateHeaders(headers))
            .collectFirst { case Some(cause) => CorsRejection(cause) }
            .fold(complete(HttpResponse(StatusCodes.OK, preflightResponseHeaders(origins, headers))))(reject(_))

        case (_, Some(origins), None) ⇒
          // Case 2: simple/actual CORS request

          val cleanAndAddHeaders: Seq[HttpHeader] => Seq[HttpHeader] = { oldHeaders =>
            actualResponseHeaders(origins) ++ oldHeaders.filterNot(h => CorsDirectives.headersToClean.exists(h.is))
          }

          validateOrigins(origins) match {
            case None ⇒
              mapResponseHeaders(cleanAndAddHeaders)
            case Some(cause) ⇒
              reject(CorsRejection(cause))
          }

        case _ if allowGenericHttpRequests ⇒
          // Case 3a: not a valid CORS request, but allowed

          pass

        case _ ⇒
          // Case 3b: not a valid CORS request, forbidden

          reject(CorsRejection(CorsRejection.Malformed))
      }
    }
  }

}

object CorsDirectives extends CorsDirectives {

  import RouteDirectives._

  private val headersToClean: List[String] = List(
    `Access-Control-Allow-Origin`,
    `Access-Control-Expose-Headers`,
    `Access-Control-Allow-Credentials`,
    `Access-Control-Allow-Methods`,
    `Access-Control-Allow-Headers`,
    `Access-Control-Max-Age`
  ).map(_.lowercaseName)

  def corsRejectionHandler = RejectionHandler.newBuilder().handle {
    case CorsRejection(cause) ⇒
      val message = cause match {
        case CorsRejection.Malformed ⇒
          "malformed request"
        case CorsRejection.InvalidOrigin(origins) ⇒
          val listOrNull = if (origins.isEmpty) "null" else origins.mkString(" ")
          s"invalid origin '$listOrNull'"
        case CorsRejection.InvalidMethod(method) ⇒
          s"invalid method '${method.value}'"
        case CorsRejection.InvalidHeaders(headers) ⇒
          s"invalid headers '${headers.mkString(" ")}'"
      }
      complete((StatusCodes.BadRequest, s"CORS: $message"))
  }.result()

}
