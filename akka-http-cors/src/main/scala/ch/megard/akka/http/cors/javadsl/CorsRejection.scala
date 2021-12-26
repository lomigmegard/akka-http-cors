package ch.megard.akka.http.cors.javadsl

import akka.http.javadsl.model.HttpMethod
import akka.http.javadsl.model.headers.HttpOrigin
import akka.http.javadsl.server.CustomRejection

/** Rejection created by the CORS directives. Signal the CORS request was rejected. The reason of the rejection is
  * specified in the cause.
  */
trait CorsRejection extends CustomRejection {
  def cause: CorsRejection.Cause
}

object CorsRejection {

  /** Signals the cause of the failed CORS request.
    */
  trait Cause {

    /** Description of this Cause in a human-readable format. Can be used for debugging or custom Rejection handlers.
      */
    def description: String
  }

  /** Signals the CORS request was malformed.
    */
  trait Malformed extends Cause

  /** Signals the CORS request was rejected because its origin was invalid. An empty list means the Origin header was
    * `null`.
    */
  trait InvalidOrigin extends Cause {
    def getOrigins: java.util.List[HttpOrigin]
  }

  /** Signals the CORS request was rejected because its method was invalid.
    */
  trait InvalidMethod extends Cause {
    def getMethod: HttpMethod
  }

  /** Signals the CORS request was rejected because its headers were invalid.
    */
  trait InvalidHeaders extends Cause {
    def getHeaders: java.util.List[String]
  }
}
