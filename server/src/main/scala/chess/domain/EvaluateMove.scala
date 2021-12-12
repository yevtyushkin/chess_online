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

import com.chessonline.chess.domain.GameStatus._

trait EvaluateMove {
  def apply(move: Move, gameState: GameState): ErrorOr[GameState]
}

object EvaluateMove {
  def apply(
      validateMove: ValidateMove,
      kingIsSafe: KingIsSafe
  ): EvaluateMove =
    new EvaluateMove {
      override def apply(
          move: Move,
          gameState: GameState
      ): ErrorOr[GameState] = {
        for {
          stateAfterMove <- validateAndEvaluate(move, gameState)
          stateWithUpdatedStatus = updateGameStatus(stateAfterMove)
        } yield stateWithUpdatedStatus
      }

      private def updateGameStatus(gameState: GameState): GameState = {
        val side = gameState.movesNow
        val isKingChecked = !kingIsSafe(forSide = side, gameState)
        println(isKingChecked)

        // Not performant. TODO: rethink this in future.
        // Worst (at least real) case of such lookup is ~ 16 pieces * 63 destination squares.
        val canPerformAtLeastOneMove = gameState.board.pieceMap
          .exists { case (from, piece) =>
            piece.side == side && {
              val destinationCoordinates = for {
                file <- CoordinateFile.values
                rank <- CoordinateRank.values
                if !(from.file == file && from.rank == rank)
              } yield Coordinate(file, rank)

              destinationCoordinates.exists { destinationCoordinate =>
                val possibleMove = Move(piece, from, destinationCoordinate)

                validateAndEvaluate(
                  possibleMove,
                  gameState
                ).isRight
              }
            }
          }

        val newStatus =
          if (canPerformAtLeastOneMove) GameContinues
          else if (isKingChecked) Win(by = side.opposite)
          else Draw

        gameState.copy(status = newStatus)
      }

      private def validateAndEvaluate(
          move: Move,
          gameState: GameState
      ): ErrorOr[GameState] = for {
        pattern <- validateMove(move, gameState)
        stateAfterMove = updateState(move, pattern, gameState)
        _ <- Either.cond(
          test = kingIsSafe(
            forSide = gameState.movesNow,
            gameState = stateAfterMove
          ),
          left = KingNotSafeAfterMove,
          right = stateAfterMove
        )
      } yield stateAfterMove

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
