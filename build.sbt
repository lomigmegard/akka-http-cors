
lazy val commonSettings = Seq(
  organization := "ch.megard",
  version := "0.3.1-SNAPSHOT",
  scalaVersion := "2.12.6",
  crossScalaVersions := Seq(scalaVersion.value, "2.11.12", "2.13.0-M3"),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-target:jvm-1.8",
    "-encoding", "utf8",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused"
  )
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  pomExtra := {
    <url>https://github.com/lomigmegard/akka-http-cors</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:lomigmegard/akka-http-cors.git</url>
      <connection>scm:git:git@github.com:lomigmegard/akka-http-cors.git</connection>
    </scm>
    <developers>
      <developer>
        <id>lomigmegard</id>
        <name>Lomig Mégard</name>
        <url>http://lomig.megard.ch</url>
      </developer>
    </developers>
  }
)

lazy val dontPublishSettings = Seq(
  //publishSigned := (()),
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

lazy val root = (project in file(".")).
  aggregate(`akka-http-cors`, `akka-http-cors-example`, `akka-http-cors-bench-jmh`).
  settings(commonSettings: _*).
  settings(dontPublishSettings: _*)

lazy val akkaVersion = "2.5.11"
lazy val akkaHttpVersion = "10.1.1"

lazy val `akka-http-cors` = project.
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion % "provided",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5-M1" % "test"
  )

lazy val `akka-http-cors-example` = project.
  dependsOn(`akka-http-cors`).
  settings(commonSettings: _*).
  settings(dontPublishSettings: _*).
  settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
    //libraryDependencies += "ch.megard" %% "akka-http-cors" % version.value
  )

lazy val `akka-http-cors-bench-jmh` = project.
  dependsOn(`akka-http-cors`).
  enablePlugins(JmhPlugin).
  settings(commonSettings: _*).
  settings(dontPublishSettings: _*).
  settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
  )
