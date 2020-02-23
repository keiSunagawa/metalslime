package me.kerfume.metalslime.scalameta

import me.kerfume.metalslime.models.Location
import Location._
import scala.meta._

class DefGetter(workspace: String) {
  def getDefine(selector: Location): Option[ScalaMetaLineRange] = {
    val path = s"${workspace}/${selector.file}"
    val tree = loadFile(path)
    getMostInner(tree, selector)
  }

  def loadFile(path: String): Source = {
    val src = scala.io.Source
      .fromFile(path)
      .getLines()
      .mkString("\n")
    val vf = Input.VirtualFile(path, src)
    vf.parse[Source].get
  }

  def getMostInner(
      tree: Tree,
      selector: Location
  ): Option[ScalaMetaLineRange] = {
    var mostMatch: Option[ScalaMetaLineRange] = None
    tree.collect {
      case d: Defn.Def =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(selector.lineRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(selector.lineRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }
      case d: Defn.Class =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(selector.lineRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(selector.lineRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }
      case d: Defn.Trait =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(selector.lineRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(selector.lineRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }

      case d: Defn.Object =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(selector.lineRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(selector.lineRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }
    }
    mostMatch
  }
}
