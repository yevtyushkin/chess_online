package com.chessonline
package chess.domain

import chess.domain.CastlingType.{KingSide, QueenSide}
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

        // Move -> en passant square coordinate
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

          "moves 2 squares forward move to an empty square from the starting coordinate with no barriers" in {
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

        "denies" - {
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

          "moves in the wrong direction" in {
            val wrongMovesForWhite = List(
              Move(whitePawn, b2, b1),
              Move(whitePawn, b2, a1),
              Move(whitePawn, b2, c1)
            )
            val wrongMovesForBlack = List(
              Move(blackPawn, b7, b8),
              Move(blackPawn, b7, a8),
              Move(blackPawn, b7, c8)
            )
            val gameState = emptyGameState.copy(board =
              Chessboard(
                Map(
                  a1 -> blackPawnSquare,
                  c1 -> blackPawnSquare,
                  a8 -> whitePawnSquare,
                  c8 -> whitePawnSquare
                )
              )
            )

            (wrongMovesForWhite ::: wrongMovesForBlack) foreach { move =>
              validate(
                move,
                gameState
              ).left.value shouldEqual InvalidMovePattern
            }
          }
        }
      }

      "for kings" - {
        val kingMoves = List(
          Move(whiteKing, e3, e4), // forward
          Move(whiteKing, e3, e2), // back
          Move(whiteKing, e3, d3), // left
          Move(whiteKing, e3, f3), // right
          Move(whiteKing, e3, d4), // left-forward
          Move(whiteKing, e3, f4), // right-forward
          Move(whiteKing, e3, d3), // left-back
          Move(whiteKing, e3, f3) // right-back
        )
        val kingSurroundedByPawnsState = emptyGameState.copy(
          board = Chessboard(
            kingMoves.map(move => move.to -> blackPawnSquare).toMap
          )
        )

        val queenSideCastlings = List(
          Move(whiteKing, e1, c1),
          Move(blackKing, e8, c8)
        )
        val kingSideCastlings = List(
          Move(whiteKing, e1, g1),
          Move(blackKing, e8, g8)
        )
        val allCastlingsAvailableState = emptyGameState.copy(
          castlingsForWhite = CastlingType.values.toList,
          castlingsForBlack = CastlingType.values.toList
        )

        "allows" - {
          "moves by 1 square to an empty square" in {
            kingMoves.foreach { move =>
              validate(move, emptyGameState).value shouldEqual Transition
            }
          }

          "regular attacks" in {
            kingMoves.foreach { move =>
              validate(
                move,
                kingSurroundedByPawnsState
              ).value shouldEqual Attack(move.to)
            }
          }

          // we assume that if the castling is available then the rook is placed correctly
          // moving the rook or etc. updates castling availabilities for both sides
          "castlings if available" in {
            queenSideCastlings.foreach { move =>
              validate(
                move,
                allCastlingsAvailableState
              ).value shouldEqual Castling(QueenSide)
            }
            kingSideCastlings.foreach { move =>
              validate(
                move,
                allCastlingsAvailableState
              ).value shouldEqual Castling(KingSide)
            }
          }
        }

        "denies" - {
          "moves for more that 1 square" in {
            val invalidMove = Move(whiteKing, a1, a3)
            val invalidMove2 = Move(whiteKing, a1, b3)

            validate(
              invalidMove,
              emptyGameState
            ).left.value shouldEqual InvalidMovePattern
            validate(
              invalidMove2,
              emptyGameState
            ).left.value shouldEqual InvalidMovePattern
          }

          "castlings if not available" in {
            queenSideCastlings ::: kingSideCastlings foreach { move =>
              validate(
                move,
                emptyGameState
              ).left.value shouldEqual InvalidMovePattern
            }
          }

          "castlings if there are barriers" in {
            val castlingsWithBarriersState = allCastlingsAvailableState.copy(
              board = Chessboard(
                Map(
                  b1 -> whitePawnSquare,
                  d8 -> whitePawnSquare,
                  f1 -> whitePawnSquare,
                  f8 -> whitePawnSquare
                )
              )
            )

            queenSideCastlings ::: kingSideCastlings foreach { move =>
              validate(
                move,
                castlingsWithBarriersState
              ).left.value shouldEqual InvalidMovePattern
            }
          }
        }
      }
    }
  }
}
