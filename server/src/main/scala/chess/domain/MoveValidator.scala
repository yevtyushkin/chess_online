package com.chessonline
package chess.domain

import chess.domain.MoveValidationError._
import chess.domain.PieceType._

/** Responsible for validating [[Move]]s. */
trait MoveValidator {

  /** Validates the given move using the provided [[Move]] and [[GameState]].
    * @param move the [[Move]] to validate.
    * @param gameState the current [[GameState]].
    * @return [[MoveValidationError]] if the [[Move]] is not valid, or the valid [[Move]] otherwise.
    */
  def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, Move]
}

/** The default [[MoveValidator]] implementation. */
object MoveValidator extends MoveValidator {

  type ErrorOrMove = Either[MoveValidationError, Move]

  override def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, Move] = {

    /** Returns the [[Left]] channel with the given `left` if the `test` is false. Otherwise, returns `move`. */
    def testMove(
        test: Boolean,
        left: => MoveValidationError
    ): ErrorOrMove = if (test) Right(move) else Left(left)

    /** Validates the given [[Move.piece]] is present at [[Move.from]]. */
    def validatePieceIsPresentAtStartingCoordinate: ErrorOrMove =
      testMove(
        test = gameState.board(move.from).contains(move.piece),
        left = AbsentOrWrongPieceAtStartingCoordinate
      )

    /** Validates the given [[Move.from]] is not equal to [[Move.to]]. */
    def validateStartAndDestinationCoordinates: ErrorOrMove =
      testMove(
        test = move.from != move.to,
        left = IdenticalStartAndDestinationCoordinates
      )

    /** Validates the given [[Move.piece]] has the same [[Side]] as [[GameState.movesNow]]. */
    def validateMoveOrder: ErrorOrMove =
      testMove(
        test = move.piece.side == gameState.movesNow,
        left = MoveNotInOrder
      )

    /** Validates that [[Move.to]] is not already taken by an allied [[Piece]]. */
    def validateDestinationNotTakenByAllyPiece: ErrorOrMove =
      testMove(
        test = gameState.board(move.to) match {
          case Some(piece) => piece.side != gameState.movesNow
          case _           => false
        },
        left = SquareTakenByAllyPiece
      )

    /** Validates the given [[Move.from]], [[Move.to]] and [[Move.piece]] form a correct chess move pattern. */
    def validateMovePattern: ErrorOrMove = ???

    /** Validates the given [[Move]] pattern is available, e.g., castling is available. */
    def validateSpecialMoveAvailability: ErrorOrMove = ???

    /** Validates the given [[Move]] does not leave or put the ally [[King]] under check. */
    def validateKingIsNotInDanger: ErrorOrMove = ???

    // wrong move pattern
    // pattern is correct but the path is blocked (rook, bishop, queen) // merge with ^?
    // castling or pawn double-forward move is available
    // move leaves/makes the king under check
    //

    for {
      _ <- validatePieceIsPresentAtStartingCoordinate
      _ <- validateStartAndDestinationCoordinates
      _ <- validateMoveOrder
      v <- validateDestinationNotTakenByAllyPiece
    } yield v
  }

}
