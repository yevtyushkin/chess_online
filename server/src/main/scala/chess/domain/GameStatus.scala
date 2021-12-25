package com.chessonline
package chess.domain

import enumeratum._

sealed abstract class GameStatus(val tag: String) extends EnumEntry

object GameStatus extends Enum[GameStatus] {
  val values: IndexedSeq[GameStatus] = findValues

  case object GameContinues extends GameStatus("gameContinues")
  final case class Win(by: Side) extends GameStatus("win")
  case object Draw extends GameStatus("draw")
}
