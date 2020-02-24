package me.kerfume.metalslime

import me.kerfume.metalslime.loader.PatchLoader
import me.kerfume.metalslime.scalameta.DefGetter
import me.kerfume.metalslime.server.MetalsServerAdapter
import me.kerfume.metalslime.models.Location
import Location._
import cats.Monad
import me.kerfume.metalslime.server.MetalsServerAdapter.RefFile
import cats.mtl.{ApplicativeAsk, MonadState}
//import cats.implicits._
import cats.instances.list._
import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.syntax.functor._
//import cats.mtl.implicits._

object App {
  case class State(
      visitedFiles: Set[String] = Set.empty
  ) {
    def visited(path: String): State = copy(
      visitedFiles = visitedFiles + path
    )
    def isVisited(path: String): Boolean = visitedFiles(path)
  }
  def run2[F[_]: Monad](workspace: String)(
      implicit R: ApplicativeAsk[F, MetalsServerAdapter],
      S: MonadState[F, State]
  ): F[Unit] = {
    for {
      server <- R.ask
      _ = server.init()
      locList = PatchLoader.load("./tets_patch.txt")
      posList = locList.flatMap { loc =>
        val path = s"${workspace}${loc.file}"
        DefGetter.getDefine(path, loc.lineRange).map { x =>
          loc -> x
        }
      }
      changedFileInvDeps <- posList.flatTraverse {
        case (loc, pos) =>
          val a = s"${workspace}${loc.file}"
          for {
            invDeps <- fetchInvDepF[F](a, pos)
            _ <- invDeps.traverse { rf =>
              S.modify(_.visited(rf.path.replaceAll("""file://""", "")))
            }
          } yield invDeps
      }
      _ <- changedFileInvDeps.flatTraverse { dep =>
        val pathOnly = dep.path.replaceAll("""file://""", "")
        for {
          st <- S.get
          res <- if (st.isVisited(pathOnly))
            R.applicative.pure(List.empty[RefFile])
          else
            DefGetter.getTopLevelDefines(pathOnly).flatTraverse { pos =>
              for {
                invDeps <- fetchInvDepF[F](pathOnly, pos)
                _ <- invDeps.traverse { rf =>
                  S.modify(_.visited(rf.path.replaceAll("""file://""", "")))
                }
              } yield invDeps
            }
        } yield res
      }
      st <- S.get
    } yield {
      println(st.visitedFiles)
      ()
    }
  }

  def fetchInvDepF[F[_]: Monad](
      filePath: String,
      pos: ScalaMetaLineRange
  )(implicit R: ApplicativeAsk[F, MetalsServerAdapter]): F[List[RefFile]] =
    R.ask.map { server =>
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
