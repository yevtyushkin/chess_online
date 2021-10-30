package com.chessonline
package chess.domain

import chess.domain.PieceType._

import enumeratum._

/** Represents a type of a castling move in the chess game. */
sealed trait CastlingType extends EnumEntry

/** Contains all [[CastlingType]] instances. */
object CastlingType extends Enum[CastlingType] {
  val values: IndexedSeq[CastlingType] = findValues

  /** Represents a castling made to the direction of a [[Queen]] (also known as a long castling). */
  case object QueenSide extends CastlingType

  /** Represents a castling made to the direction of a [[King]] (also known as a short castling). */
  case object KingSide extends CastlingType
}
