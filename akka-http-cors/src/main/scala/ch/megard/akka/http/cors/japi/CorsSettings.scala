package ch.megard.akka.http.cors.japi

import java.util.Optional

import akka.http.javadsl.model.HttpMethod
import akka.http.javadsl.model.headers.HttpOriginRange

/**
  * Public API but not intended for subclassing
  */
abstract class CorsSettings {

  def getAllowGenericHttpRequests: Boolean
  def getAllowCredentials: Boolean
  def getAllowedOrigins: HttpOriginRange
  def getAllowedHeaders: ch.megard.akka.http.cors.japi.HttpHeaderRange
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

  def defaultSettings: CorsSettings = ch.megard.akka.http.cors.CorsSettings.defaultSettings

}
