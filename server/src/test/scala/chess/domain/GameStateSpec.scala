package com.chessonline
package chess.domain

import chess.domain.Side.White

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class GameStateSpec extends AnyFreeSpec {
  "GameState" - {
    "initial" - {
      val initial = GameState.initial

      "has the initial chessboard state" in {
        initial.board shouldEqual Chessboard.initial
      }

      "has all available castlings" in {
        initial.castlingsAvailable shouldEqual Castling.values.toList
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
