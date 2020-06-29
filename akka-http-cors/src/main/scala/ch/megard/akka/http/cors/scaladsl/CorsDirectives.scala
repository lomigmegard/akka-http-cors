package ch.megard.akka.http.cors.scaladsl

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpMethod, HttpResponse, StatusCodes}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives._
import ch.megard.akka.http.cors.javadsl
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.collection.immutable.Seq

/**
  * Provides directives that implement the CORS mechanism, enabling cross origin requests.
  *
  * @see [[https://www.w3.org/TR/cors/ CORS W3C Recommendation]]
  * @see [[https://www.ietf.org/rfc/rfc6454.txt RFC 6454]]
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
    * The settings are loaded from the Actor System configuration.
    */
  def cors(): Directive0 = {
    extractActorSystem.flatMap { system =>
      cors(CorsSettings(system))
    }
  }

  /**
    * Wraps its inner route with support for the CORS mechanism, enabling cross origin requests.
    *
    * In particular the recommendation written by the W3C in https://www.w3.org/TR/cors/ is
    * implemented by this directive.
    *
    * @param settings the settings used by the CORS filter
    */
  def cors(settings: CorsSettings): Directive0 = {
    import settings._

    /** Return the invalid origins, or `Nil` if one is valid. */
    def validateOrigins(origins: Seq[HttpOrigin]): List[CorsRejection.Cause] =
      if (allowedOrigins == HttpOriginMatcher.* || origins.exists(allowedOrigins.matches)) {
        Nil
      } else {
        CorsRejection.InvalidOrigin(origins) :: Nil
      }

    /** Return the method if invalid, `Nil` otherwise. */
    def validateMethod(method: HttpMethod): List[CorsRejection.Cause] =
      if (allowedMethods.contains(method)) {
        Nil
      } else {
        CorsRejection.InvalidMethod(method) :: Nil
      }

    /** Return the list of invalid headers, or `Nil` if they are all valid. */
    def validateHeaders(headers: Seq[String]): List[CorsRejection.Cause] = {
      val invalidHeaders = headers.filterNot(allowedHeaders.matches)
      if (invalidHeaders.isEmpty) {
        Nil
      } else {
        CorsRejection.InvalidHeaders(invalidHeaders) :: Nil
      }
    }

    extractRequest.flatMap { request =>
      import request._

      (method, header[Origin].map(_.origins.toSeq), header[`Access-Control-Request-Method`].map(_.method)) match {
        case (OPTIONS, Some(origins), Some(requestMethod)) if origins.lengthCompare(1) <= 0 =>
          // Case 1: pre-flight CORS request

          val headers = header[`Access-Control-Request-Headers`].map(_.headers.toSeq).getOrElse(Seq.empty)

          validateOrigins(origins) ::: validateMethod(requestMethod) ::: validateHeaders(headers) match {
            case Nil    => complete(HttpResponse(StatusCodes.OK, preflightResponseHeaders(origins, headers)))
            case causes => reject(causes.map(CorsRejection(_)): _*)
          }

        case (_, Some(origins), None) =>
          // Case 2: simple/actual CORS request

          validateOrigins(origins) match {
            case Nil =>
              mapResponseHeaders { oldHeaders =>
                actualResponseHeaders(origins) ++ oldHeaders.filterNot(h => CorsDirectives.headersToClean.exists(h.is))
              }
            case causes =>
              reject(causes.map(CorsRejection(_)): _*)
          }

        case _ if allowGenericHttpRequests =>
          // Case 3a: not a valid CORS request, but allowed

          pass

        case _ =>
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

  def corsRejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleAll[javadsl.CorsRejection] { rejections =>
        val causes = rejections.map(_.cause.description).mkString(", ")
        complete((StatusCodes.BadRequest, s"CORS: $causes"))
      }
      .result()
}
