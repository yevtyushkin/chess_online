package com.chessonline
package chess.domain

import enumeratum._

/** Represents an error occurred during [[Move]] validation. */
sealed trait MoveValidationError extends EnumEntry

/** Contains all [[MoveValidationError]] instances. */
object MoveValidationError extends Enum[MoveValidationError] {
  val values: IndexedSeq[MoveValidationError] = findValues

  /** When there is a wrong [[Piece]] at [[Move.from]] or the piece is absent. */
  case object AbsentOrWrongPieceAtStartingCoordinate extends MoveValidationError

  /** When the [[Move.from]] is equal [[Move.to]]. */
  case object IdenticalStartAndDestinationCoordinates
      extends MoveValidationError

  /** When the [[Move.piece.side]] is not equal to [[GameState.movesNow]]. */
  case object MoveNotInOrder extends MoveValidationError

  /** When the [[Move.to]] is already taken by the same sided [[Piece]]. */
  case object SquareTakenByAllyPiece extends MoveValidationError

  /** When the [[Move.from]], [[Move.to]] and [[Move.piece]] form up the invalid chess move pattern. */
  case object InvalidMovePattern extends MoveValidationError
}
