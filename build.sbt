import sbt._

lazy val commonSettings = Seq(
  organization := "lt.vpranckaitis",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

val openNlp = "org.apache.opennlp" % "opennlp-tools" % "1.6.0"

lazy val `text-mining` = project.in(file("."))
  .aggregate(`text-crunching`)
  .settings(name := "text-mining")
  .settings(commonSettings: _*)

lazy val `text-crunching` = project.in(file("text-crunching"))
  .settings(commonSettings: _*)
  .settings(name := "text-crunching")
  .settings(libraryDependencies += openNlp)
