package com.chessonline
package chess.domain

import enumeratum._

sealed trait CastlingType extends EnumEntry

object CastlingType extends Enum[CastlingType] {
  val values: IndexedSeq[CastlingType] = findValues

  case object QueenSide extends CastlingType
  case object KingSide extends CastlingType
}
