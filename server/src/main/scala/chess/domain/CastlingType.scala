package com.chessonline
package chess.domain

import enumeratum._

sealed abstract class CastlingType(val tag: String) extends EnumEntry

object CastlingType extends Enum[CastlingType] {
  val values: IndexedSeq[CastlingType] = findValues

  case object QueenSide extends CastlingType("q")
  case object KingSide extends CastlingType("k")
}
