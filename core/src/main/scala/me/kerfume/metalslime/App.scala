package me.kerfume.metalslime

import me.kerfume.metalslime.loader.PatchLoader
import cats.data.Reader
import me.kerfume.metalslime.scalameta.DefGetter
import me.kerfume.metalslime.server.MetalsServerAdapter
import me.kerfume.metalslime.models.Location
import Location._
import me.kerfume.metalslime.server.MetalsServerAdapter.RefFile
import cats.instances.list._
import cats.syntax.traverse._

object App {
  // load pos list from patch file

  def run(workspace: String): Reader[MetalsServerAdapter, Unit] =
    for {
      _ <- Reader[MetalsServerAdapter, Unit] { _.init() }
      locList = PatchLoader.load("./tets_patch.txt")
      posList = locList.flatMap { loc =>
        val path = s"${workspace}${loc.file}"
        println(path)
        DefGetter.getDefine(path, loc.lineRange).map { x =>
          loc -> x
        }
      }
      res <- posList.traverse {
        case (loc, pos) =>
          fetchInvDep(s"${workspace}${loc.file}", pos)
      }
      res2 <- Reader[MetalsServerAdapter, Any] { server =>
        for {
          xs <- res
          y <- xs
        } yield {
          val pathOnly = y.path.replaceAll("""file://""", "")
          DefGetter.getTopLevelDefines(pathOnly).flatMap { pos =>
            fetchInvDep(pathOnly, pos).run(server)
          }
        }
      }
      _ = println(res2)
      _ <- Reader[MetalsServerAdapter, Unit] { _.close() }
    } yield ()

  def fetchInvDep(
      filePath: String,
      pos: ScalaMetaLineRange
  ): Reader[MetalsServerAdapter, List[RefFile]] = Reader { server =>
    println(filePath)
    val srcContent = scala.io.Source
      .fromFile(filePath)
      .getLines()
      .mkString("\n")

    server.didOpen(filePath, srcContent)

    server.compile()

    server.definition(
      filePath,
      pos.namePos.startLine,
      pos.namePos.startColumn
    )
  }
}
