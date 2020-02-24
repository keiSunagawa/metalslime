package me.kerfume.metalslime.loader

import scala.util.parsing.combinator._
import me.kerfume.metalslime.models.Location
import Location.PureLineRange

object GitPatchPaser extends RegexParsers {
  val break: Parser[String] = "(\n|\r|\r\n)".r
  val all: Parser[String] = "(?!(\n|\r|\r\n)).*".r
  val diff: Parser[String] = "diff"
  val gitArg: Parser[String] = "--git"
  val path: Parser[List[String]] = """/[a-zA-Z1-9-_.]*""".r.*
  val filePath: Parser[String] = """(a|b)""".r ~> path ^^ { p =>
    p.mkString
  }
  val diffLine
      : Parser[String] = diff ~> gitArg ~> filePath ~> filePath <~ break.? ^^ {
    pathB =>
      pathB
  }
  val indexLine: Parser[String] = "index" ~> all <~ break.?
  val fileLine: Parser[String] = ("+++" | "---") ~> all <~ break.?

  val location: Parser[List[Int]] = ("+" | "-") ~> ("[0-9]+".r <~ ",".?).* ^^ {
    xs =>
      xs.map(_.toInt)
  }
  val lineNumLine
      : Parser[List[Int]] = "@@" ~> location ~> location <~ all <~ break.? ^^ {
    case loc =>
      loc
  }

  val codeLine: Parser[String] = ("+" | "-") ~> all <~ break.?

  val diffs: Parser[PureLineRange] = lineNumLine <~ codeLine.* ^^ { lns =>
    lns match {
      case s :: e :: Nil => PureLineRange(s, s + e)
      case s :: Nil      => PureLineRange(s, s)
      case _             => throw new RuntimeException("invalid diffs format.")
    }
  }

  val patches
      : Parser[List[Location]] = (diffLine ~ indexLine ~ fileLine ~ fileLine ~ diffs.*).* ^^ {
    _.flatMap {
      case fn ~ _ ~ _ ~ _ ~ ln =>
        ln.map { l =>
          Location(fn, l)
        }
    }
  }

  case class Patch(
      fileName: String
  )
}
object Test {
  def run(): Unit = {
    val res = GitPatchPaser
      .parse(
        GitPatchPaser.patches,
        """diff --git a/reminder-backend/infra/src/main/scala/me/kerfume/infra/impl/domain/seqid/SeqIDRepositoryRpc.scala b/reminder-backend/infra/src/main/scala/me/kerfume/infra/impl/domain/seqid/SeqIDRepositoryRpc.scala
       |index ef2c94b..f5d11fd 100644
       |--- a/reminder-backend/infra/src/main/scala/me/kerfume/infra/impl/domain/seqid/SeqIDRepositoryRpc.scala
       |+++ b/reminder-backend/infra/src/main/scala/me/kerfume/infra/impl/domain/seqid/SeqIDRepositoryRpc.scala
       |@@ -4 +4 @@ import cats.effect.IO
       |-import me.kerfume.infra.impl.domain.remind.RemindRepositoryRpc
       |+import me.kerfume.reminder.domain.remind.RemindRepository
       |@@ -8 +8 @@ class SeqIDRepositoryRpc(
       |-    reminderRepository: RemindRepositoryRpc
       |+    reminderRepository: RemindRepository[IO]
       |diff --git a/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/AppConfig.scala b/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/AppConfig.scala
       |index 0f9f76f..007ae5a 100644
       |--- a/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/AppConfig.scala
       |+++ b/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/AppConfig.scala
       |@@ -2,0 +3 @@ package me.kerfume.reminder.server
       |+import me.kerfume.reminder.server.AppConfig.Env
       |@@ -5,0 +7 @@ trait AppConfig {
       |+  def env: Env
       |@@ -6,0 +9 @@ trait AppConfig {
       |+  def launchPort: Int
       |@@ -22 +25,3 @@ class ProdAppConfig extends TypeSafeAppConfig {
       |-  lazy val configFileEnv = "prod"
       |+  val env: Env = Env.Prod
       |+  val configFileEnv = "prod"
       |+  val launchPort: Int = 8080
       |@@ -26 +31,3 @@ class LocalAppConfig extends TypeSafeAppConfig {
       |-  lazy val configFileEnv = "local"
       |+  val env: Env = Env.Local
       |+  val configFileEnv = "local"
       |+  val launchPort: Int = 9999
        """.stripMargin
      )
      .get
    println(res)
    val res2 = GitPatchPaser
      .parse(
        GitPatchPaser.indexLine,
        """index ef2c94b..f5d11fd 100644
        """.stripMargin
      )
      .get
    println(res2)

  }
}
