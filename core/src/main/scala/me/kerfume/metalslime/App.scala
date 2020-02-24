package me.kerfume.metalslime

import me.kerfume.metalslime.loader.PatchLoader
import me.kerfume.metalslime.scalameta.DefGetter
import me.kerfume.metalslime.server.MetalsServerAdapter
import me.kerfume.metalslime.models.Location
import Location._
import cats.Monad
import me.kerfume.metalslime.server.MetalsServerAdapter.RefFile
import cats.mtl.{ApplicativeAsk, MonadState}
import cats.instances.list._
import cats.syntax.traverse._
import cats.syntax.foldable._
import cats.syntax.flatMap._
import cats.syntax.functor._

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
          } yield invDeps
      }
      _ <- changedFileInvDeps.traverse_ { dep =>
        rec[F](dep)
      }
      st <- S.get
    } yield {
      println(st.visitedFiles)
      ()
    }
  }

  def rec[F[_]: Monad](file: RefFile)(
      implicit R: ApplicativeAsk[F, MetalsServerAdapter],
      S: MonadState[F, State]
  ): F[Unit] = {
    val pathOnly = file.path.replaceAll("""file://""", "")
    for {
      st <- S.get
      _ <- if (st.isVisited(pathOnly)) R.applicative.unit
      else
        DefGetter.getTopLevelDefines(pathOnly).traverse_ { pos =>
          for {
            invDeps <- fetchInvDepF[F](pathOnly, pos)
            _ <- S.modify(_.visited(pathOnly))
            _ <- invDeps.traverse_(rf => rec[F](rf))
          } yield ()
        }
    } yield ()
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
