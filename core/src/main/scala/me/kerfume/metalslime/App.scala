package me.kerfume.metalslime

import me.kerfume.metalslime.loader.PatchLoader

object App {
  def run(workspace: String): Unit = {
    val loader = new PatchLoader(workspace)
    val list = loader.stub()
    val posList = list.flatMap { x =>
      val res = loader.loadFile(x)
      val find = loader.wark(res, x)
      find.map { x -> _ }
    }
    // posListから依存一覧の取得
    // didOpenリクエストの作成
    // difinetionリクエストの作成
    ???
  }
}
