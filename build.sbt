lazy val commonSettings = Seq(
  organization       := "ch.megard",
  version            := "1.2.1-SNAPSHOT",
  scalaVersion       := "2.13.11",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.17", "3.2.2"),
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
  licenses := Seq("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(
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

lazy val akkaVersion     = "2.6.20"
lazy val akkaHttpVersion = "10.2.10"

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
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % Test
  )

lazy val `akka-http-cors-example` = project
  .dependsOn(`akka-http-cors`)
  .settings(commonSettings)
  .settings(dontPublishSettings)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion cross CrossVersion.for3Use2_13
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
