package me.kerfume.metalslime.loader

import SelectorList._

case class SelectorList(
    file: String,
    lineRange: LineRange
) {}

object SelectorList {
  case class LineRange(strat: Int, end: Int) {
    def isInner(rhs: LineRange): Boolean = {
      this.strat <= rhs.strat && this.end >= rhs.end
    }
  }
}
