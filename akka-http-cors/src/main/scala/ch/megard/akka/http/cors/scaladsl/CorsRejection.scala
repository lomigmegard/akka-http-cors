package ch.megard.akka.http.cors.scaladsl

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.server.Rejection
import ch.megard.akka.http.cors.javadsl

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

/**
  * Rejection created by the CORS directives.
  * Signal the CORS request was rejected. The reason of the rejection is specified in the cause.
  */
final case class CorsRejection(cause: CorsRejection.Cause) extends javadsl.CorsRejection with Rejection

object CorsRejection {

  /**
    * Signals the cause of the failed CORS request.
    */
  sealed trait Cause extends javadsl.CorsRejection.Cause

  /**
    * Signals the CORS request was rejected because its origin was invalid.
    * An empty list mean the Origin header was `null`.
    */
  case object Malformed extends javadsl.CorsRejection.Malformed with Cause

  /**
    * Signals the CORS request was rejected because its origin was invalid.
    * An empty list means the Origin header was `null`.
    */
  final case class InvalidOrigin(origins: Seq[HttpOrigin]) extends javadsl.CorsRejection.InvalidOrigin with Cause {
    override def getOrigins = (origins: Seq[akka.http.javadsl.model.headers.HttpOrigin]).asJava
  }

  /**
    * Signals the CORS request was rejected because its method was invalid.
    */
  final case class InvalidMethod(method: HttpMethod) extends javadsl.CorsRejection.InvalidMethod with Cause {
    override def getMethod = method
  }

  /**
    * Signals the CORS request was rejected because its headers were invalid.
    */
  final case class InvalidHeaders(headers: Seq[String]) extends javadsl.CorsRejection.InvalidHeaders with Cause {
    override def getHeaders = headers.asJava
  }

}
