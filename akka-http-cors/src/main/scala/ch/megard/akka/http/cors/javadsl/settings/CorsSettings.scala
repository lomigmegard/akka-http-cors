package ch.megard.akka.http.cors.javadsl.settings

import java.util.Optional

import akka.actor.ActorSystem
import akka.annotation.DoNotInherit
import akka.http.javadsl.model.HttpMethod
import ch.megard.akka.http.cors.javadsl.model.{HttpHeaderRange, HttpOriginMatcher}
import ch.megard.akka.http.cors.scaladsl
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettingsImpl
import com.typesafe.config.Config

/** Public API but not intended for subclassing
  */
@DoNotInherit
abstract class CorsSettings { self: CorsSettingsImpl =>

  def getAllowGenericHttpRequests: Boolean
  def getAllowCredentials: Boolean
  def getAllowedOrigins: HttpOriginMatcher
  def getAllowedHeaders: HttpHeaderRange
  def getAllowedMethods: java.lang.Iterable[HttpMethod]
  def getExposedHeaders: java.lang.Iterable[String]
  def getMaxAge: Optional[Long]

  def withAllowGenericHttpRequests(newValue: Boolean): CorsSettings
  def withAllowCredentials(newValue: Boolean): CorsSettings
  def withAllowedOrigins(newValue: HttpOriginMatcher): CorsSettings
  def withAllowedHeaders(newValue: HttpHeaderRange): CorsSettings
  def withAllowedMethods(newValue: java.lang.Iterable[HttpMethod]): CorsSettings
  def withExposedHeaders(newValue: java.lang.Iterable[String]): CorsSettings
  def withMaxAge(newValue: Optional[Long]): CorsSettings
}

object CorsSettings {

  /** Creates an instance of settings using the given Config.
    */
  def create(config: Config): CorsSettings = scaladsl.settings.CorsSettings(config)

  /** Creates an instance of settings using the given String of config overrides to override
    * settings set in the class loader of this class (i.e. by application.conf or reference.conf files in
    * the class loader of this class).
    */
  def create(configOverrides: String): CorsSettings = scaladsl.settings.CorsSettings(configOverrides)

  /** Creates an instance of CorsSettings using the configuration provided by the given ActorSystem.
    */
  def create(system: ActorSystem): CorsSettings = scaladsl.settings.CorsSettings(system)

  /** Settings from the default loaded configuration.
    * Note that application code may want to use the `apply()` methods instead
    * to have more control over the source of the configuration.
    */
  def defaultSettings: CorsSettings = scaladsl.settings.CorsSettings.defaultSettings
}
