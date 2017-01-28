package ch.megard.akka.http.cors.scaladsl

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Rejection

import ch.megard.akka.http.cors.javadsl

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters

import scala.collection.immutable.Seq

/**
  * Rejection created when a CORS request was invalid.
  * In case of a preflight request, one to three of the causes can be marked as invalid.
  * In case of an actual request, only the origin can be marked as invalid.
  *
  * Note: when the three causes are `None`, the request itself was invalid. For example
  * the `Origin` header can be missing.
  */
case class CorsRejection(origin: Option[HttpOrigin], method: Option[HttpMethod], headers: Option[Seq[String]])
  extends javadsl.CorsRejection with Rejection {
  override def getOrigin = OptionConverters.toJava(origin)
  override def getMethod = OptionConverters.toJava(method)
  override def getHeaders = OptionConverters.toJava(headers.map(_.asJava))
}
