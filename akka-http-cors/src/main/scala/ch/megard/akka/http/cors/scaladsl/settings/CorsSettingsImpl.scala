package ch.megard.akka.http.cors.scaladsl.settings

import akka.http.scaladsl.model.{HttpHeader, HttpMethod}
import akka.http.scaladsl.model.headers._
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}

import scala.collection.immutable.Seq

/** INTERNAL API */
private[akka] final case class CorsSettingsImpl(
    allowGenericHttpRequests: Boolean,
    allowCredentials: Boolean,
    allowedOrigins: HttpOriginMatcher,
    allowedHeaders: HttpHeaderRange,
    allowedMethods: Seq[HttpMethod],
    exposedHeaders: Seq[String],
    maxAge: Option[Long]
) extends CorsSettings {

  override def productPrefix = "CorsSettings"

  private def accessControlExposeHeaders: Option[`Access-Control-Expose-Headers`] =
    if (exposedHeaders.nonEmpty)
      Some(`Access-Control-Expose-Headers`(exposedHeaders))
    else
      None

  private def accessControlAllowCredentials: Option[`Access-Control-Allow-Credentials`] =
    if (allowCredentials)
      Some(`Access-Control-Allow-Credentials`(true))
    else
      None

  private def accessControlMaxAge: Option[`Access-Control-Max-Age`] =
    maxAge.map(`Access-Control-Max-Age`.apply)

  private def accessControlAllowMethods: `Access-Control-Allow-Methods` =
    `Access-Control-Allow-Methods`(allowedMethods)

  private def accessControlAllowHeaders(requestHeaders: Seq[String]): Option[`Access-Control-Allow-Headers`] =
    allowedHeaders match {
      case HttpHeaderRange.Default(headers) => Some(`Access-Control-Allow-Headers`(headers))
      case HttpHeaderRange.* if requestHeaders.nonEmpty => Some(`Access-Control-Allow-Headers`(requestHeaders))
      case _ => None
    }

  private def accessControlAllowOrigin(origins: Seq[HttpOrigin]): `Access-Control-Allow-Origin` =
    if (allowedOrigins == HttpOriginMatcher.* && !allowCredentials)
      `Access-Control-Allow-Origin`.*
    else
      `Access-Control-Allow-Origin`.forRange(HttpOriginRange.Default(origins))

  // Cache headers that are always included in a preflight response
  private val basePreflightResponseHeaders: List[HttpHeader] =
    List(accessControlAllowMethods) ++ accessControlMaxAge ++ accessControlAllowCredentials

  // Cache headers that are always included in an actual response
  private val baseActualResponseHeaders: List[HttpHeader] =
    accessControlExposeHeaders.toList ++ accessControlAllowCredentials

  def preflightResponseHeaders(origins: Seq[HttpOrigin], requestHeaders: Seq[String]): List[HttpHeader] =
    accessControlAllowHeaders(requestHeaders) match {
      case Some(h) => h :: accessControlAllowOrigin(origins) :: basePreflightResponseHeaders
      case None => accessControlAllowOrigin(origins) :: basePreflightResponseHeaders
    }

  def actualResponseHeaders(origins: Seq[HttpOrigin]): List[HttpHeader] =
    accessControlAllowOrigin(origins) :: baseActualResponseHeaders

}
