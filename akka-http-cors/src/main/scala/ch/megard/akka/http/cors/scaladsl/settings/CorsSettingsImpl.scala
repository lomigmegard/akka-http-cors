package ch.megard.akka.http.cors.scaladsl.settings

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.headers.HttpOriginRange
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange

import scala.collection.immutable.Seq

/** INTERNAL API */
private[akka] final case class CorsSettingsImpl(
    allowGenericHttpRequests: Boolean,
    allowCredentials: Boolean,
    allowedOrigins: HttpOriginRange,
    allowedHeaders: HttpHeaderRange,
    allowedMethods: Seq[HttpMethod],
    exposedHeaders: Seq[String],
    maxAge: Option[Long]
) extends CorsSettings {

  override def productPrefix = "CorsSettings"

}
