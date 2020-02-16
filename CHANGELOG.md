# Changelog

## [unreleased]

  - Drop support for Scala 2.11.
  - Update akka to 2.6.3 (#73).
  - Update akka-http to 10.1.11.

## 0.4.2 (2019-11-17)

  - Rejection handler handles all CorsRejection (#53).
  - Update Scala to 2.12.10 and 2.13.1.
  - Update akka-http to 10.1.10.
  - Use Scalafmt for formatting.

## 0.4.1 (2019-06-12)

  - Cross compile with Scala 2.13.0.
  - Update akka-http to 10.1.8. 

## 0.4.0 (2019-03-09)

  - Support subdomain wildcard matcher in allowed origins (#25).
  - Remove directive `corsDecorate()` (#38).
  
### Migrate from 0.3 to 0.4

  - Use `HttpOriginMatcher` instead of `HttpOriginRange` in the settings.

## 0.3.4 (2019-01-17)

  - Cross compile with Scala 2.13.0-M5 (#40).
  - Update akka-http to 10.1.7.

## 0.3.3 (2018-12-17)

  - Fix: Java 1.8 support broken in 0.3.2 (#44).

## 0.3.2 (2018-12-16)

  - Support `Origin: null` in preflight requests (#43).
  - Update akka-http to 10.1.5.
  - Update Scala to 2.12.8

## 0.3.1 (2018-09-29)

  - Java 9: add `Automatic-Module-Name: ch.megard.akka.http.cors` in the `MANIFEST.MF` (#35).
  - Deprecate method `corsDecorate()` (#38).
  - Cache response headers (#39).
  - Update akka-http to 10.1.3.
  - Update Scala to 2.12.7.

## 0.3.0 (2018-03-24)

This release breaks source compatibility, planning for the 1.0 release.

  - Settings are read from a configuration file (#13).
  - Directives now clean existing CORS-related headers when responding to an actual request (#28).
  - Support `Origin: null` in simple/actual requests (#31).
  - The `CorsRejection` class has been refactored to be cleaner.
  - Update akka-http to 10.1.0, removal of Akka 2.4 support (#34).
  - Update Scala to 2.12.5 and 2.11.12.
  - Add cross-compilation with Scala 2.13.0-M3.

### Migrate from 0.2 to 0.3

  - Now that it is possible, prefer overriding settings in a `application.conf` file.
  - To update programmatically the settings, use the new `with...()` methods instead of the `copy()` method. 
  - Custom rejection handlers must be updated to reflect the new `CorsRejection` class.

## 0.2.2 (2017-09-25)

  - Update Scala to 2.12.3 and 2.11.11.
  - Update akka-http to 10.0.10.
  - Update sbt to 1.0.x major release.

## 0.2.1 (2017-04-03)

  - Add Java API (#8)
  - Update akka-http to 10.0.5.
  
### Migrate from 0.1 to 0.2
The API remains the same, but classes have moved in new packages to accommodate the Java API.

  - Directives are now in `ch.megard.akka.http.cors.scaladsl`;
  - Models are now in `ch.megard.akka.http.cors.scaladsl.model`;
  - Settings are now in `ch.megard.akka.http.cors.scaladsl.settings`.

## 0.1.11 (2017-01-31)

  - Update Scala to 2.12.1.
  - Update akka-http to 10.0.3.

## 0.1.10 (2016-11-23)

  - Update akka-http to 10.0.0.

## 0.1.9 (2016-11-10)

  - Cross compile with Scala 2.12.0.
  - Update akka-http to 10.0.0-RC2.

## 0.1.8 (2016-10-30)

  - Cross compile with Scala 2.12.0-RC2.

## 0.1.7 (2016-10-02)

  - Cross compile with Scala 2.12.0-RC1.
  - Update Akka to 2.4.11.

## 0.1.6 (2016-09-10)

  - Update Akka to 2.4.10.

## 0.1.5 (2016-08-24)

  - Update Akka to 2.4.9.
  - Update sbt to 0.13.12.

## 0.1.4 (2016-07-08)

  - Update Akka to 2.4.8.

## 0.1.3 (2016-07-08)

  - Update Akka to 2.4.7.

## 0.1.2 (2016-05-11)

  - Update Akka to 2.4.4.
  - Add benchmarks with results in README.

## 0.1.1 (2016-04-07)

  - Update Akka to 2.4.3.

## 0.1.0 (2016-03-20)

Initial release.
