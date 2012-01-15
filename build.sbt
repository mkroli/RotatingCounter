organization := "de.krolikowski.counter"

name := "counter"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.6.1" % "test",
  "junit" % "junit" % "4.10" % "test")

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation"
)
