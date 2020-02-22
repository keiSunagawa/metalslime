package me.kerfume.metalslime.loader

import SelectorList._

case class SelectorList(
    file: String,
    lineRange: LineRange
) {}

object SelectorList {
  trait LineRange {
    def start: Int
    def end: Int

    def isInner(rhs: LineRange): Boolean = {
      this.start <= rhs.start && this.end >= rhs.end
    }
  }
  case class PureLineRange(start: Int, end: Int) extends LineRange
  case class MetalsLineRange(pos: scala.meta.Position) extends LineRange {
    // metalsnの行判定が0 started?なので+1する
    def start: Int = pos.startLine + 1
    def end: Int = pos.endLine + 1
  }
}
