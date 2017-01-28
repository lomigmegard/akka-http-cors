package ch.megard.akka.http.cors.javadsl.settings

import java.util.Optional

import akka.http.javadsl.model.HttpMethod
import akka.http.javadsl.model.headers.HttpOriginRange
import ch.megard.akka.http.cors.javadsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl

/**
  * Public API but not intended for subclassing
  */
abstract class CorsSettings {

  def getAllowGenericHttpRequests: Boolean
  def getAllowCredentials: Boolean
  def getAllowedOrigins: HttpOriginRange
  def getAllowedHeaders: HttpHeaderRange
  def getAllowedMethods: java.lang.Iterable[HttpMethod]
  def getExposedHeaders: java.lang.Iterable[String]
  def getMaxAge: Optional[Long]

  def withAllowGenericHttpRequests(newValue: Boolean): CorsSettings
  def withAllowCredentials(newValue: Boolean): CorsSettings
  def withAllowedOrigins(newValue: HttpOriginRange): CorsSettings
  def withAllowedHeaders(newValue: HttpHeaderRange): CorsSettings
  def withAllowedMethods(newValue: java.lang.Iterable[HttpMethod]): CorsSettings
  def withExposedHeaders(newValue: java.lang.Iterable[String]): CorsSettings
  def withMaxAge(newValue: Optional[Long]): CorsSettings

}

object CorsSettings {

  def defaultSettings: CorsSettings = scaladsl.settings.CorsSettings.defaultSettings

}
