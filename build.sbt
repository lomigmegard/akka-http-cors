lazy val commonSettings = Seq(
  organization := "ch.megard",
  version := "1.0.1-SNAPSHOT",
  scalaVersion := "2.13.2",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.11"),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-target:jvm-1.8",
    "-encoding", "utf8",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
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
    Developer(id="lomigmegard", name="Lomig Mégard", email="", url=url("https://lomig.ch"))
  ),
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := sonatypePublishToBundle.value,
)

lazy val dontPublishSettings = Seq(
  skip in publish := true,
)

lazy val root = (project in file("."))
  .aggregate(`akka-http-cors`, `akka-http-cors-example`, `akka-http-cors-bench-jmh`)
  .settings(commonSettings)
  .settings(dontPublishSettings)

lazy val akkaVersion = "2.6.5"
lazy val akkaHttpVersion = "10.1.12"

lazy val `akka-http-cors` = project
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(

    // Java 9 Automatic-Module-Name (http://openjdk.java.net/projects/jigsaw/spec/issues/#AutomaticModuleNames)
    packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
      "Automatic-Module-Name" -> "ch.megard.akka.http.cors"
    ),

    libraryDependencies += "com.typesafe.akka" %% "akka-http"           % akkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream"         % akkaVersion      % Provided,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion  % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion      % Test,
    libraryDependencies += "org.scalatest"     %% "scalatest"           % "3.1.2"          % Test,
  )

lazy val `akka-http-cors-example` = project
  .dependsOn(`akka-http-cors`)
  .settings(commonSettings)
  .settings(dontPublishSettings)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
    //libraryDependencies += "ch.megard" %% "akka-http-cors" % version.value
  )

lazy val `akka-http-cors-bench-jmh` = project
  .dependsOn(`akka-http-cors`)
  .enablePlugins(JmhPlugin)
  .settings(commonSettings)
  .settings(dontPublishSettings)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
  )
