package ch.megard.akka.http.cors

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Rejection


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
  extends Rejection
