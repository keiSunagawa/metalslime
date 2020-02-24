  override val skipWhitespace = false

  val break: Parser[String] = "\n".r
  val all: Parser[String] = "[^\n]*".r
    // 先頭行はproject root
    p.tail.mkString
      : Parser[String] = diff ~> whiteSpace ~> gitArg ~> whiteSpace ~> filePath ~> whiteSpace ~> filePath <~ break ^^ {
  val indexLine: Parser[String] = "index" ~> whiteSpace ~> all <~ break
  val newFileMode
      : Parser[String] = "new file mode" ~> whiteSpace ~> all <~ break
  val fileLine: Parser[String] = ("+++" | "---") ~> whiteSpace ~> all <~ break
      : Parser[List[Int]] = "@@" ~> whiteSpace ~> location ~> whiteSpace ~> location <~ all <~ break ^^ {
  val codeLine: Parser[String] = ("+" | "-") ~> all <~ break
      : Parser[List[Location]] = (diffLine ~ newFileMode.? ~ indexLine ~ fileLine ~ fileLine ~ diffs.*).* ^^ {
      case fn ~ _ ~ _ ~ _ ~ _ ~ ln =>
  val test = diffLine ~ newFileMode.? ~ indexLine // ~ fileLine ~ fileLine ~ diffs.*

        """diff --git a/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/Application.scala b/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/Application.scala
index e81b68d..84fb8cf 100644
--- a/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/Application.scala
+++ b/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/Application.scala
@@ -6 +6,6 @@ import me.kerfume.reminder.server.controller.RegistController
-import me.kerfume.reminder.domain.remind.{Remind, RemindService}
+import me.kerfume.reminder.domain.remind.{
+  Remind,
+  RemindRepository,
+  RemindRepositoryInMemory,
+  RemindService
+}
@@ -12,0 +18 @@ import me.kerfume.reminder.domain.seqid.{
+import me.kerfume.reminder.server.AppConfig.Env
@@ -15,2 +21,9 @@ class Application(config: AppConfig) {
-  val remindRepository = new RemindRepositoryRpc(config.rpcEndpoint)
-  val seqIDRepository = new SeqIDRepositoryRpc(remindRepository)
+  val remindRepository = config.env match {
+    case Env.Prod => new RemindRepositoryRpc(config.rpcEndpoint)
+    case _        => new IOWrapper.RemindRepositoryIOWrapper
+  }
+  val seqIDRepository = config.env match {
+    case Env.Prod => new SeqIDRepositoryRpc(remindRepository)
+    case _        => new IOWrapper.SeqIDRepositoryIOWrapper
+  }
+
@@ -27,0 +41,8 @@ object IOWrapper {
+  class RemindRepositoryIOWrapper extends RemindRepository[IO] {
+    private val internal = new RemindRepositoryInMemory
+
+    override def findAll(): IO[List[Remind]] = IO { internal.findAll() }
+
+    override def store(remind: Remind): IO[Unit] = IO { internal.store(remind) }
+  }
+
diff --git a/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/EndPoints.scala b/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/EndPoints.scala
index 814094c..7f77bc5 100644
--- a/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/EndPoints.scala
+++ b/reminder-backend/server/src/main/scala/me/kerfume/reminder/server/EndPoints.scala
@@ -2,0 +3 @@ package me.kerfume.reminder.server
+import me.kerfume.reminder.domain.remind.Remind
@@ -30 +31 @@ object EndPoints {
-  val list: Endpoint[Unit, Unit, String, Nothing] =
+  val list: Endpoint[Unit, Unit, ListResponse, Nothing] =
@@ -33 +34 @@ object EndPoints {
-      .out(htmlBodyUtf8)
+      .out(jsonBody[ListResponse])
""".stripMargin