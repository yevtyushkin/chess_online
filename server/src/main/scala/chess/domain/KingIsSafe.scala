package com.chessonline
package chess.domain

import chess.domain.PieceType.King

trait KingIsSafe {
  def apply(forSide: Side, gameState: GameState): Boolean
}

object KingIsSafe {
  def apply(validateMove: ValidateMove): KingIsSafe =
    (forSide: Side, gameState: GameState) => {
      val pieceMap = gameState.board.pieceMap

      (for {
        kingCoordinate <- pieceMap.collectFirst {
          case (coordinate, Piece(side, King)) if side == forSide => coordinate
        }

        enemySide = forSide.opposite

        kingIsSafe = pieceMap
          .filter { case (_, piece) => piece.side == enemySide }
          .forall { case (coordinate, _) =>
            validateMove(
              Move(from = coordinate, to = kingCoordinate),
              gameState.copy(movesNow = enemySide)
            ).isLeft
          }
      } yield kingIsSafe).getOrElse(true)
    }
}
