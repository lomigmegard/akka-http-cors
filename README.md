# akka-http-cors

## WIP
This project is under development. 

## Introduction
CORS (Cross Origin Resource Sharing) is a mechanism to enable cross origin requests.

This is a Scala implementation for the server-side targeting the akka-http 2.x library. Main features:
- Works without any additional configuration. Sensible defaults are provided.
- Respects the full standard defined by the W3C, even the border cases.
- Tests, lots of tests (well, this is the future).

## Quick Start
This project is not deployed to maven. If you want to try it, just copy the unique source file inside your project. 

The simplest way to enable CORS in your application is to use the `cors` directive. 
Settings are passed as a parameter to the directive, with defaults provided for convenience.
```scala
  val route: Route = cors() {
    complete("response with CORS enabled")
  }
```

## Configuration
TODO

## References
- https://www.w3.org/TR/cors/
- https://tools.ietf.org/html/rfc6454
