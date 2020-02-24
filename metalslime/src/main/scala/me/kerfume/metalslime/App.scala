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
  def run[F[_]: Monad](workspace: String, patchFile: String)(
      implicit R: ApplicativeAsk[F, MetalsServerAdapter],
      S: MonadState[F, State]
  ): F[Unit] = {
    for {
      server <- R.ask
      _ = server.init()
      locList = PatchLoader.load(patchFile)
      posList = locList.flatMap { loc =>
        val path = s"${workspace}${loc.file}"
        DefGetter.getDefine(path, loc.lineRange).map { x =>
          path -> x
        }
      }
      changedFileInvDeps <- posList.flatTraverse {
        case (path, pos) =>
          for {
            invDeps <- fetchInvDep[F](path, pos)
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

  private def rec[F[_]: Monad](file: RefFile)(
      implicit R: ApplicativeAsk[F, MetalsServerAdapter],
      S: MonadState[F, State]
  ): F[Unit] = {
    for {
      st <- S.get
      _ <- if (st.isVisited(file.path)) R.applicative.unit
      else
        DefGetter.getTopLevelDefines(file.path).traverse_ { pos =>
          for {
            invDeps <- fetchInvDep[F](file.path, pos)
            _ <- S.modify(_.visited(file.path))
            _ <- invDeps.traverse_(rf => rec[F](rf))
          } yield ()
        }
    } yield ()
  }

  private def fetchInvDep[F[_]: Monad](
      filePath: String,
      pos: ScalaMetaLineRange
  )(implicit R: ApplicativeAsk[F, MetalsServerAdapter]): F[List[RefFile]] =
    R.ask.map { server =>
      val srcContent = scala.io.Source
        .fromFile(filePath)
        .getLines()
        .mkString("\n")

      server.didOpen(filePath, srcContent)
//      println(filePath)
//      println(s"start: ${pos.namePos.startLine}, ${pos.namePos.startColumn}")

      server.compile()

      val res = server.definition(
        filePath,
        pos.namePos.startLine,
        pos.namePos.startColumn
      )
      //println(res)
      res
    }

  case class State(
      visitedFiles: Set[String] = Set.empty
  ) {
    def visited(path: String): State = copy(
      visitedFiles = visitedFiles + path
    )
    def isVisited(path: String): Boolean = visitedFiles(path)
  }
}
