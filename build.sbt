lazy val commonSettings = Seq(
  organization := "ch.megard",
  version := "0.3.4-SNAPSHOT",
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq(scalaVersion.value, "2.11.12"),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-target:jvm-1.8",
    "-encoding", "utf8",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
  ),
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
  },
)

lazy val dontPublishSettings = Seq(
  //publishSigned := (()),
  publish := (()),
  publishLocal := (()),
  publishArtifact := false,
)

lazy val root = (project in file("."))
  .aggregate(`akka-http-cors`, `akka-http-cors-example`, `akka-http-cors-bench-jmh`)
  .settings(commonSettings)
  .settings(dontPublishSettings)

lazy val akkaVersion = "2.5.19"
lazy val akkaHttpVersion = "10.1.7"

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
    libraryDependencies += "org.scalatest"     %% "scalatest"           % "3.0.5"          % Test,
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
