package com.chessonline
package chess.domain

import chess.domain.MovePattern._
import chess.domain.MoveValidationError.InvalidMovePattern

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class MovePatternValidatorSpec extends AnyFreeSpec with EitherValues {
  import MovePatternValidator._
  import TestData._

  "MovePatternValidator" - {
    "validate" - {
      "for pawns" - {
        val attacksTestData = List(
          Move(whitePawn, from = e4, to = d5),
          Move(whitePawn, from = e4, to = f5),
          Move(blackPawn, from = e5, to = d4),
          Move(blackPawn, from = e5, to = f4)
        )

        val enPassantAttacksTestData: List[(Move, Coordinate)] =
          attacksTestData.zip(
            List(
              d4,
              f4,
              d5,
              f5
            )
          )

        "allows" - {
          "moves 1 square forward to an empty square" in {
            val whiteMove = Move(whitePawn, from = e2, to = e3)
            val blackMove = Move(blackPawn, from = e7, to = e6)

            validate(whiteMove, emptyGameState).value shouldEqual Transition
            validate(blackMove, emptyGameState).value shouldEqual Transition
          }

          "moves 2 squares forward move to an empty square from the starting coordinate if there are no barriers" in {
            val whiteMove = Move(whitePawn, from = e2, to = e4)
            val blackMove = Move(blackPawn, from = e7, to = e5)

            validate(whiteMove, emptyGameState).value shouldEqual Transition
            validate(blackMove, emptyGameState).value shouldEqual Transition
          }

          "regular attacks" in {
            val attacksGameState = emptyGameState.copy(
              board = Chessboard(
                Map(
                  d5 -> blackPawnSquare,
                  f5 -> blackPawnSquare,
                  d4 -> whitePawnSquare,
                  f4 -> whitePawnSquare
                )
              )
            )

            attacksTestData foreach { move =>
              validate(move, attacksGameState).value shouldEqual Attack(move.to)
            }
          }

          // uses impossible en passant coordinates for testing purposes
          "en passant attacks" in {
            enPassantAttacksTestData.foreach {
              case (move @ Move(_, _, to), expectedAttackedPieceCoordinate) =>
                validate(
                  move,
                  gameState = emptyGameState.copy(
                    enPassantSquareOption = Some(to)
                  )
                ).value shouldEqual Attack(expectedAttackedPieceCoordinate)
            }
          }
        }

        "denies moves" - {
          "forward to non-empty square" in {
            val whiteMove = Move(whitePawn, from = e2, to = e3)
            val blackMove = Move(blackPawn, from = e7, to = e5)

            val gameStateWithNonEmptySquares = emptyGameState.copy(
              board = Chessboard(
                Map(e3 -> blackPawnSquare, e5 -> blackPawnSquare)
              )
            )

            validate(
              whiteMove,
              gameStateWithNonEmptySquares
            ).left.value shouldEqual InvalidMovePattern

            validate(
              blackMove,
              gameStateWithNonEmptySquares
            ).left.value shouldEqual InvalidMovePattern
          }

          "moves 2 squares forward with barriers" in {
            val whiteMove = Move(whitePawn, from = e2, to = e4)
            val blackMove = Move(blackPawn, from = e7, to = e5)

            val gameStateWithBarriers = emptyGameState.copy(
              board = Chessboard(
                Map(e3 -> whitePawnSquare, e6 -> blackPawnSquare)
              )
            )

            validate(
              whiteMove,
              gameStateWithBarriers
            ).left.value shouldEqual InvalidMovePattern

            validate(
              blackMove,
              gameStateWithBarriers
            ).left.value shouldEqual InvalidMovePattern
          }

          "moves 2 squares forward from non-starting position" in {
            val whiteMove = Move(whitePawn, from = e3, to = e5)
            val blackMove = Move(blackPawn, from = e6, to = e4)

            validate(
              whiteMove,
              emptyGameState
            ).left.value shouldEqual InvalidMovePattern

            validate(
              blackMove,
              emptyGameState
            ).left.value shouldEqual InvalidMovePattern
          }

          "empty square attacks" in {
            attacksTestData.foreach { move =>
              validate(
                move,
                emptyGameState
              ).left.value shouldEqual InvalidMovePattern
            }
          }

          "ally piece attacks" in {
            val attackingAlliedPiecesGameState = emptyGameState.copy(
              board = Chessboard(
                Map(
                  d5 -> whitePawnSquare,
                  f5 -> whitePawnSquare,
                  d4 -> blackPawnSquare,
                  f4 -> blackPawnSquare
                )
              )
            )

            attacksTestData.foreach { move =>
              validate(
                move,
                attackingAlliedPiecesGameState
              ).left.value shouldEqual InvalidMovePattern
            }
          }
        }
      }
    }
  }
}
