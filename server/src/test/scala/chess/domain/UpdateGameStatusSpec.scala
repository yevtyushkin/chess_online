package com.chessonline
package chess.domain

import chess.domain.GameStatus.{Draw, GameContinues, Win}
import chess.domain.MoveValidationError.InvalidMovePattern

import cats.implicits.catsSyntaxEitherId
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class UpdateGameStatusSpec extends AnyFreeSpec with MockFactory {
  "UpdateGameStatus" - {
    val evaluateMoveStub = stub[EvaluateMove]
    val kingIsSafeStub = stub[KingIsSafe]
    val updateGameStatus =
      UpdateGameStatus.apply(evaluateMoveStub, kingIsSafeStub)
    val initialGameState = GameState.initial

    "apply" - {
      "returns GameContinues if it is possible to perform a move and the king is not under check" in {
        test(
          isKingChecked = false,
          canPerformMove = true
        ) shouldEqual GameContinues
      }

      "returns GameContinues if it is possible to perform a move and the king is under check" in {
        test(
          isKingChecked = true,
          canPerformMove = true
        ) shouldEqual GameContinues
      }

      "returns Won with the winner side if it's not possible to perform a move and the king is under check" in {
        test(
          isKingChecked = true,
          canPerformMove = false,
          gameState = initialGameState
        ) shouldEqual Win(by = initialGameState.movesNow.opposite)
      }

      "returns Draw if it's not possible to perform a move and the king is not checked" in {
        test(
          isKingChecked = false,
          canPerformMove = false,
          gameState = initialGameState
        ) shouldEqual Draw
      }
    }

    def test(
        isKingChecked: Boolean,
        canPerformMove: Boolean,
        gameState: GameState = initialGameState
    ): GameStatus = {
      kingIsSafeStub.apply _ when (gameState.movesNow, gameState) returns !isKingChecked

      evaluateMoveStub.apply _ when (*, gameState) returns (if (canPerformMove)
                                                              gameState.asRight
                                                            else
                                                              InvalidMovePattern.asLeft)

      updateGameStatus(gameState)
    }
  }
}
