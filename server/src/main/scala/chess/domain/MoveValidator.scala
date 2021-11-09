package com.chessonline
package chess.domain

import chess.domain.MoveValidationError._

trait MoveValidator {

  def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, Move]
}

object MoveValidator extends MoveValidator {

  type ErrorOrMove = Either[MoveValidationError, Move]

  override def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, Move] = {

    def testMove(
        test: Boolean,
        left: => MoveValidationError
    ): ErrorOrMove = Either.cond(test, move, left)

    def pieceIsPresentAtStartingCoordinate: ErrorOrMove =
      testMove(
        test = gameState.board.pieceAt(move.from).contains(move.piece),
        left = AbsentOrWrongPieceAtStartingCoordinate
      )

    def startAndDestinationCoordinatesDiffer: ErrorOrMove =
      testMove(
        test = move.from != move.to,
        left = IdenticalStartAndDestinationCoordinates
      )

    def moveIsInOrder: ErrorOrMove =
      testMove(
        test = move.piece.side == gameState.movesNow,
        left = MoveNotInOrder
      )

    def destinationNotTakenByAllyPiece: ErrorOrMove =
      testMove(
        test = gameState.board.pieceAt(move.to) match {
          case Some(piece) => piece.side != gameState.movesNow
          case _           => false
        },
        left = DestinationSquareTakenByAllyPiece
      )

    for {
      _ <- pieceIsPresentAtStartingCoordinate
      _ <- startAndDestinationCoordinatesDiffer
      _ <- moveIsInOrder
      v <- destinationNotTakenByAllyPiece
    } yield v
  }

}
