package com.chessonline
package chess.domain

import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.MoveValidationError._
import chess.domain.PieceType._
import chess.domain.Side._

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class MoveValidatorSpec extends AnyFreeSpec with EitherValues {

  "MoveValidator" - {
    "validate" - {
      val whitePawn = TestUtils.createPiece(White, Pawn)
      val blackPawn = TestUtils.createPiece(Black, Pawn)

      val squareWithWhitePawn = TestUtils.createSquare(Some(whitePawn))
      val squareWithBlackPawn = TestUtils.createSquare(Some(blackPawn))

      val a2 = TestUtils.createCoordinate(A, `2`)
      val a3 = TestUtils.createCoordinate(A, `3`)
      val b2 = TestUtils.createCoordinate(B, `2`)

      val whitePawnAtA2 = TestUtils.createGameState(
        board = Chessboard(Map(a2 -> squareWithWhitePawn))
      )
      val emptyState = TestUtils.createGameState()

      import MoveValidator._

      "returns an error" - {
        "when there is no piece at the starting coordinate" in {
          validate(
            move = Move(whitePawn, a2, a3),
            gameState = emptyState
          ).left.value shouldEqual AbsentOrWrongPieceAtStartingCoordinate
        }

        "when there is a wrong piece at the starting coordinate" in {
          validate(
            move = Move(blackPawn, a2, a3),
            gameState = whitePawnAtA2
          ).left.value shouldEqual AbsentOrWrongPieceAtStartingCoordinate
        }

        "when the starting coordinate is equal to the destination coordinate" in {
          validate(
            move = Move(whitePawn, a2, a2),
            gameState = whitePawnAtA2
          ).left.value shouldEqual IdenticalStartAndDestinationCoordinates
        }

        "when the side of the piece under move is not equal to the side that makes a move" in {
          val blackMovesNow = whitePawnAtA2.copy(movesNow = Black)

          validate(
            move = Move(whitePawn, a2, b2),
            gameState = blackMovesNow
          ).left.value shouldEqual MoveNotInOrder
        }

        "when the destination square is already taken by an allied piece" in {
          val whitePawnAtA3 = whitePawnAtA2.copy(
            board = Chessboard(
              whitePawnAtA2.board.squares + (a3 -> Square(Some(whitePawn)))
            )
          )

          validate(
            move = Move(whitePawn, a2, a3),
            gameState = whitePawnAtA3
          ).left.value shouldEqual SquareTakenByAllyPiece
        }
      }
    }
  }
}
