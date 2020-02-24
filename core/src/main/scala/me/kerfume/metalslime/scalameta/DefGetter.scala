package me.kerfume.metalslime.scalameta

import me.kerfume.metalslime.models.Location
import Location._
import scala.meta._
import me.kerfume.metalslime.models.LineRange

object DefGetter {
  def getDefine(
      path: String,
      originRange: LineRange
  ): Option[ScalaMetaLineRange] = {
    val tree = loadFile(path)
    getMostInner(tree, originRange)
  }

  def getTopLevelDefines(path: String): List[ScalaMetaLineRange] = {
    val tree = loadFile(path)
    getTopLevelDefines0(tree)
  }

  private def loadFile(path: String): Source = {
    val src = scala.io.Source
      .fromFile(path)
      .getLines()
      .mkString("\n")
    val vf = Input.VirtualFile(path, src)
    vf.parse[Source].get
  }

  private def getTopLevelDefines0(
      tree: Tree
  ): List[ScalaMetaLineRange] = {
    tree.collect {
      case d: Defn.Class =>
        ScalaMetaLineRange(d.pos, d.name.pos)
      case d: Defn.Trait =>
        ScalaMetaLineRange(d.pos, d.name.pos)
      case d: Defn.Object =>
        ScalaMetaLineRange(d.pos, d.name.pos)
    }
  }
  private def getMostInner(
      tree: Tree,
      originRange: LineRange
  ): Option[ScalaMetaLineRange] = {
    var mostMatch: Option[ScalaMetaLineRange] = None
    tree.collect {
      case d: Defn.Def =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(originRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(originRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }
      case d: Defn.Class =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(originRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(originRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }
      case d: Defn.Trait =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(originRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(originRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }

      case d: Defn.Object =>
        val range = ScalaMetaLineRange(d.pos, d.name.pos)
        mostMatch match {
          case None =>
            if (range.isInner(originRange)) {
              mostMatch = Some(range)
            }
          case Some(current) =>
            if (range.isInner(originRange) && current.isInner(range)) {
              mostMatch = Some(range)
            }
        }
    }
    mostMatch
  }
}
