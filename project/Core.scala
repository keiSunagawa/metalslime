import sbt._
import Keys._
import Dependencies._

object Core {
  lazy val core = (project in file("core"))
    .settings(Base.settings)
    .settings(
      name := "metalslime-core",
      scalacOptions ++= Base.commonScalaOptions,
      libraryDependencies ++= Base.commonLibs ++ Seq(
        "org.scalameta" %% "metals" % "0.8.0",
        "org.typelevel" %% "cats-effect" % "2.1.1"
      )
    )
}
