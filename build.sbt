organization in ThisBuild := "me.kerfume"

version in ThisBuild := "0.1.0-SNAPSHOT"
scalafmtOnCompile in ThisBuild := true
trapExit in ThisBuild := false
lazy val metalslime = Metalslime.metalslime
lazy val root = (project in file("."))
  .aggregate(metalslime)
