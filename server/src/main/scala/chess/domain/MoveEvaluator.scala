package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.CoordinateFile._
import chess.domain.MovePattern._
import chess.domain.MoveValidationError.KingNotSafeAfterMove
import chess.domain.MoveValidator.ErrorOr
import chess.domain.PieceType._

trait MoveEvaluator {

  def updateState(
      move: Move,
      movePattern: MovePattern,
      gameState: GameState
  ): GameState

  def kingIsSafe(forSide: Side, gameState: GameState)(
      moveValidator: MoveValidator
  ): Boolean

  def evaluate(move: Move, gameState: GameState)(
      moveValidator: MoveValidator
  ): ErrorOr[GameState] = {
    for {
      pattern <- moveValidator.validate(move, gameState).map {
        case (_, pattern) => pattern
      }
      newState = updateState(move, pattern, gameState)
      _ <- Either.cond(
        test = kingIsSafe(forSide = gameState.movesNow, gameState = newState)(
          moveValidator
        ),
        left = KingNotSafeAfterMove,
        right = newState
      )
    } yield newState
  }
}

object MoveEvaluator extends MoveEvaluator {
  override def updateState(
      move: Move,
      movePattern: MovePattern,
      gameState: GameState
  ): GameState = {
    val newCastlings =
      move.piece.pieceType match {
        case King => Nil
        case Rook =>
          move.from.file match {
            case A => gameState.castingsAvailable.filter(_ != QueenSide)
            case H => gameState.castingsAvailable.filter(_ != KingSide)
            case _ => gameState.castingsAvailable
          }
        case _ => gameState.castingsAvailable
      }
    val movesNext = gameState.movesNow.opposite
    val updatedState =
      gameState
        .updateCastlings(newCastlings)
        .copy(movesNow = movesNext, enPassantCoordinateOption = None)

    val updatedSquares =
      gameState.board.pieceMap - move.from + (move.to -> move.piece)
    movePattern match {
      case Transition(enPassantCoordinateOption) =>
        updatedState.copy(
          board = Chessboard(updatedSquares),
          enPassantCoordinateOption = enPassantCoordinateOption
        )

      case Attack(attackedCoordinate) =>
        updatedState.copy(
          board = Chessboard(
            if (attackedCoordinate != move.to)
              updatedSquares - attackedCoordinate
            else updatedSquares
          )
        )

      case Castling(castlingType) =>
        val (rookStartFile, rookDestinationFile) = castlingType match {
          case QueenSide => (A, D)
          case KingSide  => (H, F)
        }
        val rookRank = move.from.rank
        val rookStartCoordinate = Coordinate(rookStartFile, rookRank)
        val rookDestinationCoordinate =
          Coordinate(rookDestinationFile, rookRank)

        updatedState.copy(
          board = Chessboard(
            updatedSquares
              .get(rookStartCoordinate)
              .fold(ifEmpty = updatedSquares)(rook =>
                updatedSquares - rookStartCoordinate + (rookDestinationCoordinate -> rook)
              )
          )
        )
    }
  }

  override def kingIsSafe(kingSide: Side, gameState: GameState)(
      moveValidator: MoveValidator
  ): Boolean = {
    val pieceMap = gameState.board.pieceMap
    (for {
      kingCoordinate <- pieceMap.collectFirst {
        case (coordinate, Piece(side, King)) if side == kingSide => coordinate
      }

      enemySide = kingSide.opposite
      kingIsSafe = pieceMap
        .filter { case (_, piece) => piece.side == enemySide }
        .forall { case (coordinate, piece) =>
          moveValidator
            .validatePattern(
              Move(piece, from = coordinate, to = kingCoordinate),
              gameState
            )
            .isLeft
        }
    } yield kingIsSafe).getOrElse(true)
  }
}
