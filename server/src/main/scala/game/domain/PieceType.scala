package com.chessonline
package game.domain

import enumeratum._

/** Represents a type of a [[Piece]] */
sealed trait PieceType extends EnumEntry

/** Contains all [[Piece]] instances. */
object PieceType extends Enum[PieceType] {
  val values: IndexedSeq[PieceType] = findValues

  case object King extends PieceType
  case object Queen extends PieceType
  case object Rook extends PieceType
  case object Bishop extends PieceType
  case object Knight extends PieceType
  case object Pawn extends PieceType
}
