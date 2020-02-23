package me.kerfume.metalslime

import me.kerfume.metalslime.loader.PatchLoader
import cats.data.Reader
import me.kerfume.metalslime.scalameta.DefGetter
import me.kerfume.metalslime.server.MetalsServerAdapter
import me.kerfume.metalslime.models.Location
import Location._

object App {
  def run(workspace: String): Reader[MetalsServerAdapter, Unit] =
    Reader { server =>
      // load pos list from patch file
      val loader = new PatchLoader(workspace)
      val defGetter = new DefGetter(workspace)

      // launch server
      server.init()

      val locList = loader.stub()
      val posList: List[(Location, ScalaMetaLineRange)] = locList.flatMap {
        loc =>
          defGetter.getDefine(loc).map { x =>
            loc -> x
          }
      }

      val res = posList.map {
        case (loc, pos) =>
          // FIXME loadが毎回書いて微妙
          val srcContent = scala.io.Source
            .fromFile(s"${workspace}/${loc.file}")
            .getLines()
            .mkString("\n")

          println(loc.file)
          println(pos.namePos.startLine)
          println(pos.namePos.startColumn)

          server.didOpen(loc.file, srcContent)

          server.compile()

          server.definition(
            loc.file,
            pos.namePos.startLine,
            pos.namePos.startColumn
          )
      }

      println(res)

      // posListから依存一覧の取得
      // didOpenリクエストの作成
      // difinetionリクエストの作成
      ()
    }
}
