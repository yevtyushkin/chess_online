package com.chessonline
package chess.domain

import chess.domain.GameStatus.{Draw, GameContinues, Win}

trait UpdateGameStatus {
  def apply(gameState: GameState): GameStatus
}

object UpdateGameStatus {
  def apply(
      evaluateMove: EvaluateMove,
      kingIsSafe: KingIsSafe
  ): UpdateGameStatus =
    (gameState: GameState) => {
      val side = gameState.movesNow

      val isKingChecked = !kingIsSafe(forSide = side, gameState)

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

              evaluateMove(
                possibleMove,
                gameState
              ).isRight
            }
          }
        }

      if (canPerformAtLeastOneMove) GameContinues
      else if (isKingChecked) Win(by = side.opposite)
      else Draw
    }
}
