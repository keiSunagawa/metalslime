organization in ThisBuild := "me.kerfume"

version in ThisBuild := "0.1.0-SNAPSHOT"
scalafmtOnCompile in ThisBuild := true
trapExit in ThisBuild := false
lazy val core = Core.core
lazy val root = (project in file("."))
  .aggregate(core)
