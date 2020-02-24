import sbt._
import Keys._
import Dependencies._
import sbtdocker.DockerPlugin.autoImport._
import sbtassembly.AssemblyPlugin.autoImport._
import sbtdocker.DockerPlugin

object Metalslime {
  lazy val metalslime = (project in file("metalslime"))
    .settings(Base.settings)
    .enablePlugins(DockerPlugin)
    .settings(
      name := "metalslime",
      scalacOptions ++= Base.commonScalaOptions,
      libraryDependencies ++= Base.commonLibs ++ Seq(
        "org.scalameta" %% "metals" % "0.8.0",
        "org.typelevel" %% "cats-effect" % "2.1.1",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
        "org.typelevel" %% "cats-mtl-core" % "0.7.0"
      ),
      dockerfile in docker := {
        // The assembly task generates a fat JAR file
        val artifact: File = assembly.value
        val artifactTargetPath = s"/app/${artifact.name}"

        new Dockerfile {
          from("openjdk:8-jre")
          add(artifact, artifactTargetPath)
          entryPoint("java", "-jar", artifactTargetPath)
        }
      },
      imageNames in docker := Seq(
        // Sets the latest tag
        ImageName(s"keisunagawa/${name.value}:latest")
      ),
      assemblyMergeStrategy in assembly := {
        case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".properties" =>
          MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".types" =>
          MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".class" =>
          MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".so" =>
          MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".jnilib" =>
          MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".dll" =>
          MergeStrategy.first
        case "application.conf" => MergeStrategy.concat
        case "unwanted.txt"     => MergeStrategy.discard
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      }
    )
}
