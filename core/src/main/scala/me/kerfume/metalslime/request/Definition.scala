package me.kerfume.metalslime.request

import org.eclipse.lsp4j._
import scala.collection.JavaConverters._

object Definition {
  def request(
      workspace: String,
      path: String,
      loc: PointLocation
  ): TextDocumentPositionParams = {
    val td = new TextDocumentIdentifier()
    td.setUri(
      s"file://${workspace}/${path}"
    )
    val pos = new Position()
    pos.setLine(loc.line)
    pos.setCharacter(loc.col)
    val tdp = new TextDocumentPositionParams()

    tdp.setPosition(pos)
    tdp.setTextDocument(td)
    tdp
  }

  case class PointLocation(line: Int, col: Int)
}
