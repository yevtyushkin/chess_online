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
      def testValid(
          moves: Seq[Move],
          state: GameState,
          expected: MovePattern
      ): Unit = moves.foreach(
        validate(_, state).value shouldEqual expected
      )

      def testInvalid(
          moves: Seq[Move],
          state: GameState
      ): Unit = moves.foreach(
        validate(_, state).left.value shouldEqual InvalidMovePattern
      )

      "for pawns" - {
        val attackingMoves = List(
          Move(whitePawn, from = e4, to = d5),
          Move(whitePawn, from = e4, to = f5),
          Move(blackPawn, from = e5, to = d4),
          Move(blackPawn, from = e5, to = f4)
        )

        // Move -> en passant square coordinate
        val enPassantAttackMoves: List[(Move, Coordinate)] =
          attackingMoves.zip(
            List(
              d4,
              f4,
              d5,
              f5
            )
          )

        val twoSquaresForwardMoves = List(
          Move(whitePawn, from = e2, to = e4),
          Move(blackPawn, from = e7, to = e5)
        )

        "allows" - {
          "moves 1 square forward to an empty square" in {
            val moves = List(
              Move(whitePawn, from = e2, to = e3),
              Move(blackPawn, from = e7, to = e6)
            )

            testValid(
              moves = moves,
              state = emptyGameState,
              expected = Transition
            )
          }

          "moves 2 squares forward move to an empty square from the starting coordinate with no barriers" in {
            testValid(
              moves = twoSquaresForwardMoves,
              state = emptyGameState,
              expected = Transition
            )
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

            attackingMoves foreach { move =>
              validate(move, attacksGameState).value shouldEqual Attack(move.to)
            }
          }

          // uses impossible en passant coordinates for testing purposes
          "en passant attacks" in {
            enPassantAttackMoves.foreach {
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
            val invalidMoves = List(
              Move(whitePawn, from = e2, to = e3),
              Move(blackPawn, from = e7, to = e5)
            )

            val gameStateWithNonEmptySquares = emptyGameState.copy(
              board = Chessboard(
                Map(
                  e3 -> blackPawnSquare,
                  e5 -> blackPawnSquare
                )
              )
            )

            testInvalid(
              moves = invalidMoves,
              state = gameStateWithNonEmptySquares
            )
          }

          "moves 2 squares forward with barriers" in {
            val gameStateWithBarriers = emptyGameState.copy(
              board = Chessboard(
                Map(
                  e3 -> whitePawnSquare,
                  e6 -> blackPawnSquare
                )
              )
            )

            testInvalid(
              moves = twoSquaresForwardMoves,
              state = gameStateWithBarriers
            )
          }

          "moves 2 squares forward from non-starting position" in {
            val whiteMove = Move(whitePawn, from = e3, to = e5)
            val blackMove = Move(blackPawn, from = e6, to = e4)

            testInvalid(
              moves = List(whiteMove, blackMove),
              state = emptyGameState
            )
          }

          "empty square attacks" in {
            testInvalid(moves = attackingMoves, state = emptyGameState)
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

            // adds enemy pieces near the listed pawns
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

            testInvalid(
              moves = wrongMovesForWhite ::: wrongMovesForBlack,
              state = gameState
            )
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
            testValid(
              moves = kingMoves,
              state = emptyGameState,
              expected = Transition
            )
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
            testValid(
              moves = queenSideCastlings,
              state = allCastlingsAvailableState,
              expected = Castling(QueenSide)
            )
            testValid(
              moves = kingSideCastlings,
              state = allCastlingsAvailableState,
              expected = Castling(KingSide)
            )
          }
        }

        "denies" - {
          "moves for more that 1 square" in {
            val invalidMoves = List(
              Move(whiteKing, a1, a3),
              Move(whiteKing, a1, b3)
            )

            testInvalid(
              moves = invalidMoves,
              state = emptyGameState
            )
          }

          "castlings if not available" in {
            testInvalid(
              moves = queenSideCastlings ::: kingSideCastlings,
              state = emptyGameState
            )
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

            testInvalid(
              moves = queenSideCastlings ::: kingSideCastlings,
              state = castlingsWithBarriersState
            )
          }
        }
      }

      "for queen" - {
        val pawnAtA1State = emptyGameState.copy(
          board = Chessboard(
            Map(
              a1 -> whitePawnSquare
            )
          )
        )

        "allows" - {
          "vertical moves" in {
            val moves = List(
              Move(whiteQueen, b1, b8),
              Move(whiteQueen, c8, c1)
            )

            testValid(
              moves = moves,
              state = emptyGameState,
              expected = Transition
            )
          }

          "horizontal moves" in {
            val moves = List(
              Move(whiteQueen, b1, h1),
              Move(whiteQueen, h8, a8)
            )

            testValid(
              moves = moves,
              state = emptyGameState,
              expected = Transition
            )
          }

          "diagonal moves" in {
            val moves = List(
              Move(whiteQueen, a1, h8),
              Move(whiteQueen, h1, a8)
            )

            testValid(
              moves = moves,
              state = emptyGameState,
              expected = Transition
            )
          }

          "regular attacks" in {
            val moves = List(
              Move(whiteQueen, a8, a1),
              Move(whiteQueen, h1, a1),
              Move(whiteQueen, h8, a1)
            )

            testValid(
              moves = moves,
              state = pawnAtA1State,
              expected = Attack(a1)
            )
          }
        }

        "denies" - {
          "moves with invalid patterns" in {
            val invalidMoves = List(
              Move(whiteQueen, a1, b3),
              Move(whiteQueen, a2, h1)
            )

            testInvalid(moves = invalidMoves, state = emptyGameState)
          }
        }
      }
    }
  }
}
