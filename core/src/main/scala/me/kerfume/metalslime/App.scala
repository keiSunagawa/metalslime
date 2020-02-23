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

//      Thread.sleep(20000)
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
          //Thread.sleep(600000)
//          Thread.sleep(10000)
          val r1 = server.definition(
            loc.file,
            pos.namePos.startLine,
            pos.namePos.startColumn
          )
          println(r1)
          Thread.sleep(1000)
          val r2 = server.definition(
            loc.file,
            pos.namePos.startLine,
            pos.namePos.startColumn
          )
          println(r2)
      }

//      Thread.sleep(10000)
      println(res)

      //    server.close()

      // posListから依存一覧の取得
      // didOpenリクエストの作成
      // difinetionリクエストの作成
      ()
    }
}
