
name := "akka-http-cors"
organization := "ch.megard"
version := "0.1"

scalaVersion := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % "2.4.2"

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "2.4.2" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
