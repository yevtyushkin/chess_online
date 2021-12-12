package com.chessonline
package chess.domain

import enumeratum._

sealed abstract class PieceType(val tag: String) extends EnumEntry

object PieceType extends Enum[PieceType] {
  val values: IndexedSeq[PieceType] = findValues

  case object King extends PieceType("K")
  case object Queen extends PieceType("Q")
  case object Rook extends PieceType("R")
  case object Bishop extends PieceType("B")
  case object Knight extends PieceType("N")
  case object Pawn extends PieceType("P")
}
