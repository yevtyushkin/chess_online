package com.chessonline
package chess.domain

import chess.domain.MoveValidationError._

/** Responsible for validating [[Move]]s. */
object MoveValidator {

  type ErrorOrMove = Either[MoveValidationError, Move]

  /** Validates the given move using the provided game state.
    * @return either a [[MoveValidationError]] in the left channel, or the given [[Move]] in the right channel.
    */
  def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, Move] = {

    /** Returns the [[Left]] channel with the given `left` if the `test` is false. Otherwise, returns `move`. */
    def errorOrMoveFromTest(
        test: Boolean,
        left: => MoveValidationError
    ): ErrorOrMove = if (test) Right(move) else Left(left)

    /** Validates the given [[Move.piece]] is present at [[Move.from]]. */
    def validatePieceIsPresentAtStartingCoordinate: ErrorOrMove =
      errorOrMoveFromTest(
        test = gameState.board(move.from).contains(move.piece),
        left = AbsentOrWrongPieceAtStartingCoordinate
      )

    /** Validates the given [[Move.from]] is not equal to [[Move.to]]. */
    def validateStartAndDestinationCoordinates: ErrorOrMove =
      errorOrMoveFromTest(
        test = move.from != move.to,
        left = IdenticalStartAndDestinationCoordinates
      )

    /** Validates the given [[Move.piece]] has the same [[Side]] as [[GameState.movesNow]]. */
    def validateMoveOrder: ErrorOrMove =
      errorOrMoveFromTest(
        test = move.piece.side == gameState.movesNow,
        MoveNotInOrder
      )

    /** Validates that [[Move.to]] is not already taken by an allied [[Piece]]. */
    def validateDestinationNotTakenByAllyPiece: ErrorOrMove =
      errorOrMoveFromTest(
        test = gameState.board(move.to) match {
          case Some(piece) => piece.side != gameState.movesNow
          case _           => false
        },
        SquareTakenByAllyPiece
      )

    for {
      _ <- validatePieceIsPresentAtStartingCoordinate
      _ <- validateStartAndDestinationCoordinates
      _ <- validateMoveOrder
      v <- validateDestinationNotTakenByAllyPiece
    } yield v
  }

}
