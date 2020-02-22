package me.kerfume.metalslime.request

import org.eclipse.lsp4j._
import scala.collection.JavaConverters._

object Initialize {
  private val symbols = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26).map(SymbolKind.forValue(_))

  def request(workspace: String): InitializeParams = {
    def workSpace: WorkspaceClientCapabilities = {
      val ws = new WorkspaceClientCapabilities()
      ws.setApplyEdit(true)
      val workspaceEdit = new WorkspaceEditCapabilities()
      workspaceEdit.setDocumentChanges(true)
      workspaceEdit.setResourceOperations(
        List("create", "rename", "delete").asJava
      )
      // skip didChangeWatchedFiles
      ws.setWorkspaceEdit(workspaceEdit)

      val sym = new SymbolCapabilities()
      val symK = new SymbolKindCapabilities()
      symK.setValueSet(symbols.asJava)
      sym.setSymbolKind(symK)
      ws.setSymbol(sym)
      // skip executeCommand
      ws.setWorkspaceFolders(true)
      ws.setConfiguration(true)
      ws
    }
    def textDocment: TextDocumentClientCapabilities = {
      val td = new TextDocumentClientCapabilities()
      // skip synchronization
      // skip completion
      // skip hover
      // skip signatureHelp
      val symTd = new DocumentSymbolCapabilities()
      val symKTd = new SymbolKindCapabilities()
      symKTd.setValueSet(symbols.asJava)
      symTd.setSymbolKind(symKTd)
      td.setDocumentSymbol(symTd)

      val df = new DefinitionCapabilities()
      df.setLinkSupport(true)
      td.setDefinition(df)
      td
    }

    val p = new InitializeParams()
    p.setProcessId(1) // not use?
    p.setRootUri(s"file:///${workspace}")

    val c = new ClientCapabilities()

    c.setWorkspace(workSpace)
    c.setTextDocument(textDocment)

    p.setCapabilities(c)
    p
  }
}
