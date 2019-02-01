package ch.megard.akka.http.cors.scaladsl.settings

import java.util.Optional
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.annotation.DoNotInherit
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, HttpMethods}
import ch.megard.akka.http.cors.javadsl
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}
import com.typesafe.config.ConfigException.{Missing, WrongType}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq
import scala.compat.java8.OptionConverters
import scala.util.Try

/**
  * Settings used by the CORS directives.
  *
  * Public API but not intended for subclassing.
  */
@DoNotInherit
abstract class CorsSettings private[akka] () extends javadsl.settings.CorsSettings { self: CorsSettingsImpl ⇒

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
    * @see [[https://www.w3.org/TR/cors/#access-control-allow-credentials-response-header Access-Control-Allow-Credentials]]
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
    * @see [[https://www.w3.org/TR/cors/#access-control-allow-origin-response-header Access-Control-Allow-Origin]]
    */
  def allowedOrigins: HttpOriginMatcher

  /**
    * List of request headers that can be used when making an actual request. Controls
    * the content of the `Access-Control-Allow-Headers` header in a preflight response:
    * if parameter is `*`, the headers from `Access-Control-Request-Headers` are echoed.
    * Otherwise the parameter list is returned as part of the header.
    *
    * Default: `HttpHeaderRange.*`
    *
    * @see [[https://www.w3.org/TR/cors/#access-control-allow-headers-response-header Access-Control-Allow-Headers]]
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
    * @see [[https://www.w3.org/TR/cors/#access-control-allow-methods-response-header Access-Control-Allow-Methods]]
    */
  def allowedMethods: Seq[HttpMethod]

  /**
    * List of headers (other than simple response headers) that browsers are allowed to access.
    * If not empty, this list is returned as part of the `Access-Control-Expose-Headers`
    * header in the actual response.
    *
    * Default: `Seq.empty`
    *
    * @see [[https://www.w3.org/TR/cors/#simple-response-header Simple response headers]]
    * @see [[https://www.w3.org/TR/cors/#access-control-expose-headers-response-header Access-Control-Expose-Headers]]
    */
  def exposedHeaders: Seq[String]

  /**
    * When set, the amount of seconds the browser is allowed to cache the results of a preflight request.
    * This value is returned as part of the `Access-Control-Max-Age` preflight response header.
    * If `None`, the header is not added to the preflight response.
    *
    * Default: `Some(30 * 60)`
    *
    * @see [[https://www.w3.org/TR/cors/#access-control-max-age-response-header Access-Control-Max-Age]]
    */
  def maxAge: Option[Long]


  /* Java APIs */

  override def getAllowGenericHttpRequests = allowGenericHttpRequests
  override def getAllowCredentials = allowCredentials
  override def getAllowedOrigins = allowedOrigins
  override def getAllowedHeaders = allowedHeaders
  override def getAllowedMethods = (allowedMethods: Seq[akka.http.javadsl.model.HttpMethod]).asJava
  override def getExposedHeaders = exposedHeaders.asJava
  override def getMaxAge = OptionConverters.toJava(maxAge)

  // Currently the easiest way to go from Java models to their Scala equivalent is to cast.
  // See https://github.com/akka/akka-http/issues/661 for a potential opening of the JavaMapping API.
  override def withAllowGenericHttpRequests(newValue: Boolean): CorsSettings = {
    copy(allowGenericHttpRequests = newValue)
  }
  override def withAllowCredentials(newValue: Boolean): CorsSettings = {
    copy(allowCredentials = newValue)
  }
  override def withAllowedOrigins(newValue: javadsl.model.HttpOriginMatcher): CorsSettings = {
    copy(allowedOrigins = newValue.asInstanceOf[HttpOriginMatcher])
  }
  override def withAllowedHeaders(newValue: javadsl.model.HttpHeaderRange): CorsSettings = {
    copy(allowedHeaders = newValue.asInstanceOf[HttpHeaderRange])
  }
  override def withAllowedMethods(newValue: java.lang.Iterable[akka.http.javadsl.model.HttpMethod]): CorsSettings = {
    copy(allowedMethods = newValue.asScala.toList.asInstanceOf[List[HttpMethod]])
  }
  override def withExposedHeaders(newValue: java.lang.Iterable[String]): CorsSettings = {
    copy(exposedHeaders = newValue.asScala.toList)
  }
  override def withMaxAge(newValue: Optional[Long]): CorsSettings = {
    copy(maxAge = OptionConverters.toScala(newValue))
  }

  // overloads for Scala idiomatic use
  def withAllowedOrigins(newValue: HttpOriginMatcher): CorsSettings = copy(allowedOrigins = newValue)
  def withAllowedHeaders(newValue: HttpHeaderRange): CorsSettings = copy(allowedHeaders = newValue)
  def withAllowedMethods(newValue: Seq[HttpMethod]): CorsSettings = copy(allowedMethods = newValue)
  def withExposedHeaders(newValue: Seq[String]): CorsSettings = copy(exposedHeaders = newValue)
  def withMaxAge(newValue: Option[Long]): CorsSettings = copy(maxAge = newValue)

  private[akka] def preflightResponseHeaders(origins: Seq[HttpOrigin], requestHeaders: Seq[String]): List[HttpHeader]
  private[akka] def actualResponseHeaders(origins: Seq[HttpOrigin]): List[HttpHeader]

}

object CorsSettings {

  private val prefix = "akka-http-cors"

  def apply(system: ActorSystem): CorsSettings = apply(system.settings.config)
  def apply(config: Config): CorsSettings = fromSubConfig(config.getConfig(prefix))
  def apply(configOverrides: String): CorsSettings = apply(
    ConfigFactory.parseString(configOverrides).withFallback(ConfigFactory.defaultReference(getClass.getClassLoader))
  )

  def fromSubConfig(config: Config): CorsSettings = {

    def parseStringList(path: String): List[String] =
      Try(config.getStringList(path).asScala.toList)
        .recover {
          case _: WrongType => config.getString(path).split(" ").toList
        }
        .get

    def parseSeconds(path: String): Option[Long] =
      Try(Some(config.getLong(path)))
        .recover {
          case _: WrongType => Some(config.getDuration(path, TimeUnit.SECONDS))
          case _: Missing => None
        }
        .get


    CorsSettingsImpl(
      allowGenericHttpRequests = config.getBoolean("allow-generic-http-requests"),
      allowCredentials = config.getBoolean("allow-credentials"),
      allowedOrigins = parseStringList("allowed-origins") match {
        case List("*") => HttpOriginMatcher.*
        case origins => HttpOriginMatcher(origins.map(HttpOrigin(_)): _*)
      },
      allowedHeaders = parseStringList("allowed-headers") match {
        case List("*") => HttpHeaderRange.*
        case headers => HttpHeaderRange(headers: _*)
      },
      allowedMethods = parseStringList("allowed-methods").map(method =>
        HttpMethods.getForKey(method).getOrElse(HttpMethod.custom(method))
      ),
      exposedHeaders = parseStringList("exposed-headers"),
      maxAge = parseSeconds("max-age")
    )
  }

  val defaultSettings = apply(ConfigFactory.load(getClass.getClassLoader))

}
