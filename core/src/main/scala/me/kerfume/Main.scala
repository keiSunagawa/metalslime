package me.kerfume

import java.nio.charset.StandardCharsets
import scala.meta.internal.metals._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

import org.eclipse.lsp4j._

import scala.meta.io.AbsolutePath
import scala.util.control.NonFatal
import metalslime._
import request._
import me.kerfume.metalslime.request.Definition.PointLocation

object Main {
  def main(args: Array[String]): Unit = {
    val exec = Executors.newCachedThreadPool()
    val ec = ExecutionContext.fromExecutorService(exec)
    val config = MetalsServerConfig.base.copy(
      isExitOnShutdown = true
    )
    val server = new MetalsLanguageServer(
      ec,
      redirectSystemOut = true,
      charset = StandardCharsets.UTF_8,
      config = config
    )

    val workspace = "/Users/kerfume/gits/Reminder/reminder-backend"

    val path = AbsolutePath(
      "file:///Users/kerfume/gits/Reminder/reminder-backend"
    )
    val buffers = Buffers()
    val client = new mock.MockClient(path, buffers)

    MetalsLogger.updateDefaultFormat()

    server.connectToLanguageClient(client)

    val initialize = Initialize.request(workspace)
    server.initialize(initialize).get()

    val a = new InitializedParams
    server.initialized(a).get()

    val didOpen = DidOpen.request(
      workspace,
      "server/src/main/scala/me/kerfume/reminder/server/ErrorInfo.scala",
      "package me.kerfume.reminder.server\n\nimport sttp.model.{StatusCode, Uri}\nimport sttp.tapir.{Codec, CodecFormat, EndpointOutput}\n\nsealed trait ErrorInfo\nobject ErrorInfo {\n  case class BadRequest(msg: String) extends ErrorInfo\n  object BadRequest {\n    implicit val codecPlaneText\n        : Codec[BadRequest, CodecFormat.TextPlain, String] \u003d\n      Codec.stringPlainCodecUtf8.map(BadRequest(_))(_.msg)\n  }\n  case class Redirect(uri: Uri) extends ErrorInfo\n  object Redirect {\n//    implicit val codecPlaneText\n//        : Codec[Redirect, CodecFormat.TextPlain, String] \u003d\n//      Codec.stringPlainCodecUtf8.map(\n//        s \u003d\u003e Redirect(Uri.parse(s).toOption.get) // FIXME unsafe\n//      )(_.uri.toString)\n    // FIXME: frontend: Affjax?redirect??????????????200???, affjax????????????????\n    implicit val codecPlaneText\n        : Codec[Redirect, CodecFormat.TextPlain, String] \u003d\n      Codec.stringPlainCodecUtf8.map(\n        s \u003d\u003e Redirect(Uri.parse(s).toOption.get) // FIXME unsafe\n      )(x \u003d\u003e s\"go redirect: ${x.uri.toString}\")\n  }\n\n  import sttp.tapir._\n\n  def errorInfoOutput: EndpointOutput[ErrorInfo] \u003d\n    oneOf[ErrorInfo](\n      statusMapping(StatusCode.BadRequest, plainBody[BadRequest]),\n//      statusMapping(\n//        StatusCode.MovedPermanently,\n//        header(\"Cache-Control\", \"no-cache\") and header[Redirect](\"Location\")\n//      )\n      // FIXME: frontend: Affjax?redirect??????????????200???, affjax????????????????\n      statusMapping(\n        StatusCode.Ok,\n        header(\"Cache-Control\", \"no-cache\") and plainBody[\n          Redirect\n        ]\n      )\n    )\n}\n"
    )

    server.didOpen(didOpen).get()

    val definition = Definition.request(
      workspace,
      "server/src/main/scala/me/kerfume/reminder/server/ErrorInfo.scala",
      PointLocation(6, 10)
    )

    val res = server.definition(definition).get()

    println(res)

    server.shutdown().get()
  }
}
