lazy val commonSettings = Seq(
  organization       := "ch.megard",
  organizationName   := "Lomig Mégard",
  startYear          := Some(2016),
  version            := "1.3.0-SNAPSHOT",
  scalaVersion       := "2.13.18",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.21", "3.3.7"),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-deprecation"
  ),
  javacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-source",
    "8",
    "-target",
    "8"
  ),
  homepage := Some(url("https://github.com/lomigmegard/akka-http-cors")),
  licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo  := Some(
    ScmInfo(
      url("https://github.com/lomigmegard/akka-http-cors"),
      "scm:git@github.com:lomigmegard/akka-http-cors.git"
    )
  ),
  developers := List(
    Developer(id = "lomigmegard", name = "Lomig Mégard", email = "", url = url("https://lomig.ch"))
  )
)

lazy val publishSettings = Seq(
  publishMavenStyle      := true,
  Test / publishArtifact := false,
  pomIncludeRepository   := { _ => false },
  publishTo              := sonatypePublishToBundle.value
)

lazy val dontPublishSettings = Seq(
  publish / skip := true
)

lazy val root = (project in file("."))
  .aggregate(`akka-http-cors`, `akka-http-cors-example`, `akka-http-cors-bench-jmh`)
  .settings(commonSettings)
  .settings(dontPublishSettings)

// Akka 2.7.0 and Akka HTTP 10.4.0 were released in October 2022 under BSL and converted to
// Apache 2.0 in October 2025 (after the 3-year BSL conversion period). Newer versions remain
// under BSL until their respective conversion dates:
//   - Akka 2.8.x / HTTP 10.5.x → Apache 2.0 on Feb-Mar 2026
//   - Akka 2.9.x / HTTP 10.6.x → Apache 2.0 on Oct 2026
//   - Akka HTTP 10.7.x → Apache 2.0 on Oct 2027
//   - Akka 2.10.x → Apache 2.0 on Dec 2028
lazy val akkaVersion     = "2.7.0"
lazy val akkaHttpVersion = "10.4.0"

lazy val `akka-http-cors` = project
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    // Java 9 Automatic-Module-Name (http://openjdk.java.net/projects/jigsaw/spec/issues/#AutomaticModuleNames)
    Compile / packageBin / packageOptions += Package.ManifestAttributes(
      "Automatic-Module-Name" -> "ch.megard.akka.http.cors"
    ),
    libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion cross CrossVersion.for3Use2_13,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion % Provided cross CrossVersion.for3Use2_13,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test cross CrossVersion.for3Use2_13,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test cross CrossVersion.for3Use2_13,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )

lazy val `akka-http-cors-example` = project
  .dependsOn(`akka-http-cors`)
  .settings(commonSettings)
  .settings(dontPublishSettings)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-stream"      % akkaVersion cross CrossVersion.for3Use2_13,
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion cross CrossVersion.for3Use2_13
    // libraryDependencies += "ch.megard" %% "akka-http-cors" % version.value
  )

lazy val `akka-http-cors-bench-jmh` = project
  .dependsOn(`akka-http-cors`)
  .enablePlugins(JmhPlugin)
  .settings(commonSettings)
  .settings(dontPublishSettings)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion cross CrossVersion.for3Use2_13
  )
