package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.MovePattern._
import chess.domain.MoveValidationError.KingNotSafeAfterMove
import chess.domain.ValidateMove.ErrorOr
import chess.domain.PieceType._
import chess.domain.Side._

trait EvaluateMove {
  def apply(move: Move, gameState: GameState): ErrorOr[GameState]
}

object EvaluateMove {
  def apply(validateMove: ValidateMove, kingIsSafe: KingIsSafe): EvaluateMove =
    new EvaluateMove {
      override def apply(
          move: Move,
          gameState: GameState
      ): ErrorOr[GameState] = {
        for {
          pattern <- validateMove(move, gameState)
          newState = updateState(move, pattern, gameState)
          _ <- Either.cond(
            test =
              kingIsSafe(forSide = gameState.movesNow, gameState = newState),
            left = KingNotSafeAfterMove,
            right = newState
          )

        } yield newState
      }

      private def updateState(
          move: Move,
          movePattern: MovePattern,
          gameState: GameState
      ): GameState = {
        val newCastlings =
          move.piece.pieceType match {
            case King => Nil
            case Rook =>
              val isFirstRooksMove =
                (gameState.movesNow == White && move.from.rank == `1`) ||
                  (gameState.movesNow == Black && move.from.rank == `8`)

              move.from.file match {
                case A if isFirstRooksMove =>
                  gameState.castingsAvailable.filter(_ != QueenSide)
                case H if isFirstRooksMove =>
                  gameState.castingsAvailable.filter(_ != KingSide)
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
    }
}
