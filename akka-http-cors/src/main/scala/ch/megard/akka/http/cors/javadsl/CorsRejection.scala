package ch.megard.akka.http.cors.javadsl

import java.util.Optional

import akka.http.javadsl.model.HttpMethod
import akka.http.javadsl.model.headers.HttpOrigin
import akka.http.javadsl.server.CustomRejection

/**
  * Rejection created by the CORS directives.
  * In case of a preflight request, one to three of the causes can be marked as invalid.
  * In case of an actual request, only the origin can be marked as invalid.
  *
  * Note: when the three causes are `empty`, the request itself was invalid. For example
  * the `Origin` header can be missing.
  */
trait CorsRejection extends CustomRejection {
  def getOrigin: Optional[HttpOrigin]
  def getMethod: Optional[HttpMethod]
  def getHeaders: Optional[java.util.List[String]]
}
