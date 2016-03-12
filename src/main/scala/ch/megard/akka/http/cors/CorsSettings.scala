package ch.megard.akka.http.cors

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.HttpOriginRange

import scala.collection.immutable.Seq

/**
  * Settings used by the CORS directives.
  */
abstract class CorsSettings {

  /**
    * If `true`, allow generic requests (that are outisde the scope of the specification)
    * to pass through the directive. Else, strict CORS filtering is applied and any
    * invalid request will be rejected.
    *
    * Default: `true`
    */
  def allowGenericHttpRequests: Boolean
  def allowCredentials: Boolean
  def allowedOrigins: HttpOriginRange
  def allowedHeaders: HttpHeaderRange
  def allowedMethods: Seq[HttpMethod]
  def exposedHeaders: Seq[String]

  /**
    * When set, the amount of seconds the browser is allowed to cache the results of a preflight request.
    * This value is returned as part of the `Access-Control-Max-Age` preflight response header.
    * If `None`, the header is not added to the preflight response.
    *
    * Default: `Some(30 * 60)`
    *
    * @see https://www.w3.org/TR/cors/#access-control-max-age-response-header
    */
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
