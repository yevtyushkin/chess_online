package com.chessonline
package chess.domain

import chess.domain.MovePattern.Transition
import chess.domain.MoveValidationError.InvalidMovePattern
import chess.domain.Side.White

import cats.implicits.catsSyntaxEitherId
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class KingIsSafeSpec extends AnyFreeSpec with MockFactory {
  "KingIsSafe" - {
    import TestData._

    val validateMove = stub[ValidateMove]
    val kingIsSafe = KingIsSafe(validateMove)

    val state = emptyGameState.copy(
      board = Chessboard(Map(a1 -> whiteKing, a8 -> blackPawn, b7 -> blackPawn))
    )
    val defaultMove = Move(blackPawn, a8, a1)

    "apply" - {
      "returns true" - {
        // for coverage only
        "if the king is not present on board" in {
          kingIsSafe(
            forSide = White,
            gameState = emptyGameState
          ) shouldEqual true
        }

        "if the king can't be attacked by any enemy piece" in {
          validateMove.apply _ when where { (move, _) =>
            move.piece.side == state.movesNow.opposite
          } returns InvalidMovePattern.asLeft

          kingIsSafe(
            forSide = White,
            gameState = state
          ) shouldEqual true
        }
      }

      "returns false" - {
        "if the king can be attacked by an enemy piece" in {
          validateMove.apply _ when where { (move, _) =>
            move.piece.side == state.movesNow.opposite
          } returns Transition().asRight

          kingIsSafe(
            forSide = White,
            gameState = state
          ) shouldEqual false
        }
      }
    }
  }
}
