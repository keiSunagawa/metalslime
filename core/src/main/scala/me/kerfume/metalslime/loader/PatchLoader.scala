package me.kerfume.metalslime.loader

import SelectorList._
import scala.meta._

class PatchLoader(workspace: String) {
  def stub(): List[SelectorList] = {
    List(
      SelectorList(
        // TODO ignore project root dir
        "infra/src/main/scala/me/kerfume/infra/impl/domain/seqid/SeqIDRepositoryRpc.scala",
        PureLineRange(8, 8)
      )
    )
  }

  def loadFile(selector: SelectorList): Source = {
    val path = s"${workspace}/${selector.file}"
    val src =
      scala.io.Source
        .fromFile(path)
        .getLines()
        .mkString("\n")
    val vf = Input.VirtualFile(path, src)
    vf.parse[Source].get
  }

  def wark(tree: Tree, selector: SelectorList): Option[MetalsLineRange] = {
    var mostMatch: Option[MetalsLineRange] = None
    tree.collect {
      case d: Defn =>
        // metalsnの行判定が0 started?なので+1する
        val range = MetalsLineRange(d.pos)
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
