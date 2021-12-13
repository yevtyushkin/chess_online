package com.chessonline
package chess.domain

import cats.Show
import enumeratum._

sealed trait MoveValidationError extends EnumEntry

object MoveValidationError extends Enum[MoveValidationError] {
  val values: IndexedSeq[MoveValidationError] = findValues

  case object NoPieceAtStartingCoordinate extends MoveValidationError

  case object IdenticalStartAndDestinationCoordinates
      extends MoveValidationError

  case object WrongPieceColor extends MoveValidationError

  case object DestinationTakenByAllyPiece extends MoveValidationError

  case object InvalidMovePattern extends MoveValidationError

  case object KingNotSafeAfterMove extends MoveValidationError

  implicit val showMoveValidationError: Show[MoveValidationError] = {
    case NoPieceAtStartingCoordinate => "No piece at starting coordinate"
    case IdenticalStartAndDestinationCoordinates =>
      "Move's start and destination should differ"
    case WrongPieceColor => "Wrong piece color"
    case DestinationTakenByAllyPiece =>
      "Destination is taken by and ally piece"
    case InvalidMovePattern   => "Invalid move pattern"
    case KingNotSafeAfterMove => "King is not safe after move"
  }
}
