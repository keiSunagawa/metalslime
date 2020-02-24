import sbt._
import Keys._
import Dependencies._

object Base {
  val commonLibs = cats ++ testDep

  val commonScalaOptions =
    Seq(
      "-Ypartial-unification",
      "-deprecation",
      "-encoding",
      "utf-8",
      "-feature",
      "-Xlint",
      "-unchecked",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-unused",
      "-Ywarn-unused:-implicits",
      "-language:higherKinds",
      "-Ypatmat-exhaust-depth",
      "off"
    )

  lazy val settings = Seq(
    scalaVersion := "2.12.10",
    publish / skip := true,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )

  lazy val strictScalacOptions = Seq(
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    )
  )
}
