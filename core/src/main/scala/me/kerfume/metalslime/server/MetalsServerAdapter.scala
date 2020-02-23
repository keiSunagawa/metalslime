package me.kerfume.metalslime.server

import MetalsServerAdapter._
trait MetalsServerAdapter {
  def workspace: String

  def init(): Unit
  def didOpen(path: String, content: String): Unit
  def definition(path: String, line: Int, col: Int): List[RefFile]
  def close(): Unit
}

object MetalsServerAdapter {
  case class RefFile(path: String)
}
