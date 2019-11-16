package ch.megard.akka.http.cors.scaladsl.model

import akka.http.javadsl.{model => jm}
import akka.http.scaladsl.model.headers.HttpOrigin
import ch.megard.akka.http.cors.javadsl

import scala.collection.immutable.Seq

/**
  * [[HttpOrigin]] matcher.
  */
abstract class HttpOriginMatcher extends javadsl.model.HttpOriginMatcher {
  def matches(origin: HttpOrigin): Boolean

  /** Java API */
  def matches(origin: jm.headers.HttpOrigin): Boolean = matches(origin.asInstanceOf[HttpOrigin])
}

object HttpOriginMatcher {
  case object `*` extends HttpOriginMatcher {
    def matches(origin: HttpOrigin) = true
  }

  final case class Default(origins: Seq[HttpOrigin]) extends HttpOriginMatcher {
    private class StrictHostMatcher(origin: HttpOrigin) extends (HttpOrigin => Boolean) {
      override def apply(origin: HttpOrigin): Boolean = origin == this.origin
    }

    private class WildcardHostMatcher(wildcardOrigin: HttpOrigin) extends (HttpOrigin => Boolean) {
      private val suffix: String = wildcardOrigin.host.host.address.stripPrefix("*")
      override def apply(origin: HttpOrigin): Boolean = {
        origin.scheme == wildcardOrigin.scheme &&
        origin.host.port == wildcardOrigin.host.port &&
        origin.host.host.address.endsWith(suffix)
      }
    }

    private val matchers: Seq[HttpOrigin => Boolean] = {
      origins.map { origin =>
        if (hasWildcard(origin)) new WildcardHostMatcher(origin) else new StrictHostMatcher(origin)
      }
    }

    override def matches(origin: HttpOrigin): Boolean = matchers.exists(_.apply(origin))
    override def toString: String                     = origins.mkString(" ")
  }

  final case class Strict(origins: Seq[HttpOrigin]) extends HttpOriginMatcher {
    override def matches(origin: HttpOrigin): Boolean = origins contains origin
    override def toString: String                     = origins.mkString(" ")
  }

  private def hasWildcard(origin: HttpOrigin): Boolean =
    origin.host.host.isNamedHost && origin.host.host.address.startsWith("*.")

  /**
    * Build a matcher that will accept any of the given origins.
    * Wildcard in the hostname will not be interpreted.
    */
  def strict(origins: HttpOrigin*): HttpOriginMatcher =
    Strict(origins.toList)

  /**
    * Build a matcher that will accept any of the given origins.
    * Hostname starting with `*.` will match any sub-domain.
    * The scheme and the port are always strictly matched.
    */
  def apply(origins: HttpOrigin*): HttpOriginMatcher = {
    if (origins.exists(hasWildcard))
      Default(origins.toList)
    else
      Strict(origins.toList)
  }
}
