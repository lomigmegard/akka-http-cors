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
    * If `true`, allow generic requests (that are outside the scope of the specification)
    * to pass through the directive. Else, strict CORS filtering is applied and any
    * invalid request will be rejected.
    *
    * Default: `true`
    */
  def allowGenericHttpRequests: Boolean

  /**
    * Indicates whether the resource supports user credentials.  If `true`, the header
    * `Access-Control-Allow-Credentials` is set in the response, indicating that the
    * actual request can include user credentials. Examples of user credentials are:
    * cookies, HTTP authentication or client-side certificates.
    *
    * Default: `true`
    *
    * @see <a href="https://www.w3.org/TR/cors/#access-control-allow-credentials-response-header">Access-Control-Allow-Credentials</a>
    */
  def allowCredentials: Boolean

  /**
    * List of origins that the CORS filter must allow. Can also be set to `*` to allow
    * access to the resource from any origin. Controls the content of the
    * `Access-Control-Allow-Origin` response header: if parameter is `*` and credentials
    * are not allowed, a `*` is set in `Access-Control-Allow-Origin`. Otherwise, the
    * origins given in the `Origin` request header are echoed.
    *
    * The actual or preflight request is rejected if any of the origins from the request
    * is not allowed.
    *
    * Default: `HttpOriginRange.*`
    *
    * @see <a href="https://www.w3.org/TR/cors/#access-control-allow-origin-response-header">Access-Control-Allow-Origin</a>
    */
  def allowedOrigins: HttpOriginRange

  /**
    * List of request headers that can be used when making an actual request. Controls
    * the content of the `Access-Control-Allow-Headers` header in a preflight response:
    * if parameter is `*`, the headers from `Access-Control-Request-Headers` are echoed.
    * Otherwise the parameter list is returned as part of the header.
    *
    * Default: `HttpHeaderRange.*`
    *
    * @see <a href="https://www.w3.org/TR/cors/#access-control-allow-headers-response-header">Access-Control-Allow-Headers</a>
    */
  def allowedHeaders: HttpHeaderRange

  /**
    * List of methods that can be used when making an actual request. The list is
    * returned as part of the `Access-Control-Allow-Methods` preflight response header.
    *
    * The preflight request will be rejected if the `Access-Control-Request-Method`
    * header's method is not part of the list.
    *
    * Default: `Seq(GET, POST, HEAD, OPTIONS)`
    *
    * @see <a href="https://www.w3.org/TR/cors/#access-control-allow-methods-response-header">Access-Control-Allow-Methods</a>
    */
  def allowedMethods: Seq[HttpMethod]

  /**
    * List of headers (other than simple response headers) that browsers are allowed to access.
    * If not empty, this list is returned as part of the `Access-Control-Expose-Headers`
    * header in the actual response.
    *
    * Default: `Seq.empty`
    *
    * @see <a href="https://www.w3.org/TR/cors/#simple-response-header">Simple response headers</a>.
    * @see <a href="https://www.w3.org/TR/cors/#access-control-expose-headers-response-header">Access-Control-Expose-Headers</a>
    */
  def exposedHeaders: Seq[String]

  /**
    * When set, the amount of seconds the browser is allowed to cache the results of a preflight request.
    * This value is returned as part of the `Access-Control-Max-Age` preflight response header.
    * If `None`, the header is not added to the preflight response.
    *
    * Default: `Some(30 * 60)`
    *
    * @see <a href="https://www.w3.org/TR/cors/#access-control-max-age-response-header">Access-Control-Max-Age</a>
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
