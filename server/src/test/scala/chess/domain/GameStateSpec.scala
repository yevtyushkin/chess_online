package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.GameStatus.GameContinues
import chess.domain.Side._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class GameStateSpec extends AnyFreeSpec {
  "GameState" - {
    import TestData.emptyGameState

    val castlingsForWhite = List(QueenSide)
    val castlingsForBlack = List(KingSide)

    "castlingAvailable" - {
      val gameState = emptyGameState.copy(
        castlingsForWhite = castlingsForWhite,
        castlingsForBlack = castlingsForBlack
      )

      "should return all available castlings for the side that moves now" in {
        gameState
          .copy(movesNow = White)
          .castingsAvailable shouldEqual castlingsForWhite
        gameState
          .copy(movesNow = Black)
          .castingsAvailable shouldEqual castlingsForBlack
      }
    }

    "updateCastlings" - {
      "returns new state with the updated castlings for the side that moves now" in {
        emptyGameState
          .copy(movesNow = White)
          .updateCastlings(castlingsForWhite)
          .castlingsForWhite shouldEqual castlingsForWhite

        emptyGameState
          .copy(movesNow = Black)
          .updateCastlings(castlingsForBlack)
          .castlingsForBlack shouldEqual castlingsForBlack
      }
    }

    "initial" - {
      val initial = GameState.initial

      "has the correct game status" in {
        initial.status shouldEqual GameContinues
      }

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
        initial.enPassantCoordinateOption shouldEqual None
      }
    }
  }
}
