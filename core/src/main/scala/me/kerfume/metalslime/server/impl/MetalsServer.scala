package me.kerfume.metalslime.server.impl

import me.kerfume.metalslime.server.MetalsServerAdapter
import java.nio.charset.StandardCharsets
import scala.meta.internal.metals._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

import org.eclipse.lsp4j._

import scala.meta.io.AbsolutePath
import scala.util.control.NonFatal
import mock._
import request._
import org.eclipse.lsp4j._
import MetalsServerAdapter._
import me.kerfume.metalslime.server.impl.request.Definition.PointLocation
import scala.collection.JavaConverters._

class MetalsServer(val workspace: String) extends MetalsServerAdapter {
  import scala.meta.internal.metals._

  def init(): Unit = {
    val initialize = Initialize.request(workspace)
    underliyng.initialize(initialize).get()

    val a = new InitializedParams
    underliyng.initialized(a).get()
  }

  def didOpen(path: String, content: String): Unit = {
    val didOpen = DidOpen.request(
      path,
      content
    )

    underliyng.didOpen(didOpen).get()
  }

  def definition(
      path: String,
      line: Int,
      col: Int
  ): List[RefFile] = {
    val definition = Definition.request(
      path,
      PointLocation(line, col)
    )

    val res = underliyng.definition(definition).get()

    res.asScala.map { r =>
      RefFile(r.getUri())
    }.toList
  }

  def close(): Unit = {
    underliyng.shutdown().get()
  }

  def compile(): Unit = {
    val cmd = new ExecuteCommandParams()
    cmd.setCommand("metals.compile-cascade")
    underliyng.executeCommand(cmd).get()
    ()
  }

  val exec = Executors.newCachedThreadPool()
  val ec = ExecutionContext.fromExecutorService(exec)
  val config = MetalsServerConfig.base.copy(
    //isExitOnShutdown = true
  )
  val underliyng = new MetalsLanguageServer(
    ec,
    redirectSystemOut = true,
    charset = StandardCharsets.UTF_8,
    config = config
  )

  val path = AbsolutePath(
    s"file://${workspace}"
  )
  val buffers = Buffers()
  val client = new MockClient(path, buffers)

  MetalsLogger.updateDefaultFormat()

  underliyng.connectToLanguageClient(client)
}
