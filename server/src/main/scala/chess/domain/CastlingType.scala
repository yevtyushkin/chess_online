package com.chessonline
package chess.domain

import enumeratum._

/** Represents a castling move type. */
sealed trait CastlingType extends EnumEntry

/** Contains all [[CastlingType]] instances. */
object CastlingType extends Enum[CastlingType] {
  val values: IndexedSeq[CastlingType] = findValues

  case object WhiteQueenSide extends CastlingType
  case object WhiteKingSide extends CastlingType
  case object BlackQueenSide extends CastlingType
  case object BlackKingSide extends CastlingType
}
