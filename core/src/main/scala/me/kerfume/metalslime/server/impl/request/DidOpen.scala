package me.kerfume.metalslime.server.impl.request

import org.eclipse.lsp4j._
import scala.collection.JavaConverters._

object DidOpen {
  def request(
      path: String,
      content: String
  ): DidOpenTextDocumentParams = {
    val td = new DidOpenTextDocumentParams()
    val tdRow = new TextDocumentItem()
    tdRow.setUri(
      s"file://${path}"
    )
    tdRow.setLanguageId("scala")
    tdRow.setVersion(0)
    tdRow.setText(content)
    td.setTextDocument(tdRow)
    td
  }
}
