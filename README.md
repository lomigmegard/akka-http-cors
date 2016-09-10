# akka-http-cors

[![Build Status](https://travis-ci.org/lomigmegard/akka-http-cors.svg?branch=master&style=flat)](https://travis-ci.org/lomigmegard/akka-http-cors)
[![Software License](https://img.shields.io/badge/license-Apache 2-brightgreen.svg?style=flat)](LICENSE)

CORS (Cross Origin Resource Sharing) is a mechanism to enable cross origin requests.

This is a Scala implementation for the server-side targeting the akka-http 2.x library. Main features:
- [x] Works without any additional configuration. Sensible defaults are provided.
- [x] Respects the full standard defined by the W3C, even the border cases.
- [ ] Tests, lots of tests.

## Getting Akka Http Cors
akka-http-cors is deployed to Maven Central. Add it to your `build.sbt` or `Build.scala`:
```scala
libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.1.6"
```

## Quick Start
The simplest way to enable CORS in your application is to use the `cors` directive.
Settings are passed as a parameter to the directive, with defaults provided for convenience.
```scala
val route: Route = cors() {
  complete(...)
}
```

The default settings can be used as a baseline to customize the CORS directive behaviour:
```scala
val settings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = false)
val strictRoute: Route = cors(settings) {
  complete(...)
}
```

A second directive, `corsDecorate`, implements the same behaviour as the first one but additionally provides information about the current request to the inner route.
```scala
val route: Route = corsDecorate() {
  case CorsRequest(origins) ⇒ complete("actual")
  case NotCorsRequest       ⇒ complete("not cors")
}
```

## Rejection
The CORS directives can reject requests using the `CorsRejection` class. Requests can be either malformed or not allowed to access the resource.

A rejection handler is provided by the library to return meaningful HTTP responses. Read the [akka documentation](http://doc.akka.io/docs/akka/2.4/scala/http/routing-dsl/rejections.html) to learn more about rejections, or if you need to write your own handler.
```scala
import akka.http.scaladsl.server.directives.ExecutionDirectives._
import ch.megard.akka.http.cors.CorsDirectives._

val route: Route = rejectionHandler(corsRejectionHandler) {
  cors() {
    complete(...)
  }
}
```

## Configuration

#### allowGenericHttpRequests
`Boolean` with default value `true`.

If `true`, allow generic requests (that are outside the scope of the specification) to pass through the directive. Else, strict CORS filtering is applied and any invalid request will be rejected.

#### allowCredentials
`Boolean` with default value `true`.

Indicates whether the resource supports user credentials.  If `true`, the header `Access-Control-Allow-Credentials` is set in the response, indicating the actual request can include user credentials.

Examples of user credentials are: cookies, HTTP authentication or client-side certificates.

#### allowedOrigins
`HttpOriginRange` with default value `HttpOriginRange.*`.

List of origins that the CORS filter must allow. Can also be set to `*` to allow access to the resource from any origin. Controls the content of the `Access-Control-Allow-Origin` response header:
* if parameter is `*` **and** credentials are not allowed, a `*` is set in `Access-Control-Allow-Origin`.
* otherwise, the origins given in the `Origin` request header are echoed.

The actual or preflight request is rejected if any of the origins from the request is not allowed.

#### allowedHeaders
`HttpHeaderRange` with default value `HttpHeaderRange.*`.

 List of request headers that can be used when making an actual request. Controls the content of the `Access-Control-Allow-Headers` header in a preflight response:
 * if parameter is `*`, the headers from `Access-Control-Request-Headers` are echoed.
 * otherwise the parameter list is returned as part of the header.

#### allowedMethods
`Seq[HttpMethod]` with default value `Seq(GET, POST, HEAD, OPTIONS)`.

List of methods that can be used when making an actual request. The list is returned as part of the `Access-Control-Allow-Methods` preflight response header.

The preflight request will be rejected if the `Access-Control-Request-Method` header's method is not part of the list.

#### exposedHeaders
`Seq[String]` with default value `Seq.empty`.

List of headers (other than [simple response headers](https://www.w3.org/TR/cors/#simple-response-header)) that browsers are allowed to access. If not empty, this list is returned as part of the `Access-Control-Expose-Headers` header in the actual response.

#### maxAge
`Option[Long]` (in seconds) with default value `Some (30 * 60)`.

When set, the amount of seconds the browser is allowed to cache the results of a preflight request. This value is returned as part of the `Access-Control-Max-Age` preflight response header. If `None`, the header is not added to the preflight response.

## Benchmarks
Using the [sbt-jmh](https://github.com/ktoso/sbt-jmh) plugin, preliminary benchmarks have been performed to measure the impact of the `cors` directive on the performance. The first results are shown below.

#### v0.1.2 (Akka 2.4.4)
```
> jmh:run -i 40 -wi 30 -f2 -t1
Benchmark                         Mode  Cnt     Score     Error  Units
CorsBenchmark.baseline           thrpt   80  3601.121 ± 102.274  ops/s
CorsBenchmark.default_cors       thrpt   80  3582.090 ±  95.304  ops/s
CorsBenchmark.default_preflight  thrpt   80  3482.716 ±  89.124  ops/s
```

#### v0.1.3 (Akka 2.4.7)
```
> jmh:run -i 40 -wi 30 -f2 -t1
Benchmark                         Mode  Cnt     Score     Error  Units
CorsBenchmark.baseline           thrpt   80  3657.762 ± 141.409  ops/s
CorsBenchmark.default_cors       thrpt   80  3687.351 ±  35.176  ops/s
CorsBenchmark.default_preflight  thrpt   80  3645.629 ±  30.411  ops/s
```

## References
- [W3C Specification: CORS](https://www.w3.org/TR/cors/)
- [RFC-6454: The Web Origin Concept](https://tools.ietf.org/html/rfc6454)

## License
This code is open source software licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
