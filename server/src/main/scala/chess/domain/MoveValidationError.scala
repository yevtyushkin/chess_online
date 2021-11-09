package com.chessonline
package chess.domain

import enumeratum._

sealed trait MoveValidationError extends EnumEntry

object MoveValidationError extends Enum[MoveValidationError] {
  val values: IndexedSeq[MoveValidationError] = findValues

  case object AbsentOrWrongPieceAtStartingCoordinate extends MoveValidationError

  case object IdenticalStartAndDestinationCoordinates
      extends MoveValidationError

  case object MoveNotInOrder extends MoveValidationError

  case object DestinationSquareTakenByAllyPiece extends MoveValidationError

  case object InvalidMovePattern extends MoveValidationError
}
