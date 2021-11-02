package com.chessonline
package chess.domain

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

        "allows" - {
          "making a 1 square forward move to a free square" in {
            val whiteMove = Move(whitePawn, from = e2, to = e3)
            val blackMove = Move(blackPawn, from = e7, to = e6)

            validate(whiteMove, emptyGameState).value shouldEqual whiteMove
            validate(blackMove, emptyGameState).value shouldEqual blackMove
          }

          "making a 2 square forward move to a free square from the starting coordinate" in {
            val whiteMove = Move(whitePawn, from = e2, to = e4)
            val blackMove = Move(blackPawn, from = e7, to = e5)

            validate(whiteMove, emptyGameState).value shouldEqual whiteMove
            validate(blackMove, emptyGameState).value shouldEqual blackMove
          }

          "making a regular attack" in {
            val attackingMoves: List[(Move, Chessboard)] =
              attacksTestData.map { case move @ Move(piece, _, to) =>
                (
                  move,
                  Chessboard(
                    Map(
                      to -> Square(Some(piece.copy(side = piece.side.opposite)))
                    )
                  )
                )
              }

            attackingMoves foreach { case (move, board) =>
              validate(
                move,
                emptyGameState.copy(board = board)
              ).value shouldEqual move
            }
          }

          // here we use unreal en passant coordinates, but for this level we consider this valid
          "making en passant attack" in {
            attacksTestData.foreach { case move @ Move(_, _, to) =>
              validate(
                move,
                gameState = emptyGameState.copy(
                  enPassantSquareOption = Some(to)
                )
              ).value shouldEqual move
            }
          }
        }

        "denies moves" - {
          "to non-empty square" in {
            val whiteMove = Move(whitePawn, from = e2, to = e3)
            val blackMove = Move(blackPawn, from = e7, to = e5)

            validate(
              whiteMove,
              emptyGameState.copy(board =
                Chessboard(Map(e3 -> blackPawnSquare))
              )
            ).left.value shouldEqual InvalidMovePattern

            validate(
              blackMove,
              emptyGameState.copy(board =
                Chessboard(Map(e5 -> blackPawnSquare))
              )
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

          "attacking empty squares" in {
            attacksTestData.foreach(move =>
              validate(
                move,
                emptyGameState
              ).left.value shouldEqual InvalidMovePattern
            )
          }

          "attacking ally pieces" in {
            attacksTestData.foreach { case move @ Move(piece, _, to) =>
              MovePatternValidator
                .validate(
                  move,
                  gameState = emptyGameState.copy(board =
                    Chessboard(Map(to -> Square(Some(piece))))
                  )
                )
                .left
                .value shouldEqual InvalidMovePattern
            }
          }
        }
      }
    }
  }
}
