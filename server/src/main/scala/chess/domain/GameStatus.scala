package com.chessonline
package chess.domain

import enumeratum._

sealed trait GameStatus extends EnumEntry

object GameStatus extends Enum[GameStatus] {
  val values: IndexedSeq[GameStatus] = findValues

  case object GameContinues extends GameStatus
  final case class Win(by: Side) extends GameStatus
  case object Draw extends GameStatus
}
