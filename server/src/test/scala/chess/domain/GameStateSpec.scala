package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.Side._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class GameStateSpec extends AnyFreeSpec {
  "GameState" - {
    import TestData.emptyGameState

    "castlingAvailable" - {
      val queenSideForWhite = List(QueenSide)
      val kingSideForBlack = List(KingSide)
      val gameState = emptyGameState.copy(
        castlingsForWhite = queenSideForWhite,
        castlingsForBlack = kingSideForBlack
      )

      "should return true if the castling type is available for the given side" - {
        gameState.castingAvailable(White, QueenSide) shouldBe true
        gameState.castingAvailable(Black, KingSide) shouldBe true
      }

      "should return false if the castling type isn't available for the given side" - {
        gameState.castingAvailable(White, KingSide) shouldBe false
        gameState.castingAvailable(Black, QueenSide) shouldBe false
      }
    }

    "initial" - {
      val initial = GameState.initial

      "has the initial chessboard state" in {
        initial.board shouldEqual Chessboard.initial
      }

      "has all available castlings for white side" in {
        initial.castlingsForWhite shouldEqual CastlingType.values.toList
      }

      "has all available castlings for black side" in {
        initial.castlingsForWhite shouldEqual CastlingType.values.toList
      }

      "has the white side to move now" in {
        initial.movesNow shouldEqual White
      }

      "doesn't have an en passant square" in {
        initial.enPassantSquareOption shouldEqual None
      }
    }
  }
}
