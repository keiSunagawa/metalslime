package me.kerfume.metalslime.loader

import me.kerfume.metalslime.models.Location
import me.kerfume.metalslime.models.Location._

object PatchLoader {
  def load(patchPath: String): List[Location] = {
    val patch = scala.io.Source.fromFile(patchPath).getLines().mkString("\n")
    val diffList = GitPatchPaser.parse(GitPatchPaser.patches, patch).get
    diffList.filter(_.file.endsWith(".scala"))
  }
}
