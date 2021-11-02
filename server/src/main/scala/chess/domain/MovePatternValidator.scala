package com.chessonline
package chess.domain

import chess.domain.CoordinateRank._
import chess.domain.MoveValidationError.InvalidMovePattern
import chess.domain.PieceType._
import chess.domain.Side._

/** Responsible for validating [[Move]] patterns. */
trait MovePatternValidator {

  /** Validates the given [[Move]] pattern using the given [[GameState]].
    * @param move the [[Move]] to validate.
    * @param gameState the current [[GameState]].
    * @return [[MoveValidationError.InvalidMovePattern]] if the [[Move]] pattern is not valid, or the valid [[Move]] otherwise.
    */
  def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, Move]
}

/** The default [[MovePatternValidator]] implementation. */
object MovePatternValidator extends MovePatternValidator {
  override def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, Move] = {

    /** Returns file and rank distance between the given [[Move.to]] and [[Move.from]]. */
    def moveDistance: (Int, Int) = {
      val fileIndex: CoordinateFile => Int = CoordinateFile.indexOf
      val rankIndex: CoordinateRank => Int = CoordinateRank.indexOf

      (
        fileIndex(move.to.file) - fileIndex(move.from.file),
        rankIndex(move.to.rank) - rankIndex(move.from.rank)
      )
    }

    /** Validates the move pattern for [[Pawn]]. */
    def validatePawnMovePattern: Either[MoveValidationError, Move] = {
      val destinationIsEmpty = gameState.board(move.to).isEmpty
      val isFirstMove = List(`2`, `7`).contains(move.from.rank)

      val side = move.piece.side
      val isEnPassantAttack = gameState.enPassantSquareOption.contains(move.to)
      val isRegularAttack = gameState.board(move.to) match {
        case Some(attackedPiece) => side != attackedPiece.side
        case _                   => false
      }

      val absoluteMoveDistance = moveDistance match {
        case (fileDelta, rankDelta) =>
          val newRankDelta = if (side == White) rankDelta else -rankDelta
          (fileDelta, newRankDelta)
      }

      absoluteMoveDistance match {
        case (0, 1) if destinationIsEmpty                => Right(move)
        case (0, 2) if isFirstMove && destinationIsEmpty => Right(move)
        case (-1, 1) | (1, 1) if isRegularAttack || isEnPassantAttack =>
          Right(move)
        case _ => Left(InvalidMovePattern)
      }
    }

    move.piece.pieceType match {
      case Pawn => validatePawnMovePattern
      case _    => Left(InvalidMovePattern)
//        case PieceType.King => ???
//        case PieceType.Queen => ???
//        case PieceType.Rook => ???
//        case PieceType.Bishop => ???
//        case PieceType.Knight => ???
    }
  }

}
