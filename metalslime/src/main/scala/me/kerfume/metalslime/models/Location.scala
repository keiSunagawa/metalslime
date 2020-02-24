package me.kerfume.metalslime.models

import me.kerfume.metalslime.models.Location._

case class Location(
    file: String,
    lineRange: LineRange
) {}

trait LineRange {
  def start: Int
  def end: Int

  def isInner(rhs: LineRange): Boolean = {
    this.start <= rhs.start && this.end >= rhs.end
  }
}
object Location {
  case class PureLineRange(start: Int, end: Int) extends LineRange
  case class ScalaMetaLineRange(
      pos: scala.meta.Position,
      namePos: scala.meta.Position
  ) extends LineRange {
    // metalsnの行判定が0 started?なので+1する
    def start: Int = pos.startLine + 1
    def end: Int = pos.endLine + 1
  }
}
