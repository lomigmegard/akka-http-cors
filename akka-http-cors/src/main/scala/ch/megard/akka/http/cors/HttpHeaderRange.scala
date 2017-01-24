package ch.megard.akka.http.cors

import java.util.Locale

import scala.collection.immutable.Seq


abstract class HttpHeaderRange extends ch.megard.akka.http.cors.japi.HttpHeaderRange

object HttpHeaderRange {
  case object `*` extends HttpHeaderRange {
    def matches(header: String) = true
  }

  final case class Default(headers: Seq[String]) extends HttpHeaderRange {
    val lowercaseHeaders = headers.map(_.toLowerCase(Locale.ROOT))
    def matches(header: String): Boolean = lowercaseHeaders contains header.toLowerCase(Locale.ROOT)
  }

  def apply(headers: String*): Default = Default(Seq(headers: _*))
}
