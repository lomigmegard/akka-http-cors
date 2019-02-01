package ch.megard.akka.http.cors.javadsl.settings

import java.util.Optional

import akka.actor.ActorSystem
import akka.annotation.DoNotInherit
import akka.http.javadsl.model.HttpMethod
import ch.megard.akka.http.cors.javadsl.model.{HttpHeaderRange, HttpOriginMatcher}
import ch.megard.akka.http.cors.scaladsl
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettingsImpl
import com.typesafe.config.Config

/**
  * Public API but not intended for subclassing
  */
@DoNotInherit
abstract class CorsSettings { self: CorsSettingsImpl ⇒

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

  def create(config: Config): CorsSettings = scaladsl.settings.CorsSettings(config)
  def create(configOverrides: String): CorsSettings = scaladsl.settings.CorsSettings(configOverrides)
  def create(system: ActorSystem): CorsSettings = create(system.settings.config)

  def defaultSettings: CorsSettings = scaladsl.settings.CorsSettings.defaultSettings

}
