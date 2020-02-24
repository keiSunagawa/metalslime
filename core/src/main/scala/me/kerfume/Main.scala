package me.kerfume

import java.nio.charset.StandardCharsets

import scala.meta.internal.metals._
import java.util.concurrent.Executors

import cats.data.{Reader, StateT}
import me.kerfume.metalslime.server.MetalsServerAdapter

import scala.concurrent.ExecutionContext
import org.eclipse.lsp4j._

import scala.meta.io.AbsolutePath
import scala.util.control.NonFatal
import metalslime._
import me.kerfume.metalslime.server.impl.request._
import me.kerfume.metalslime.server.impl.request.Definition.PointLocation
import me.kerfume.metalslime.server.impl.MetalsServer
import cats.mtl.implicits._

object Main {
  def main(args: Array[String]): Unit = {
    val workspace = "/Users/kerfume/gits/Reminder/reminder-backend"

    val server = new MetalsServer(workspace)
    App
      .run2[StateT[Reader[MetalsServerAdapter, *], App.State, *]](workspace)
      .runA(App.State())
      .run(server)

    server.close()
  }
}
