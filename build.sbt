import sbt._

lazy val commonSettings = Seq(
  organization := "lt.vpranckaitis",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

val openNlp = "org.apache.opennlp" % "opennlp-tools" % "1.6.0"
val sprayJson= "io.spray" %%  "spray-json" % "1.3.2"
val elki = "de.lmu.ifi.dbs.elki" % "elki" % "0.7.1"


lazy val `text-mining` = project.in(file("."))
  .settings(name := "text-mining")
  .settings(commonSettings: _*)
  .aggregate(`text-crunching`, `text-classification`)

lazy val `text-crunching` = project.in(file("text-crunching"))
  .settings(commonSettings: _*)
  .settings(name := "text-crunching")
  .settings(libraryDependencies ++= Seq(openNlp, sprayJson))

lazy val `text-classification` = project.in(file("text-classification"))
  .settings(commonSettings: _*)
  .settings(name := "text-classification")
  .settings(libraryDependencies += elki)
  .dependsOn(`text-crunching`)