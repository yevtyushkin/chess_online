package com.chessonline
package chess.domain

import enumeratum._

sealed trait MoveValidationError extends EnumEntry

object MoveValidationError extends Enum[MoveValidationError] {
  val values: IndexedSeq[MoveValidationError] = findValues

  case object AbsentOrWrongPieceAtStartingCoordinate extends MoveValidationError

  case object IdenticalStartAndDestinationCoordinates
      extends MoveValidationError

  case object WrongPieceColor extends MoveValidationError

  case object DestinationTakenByAllyPiece extends MoveValidationError

  case object InvalidMovePattern extends MoveValidationError

  case object KingNotSafeAfterMove extends MoveValidationError
}
