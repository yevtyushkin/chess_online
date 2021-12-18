package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.GameStatus.GameContinues
import chess.domain.Side._
import chess.domain.TestData.a1

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

    "toFEN" - {
      def splitFEN(gameState: GameState) = gameState.toFEN.split(" ")

      "should return a string that starts with the board's FEN string" in {
        splitFEN(emptyGameState)(0) shouldEqual emptyGameState.board.toFEN
      }

      "should return a string with the correct active side" in {
        splitFEN(emptyGameState)(1) shouldEqual emptyGameState.movesNow.tag
      }

      "should return a string with the correct available" - {
        "when at least one castling is available" in {
          val state = emptyGameState.copy(
            castlingsForWhite = List(KingSide, QueenSide),
            castlingsForBlack = List(QueenSide)
          )

          splitFEN(state)(2) shouldEqual "KQq"
        }

        "when no castlings available" in {
          splitFEN(emptyGameState)(2) shouldEqual "-"
        }
      }

      "should return a string with correct en passant coordinate" - {
        "if there is an available en passant coordinate" in {
          val state = emptyGameState.copy(enPassantCoordinateOption = Some(a1))

          splitFEN(state)(3) shouldEqual "a1"
        }

        "when no coordinate is available" in {
          splitFEN(emptyGameState)(3) shouldEqual "-"
        }
      }

      "should return a string with correct half move number" in {
        splitFEN(emptyGameState)(4) shouldEqual
          s"${emptyGameState.halfMoveNumber}"
      }

      "should return a string with correct full move number" in {
        splitFEN(emptyGameState)(5) shouldEqual
          s"${emptyGameState.fullMoveNumber}"
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
