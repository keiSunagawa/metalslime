package me.kerfume.metalslime

import me.kerfume.metalslime.loader.PatchLoader

object App {
  def run(workspace: String): Unit = {
    val loader = new PatchLoader(workspace)
    val list = loader.stub()
    list.foreach { x =>
      val res = loader.loadFile(x)
      val find = loader.wark(res, x)
      println(find)
    }
  }
}
