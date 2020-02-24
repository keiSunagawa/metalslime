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
  def run(workspace: String): Reader[MetalsServerAdapter, Unit] = {
    var visitedPaths: Set[String] = Set.empty
    var targetFiles: Set[String] = Set.empty
    val targetRegex = ".*server.*".r
    for {
      _ <- Reader[MetalsServerAdapter, Unit] { _.init() }
      locList = PatchLoader.load("./tets_patch.txt")
      posList = locList.flatMap { loc =>
        val path = s"${workspace}${loc.file}"
        DefGetter.getDefine(path, loc.lineRange).map { x =>
          loc -> x
        }
      }
      changedFileInvDeps <- posList.flatTraverse {
        case (loc, pos) =>
          fetchInvDep(s"${workspace}${loc.file}", pos).map { invDeps =>
            invDeps.foreach { rf =>
              val pathOnly = rf.path.replaceAll("""file://""", "")
              if (targetRegex.findFirstIn(pathOnly).nonEmpty) {
                targetFiles += pathOnly
              }
            }
            invDeps
          }
      }
      _ <- changedFileInvDeps.flatTraverse { dep =>
        val pathOnly = dep.path.replaceAll("""file://""", "")
        if (visitedPaths(pathOnly))
          Reader[MetalsServerAdapter, List[RefFile]](_ => Nil)
        else
          DefGetter.getTopLevelDefines(pathOnly).flatTraverse { pos =>
            fetchInvDep(pathOnly, pos).map { xs =>
              visitedPaths += pathOnly
              xs
            }
          }
      }
      _ = println(visitedPaths)
      //_ = println(targetFiles)
      _ <- Reader[MetalsServerAdapter, Unit] { _.close() }
    } yield ()
  }
  def fetchInvDep(
      filePath: String,
      pos: ScalaMetaLineRange
  ): Reader[MetalsServerAdapter, List[RefFile]] = Reader { server =>
    val srcContent = scala.io.Source
      .fromFile(filePath)
      .getLines()
      .mkString("\n")

    server.didOpen(filePath, srcContent)
    println(filePath)
    println(s"start: ${pos.namePos.startLine}, ${pos.namePos.startColumn}")

    server.compile()

    val res = server.definition(
      filePath,
      pos.namePos.startLine,
      pos.namePos.startColumn
    )
    println(res)
    res
  }
}
