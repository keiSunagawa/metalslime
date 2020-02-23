package me.kerfume.metalslime.loader

import me.kerfume.metalslime.models.Location
import me.kerfume.metalslime.models.Location._

import scala.meta._

class PatchLoader(workspace: String) {
  def stub(): List[Location] = {
    List(
      // Location(
      //   // TODO ignore project root dir
      //   "server/src/main/scala/me/kerfume/reminder/server/AppConfig.scala",
      //   PureLineRange(9, 9)
      // ),
      Location(
        // TODO ignore project root dir
        "infra/src/main/scala/me/kerfume/infra/impl/domain/seqid/SeqIDRepositoryRpc.scala",
        PureLineRange(8, 8)
      )
      // Location(
      //   // TODO ignore project root dir
      //   "server/src/main/scala/me/kerfume/reminder/server/ErrorInfo.scala",
      //   PureLineRange(6, 6)
      // )
    )
  }
}
