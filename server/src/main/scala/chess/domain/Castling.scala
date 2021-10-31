package com.chessonline
package chess.domain

import enumeratum._

/** Represents a castling move. */
sealed trait Castling extends EnumEntry

/** Contains all [[Castling]] instances. */
object Castling extends Enum[Castling] {
  val values: IndexedSeq[Castling] = findValues

  case object WhiteQueenSide extends Castling
  case object WhiteKingSide extends Castling
  case object BlackQueenSide extends Castling
  case object BlackKingSide extends Castling
}
