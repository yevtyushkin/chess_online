package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.MovePattern._
import chess.domain.MoveValidationError._
import chess.domain.ValidateMove.ErrorOr
import chess.domain.Side._

import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class ValidateMoveSpec extends AnyFreeSpec with EitherValues {
  "MoveValidator" - {
    import TestData._

    val validateMove = ValidateMove.apply

    "validate" - {
      "returns an error" - {
        "if the piece color is wrong" in {
          validateMove(
            Move(whitePawn, a2, a4),
            emptyGameState.copy(movesNow = Black)
          ).left.value shouldEqual WrongPieceColor
        }

        "if the piece is not present at starting coordinate" in {
          validateMove(
            Move(whitePawn, a2, a4),
            emptyGameState
          ).left.value shouldEqual AbsentOrWrongPieceAtStartingCoordinate
        }

        "if there's a wrong piece at the starting coordinate" in {
          validateMove(
            Move(whitePawn, a2, a4),
            emptyGameState.copy(board = Chessboard(Map(a2 -> whiteKing)))
          ).left.value shouldEqual AbsentOrWrongPieceAtStartingCoordinate
        }

        "if the starting coordinate is equal to the destination coordinate" in {
          validateMove(
            Move(whitePawn, a2, a2),
            emptyGameState.copy(board = Chessboard(Map(a2 -> whitePawn)))
          ).left.value shouldEqual IdenticalStartAndDestinationCoordinates
        }

        "if the destination coordinate is taken by an ally piece" in {
          validateMove(
            Move(whitePawn, a2, a4),
            emptyGameState.copy(board =
              Chessboard(Map(a2 -> whitePawn, a4 -> whiteKing))
            )
          ).left.value shouldEqual DestinationTakenByAllyPiece
        }
      }

      "for pawn moves" - {
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

        val oneSquareForwardMoves = List(
          Move(whitePawn, from = e2, to = e3),
          Move(blackPawn, from = e7, to = e6)
        )

        val twoSquaresForwardMoves = List(
          Move(whitePawn, from = e2, to = e4),
          Move(blackPawn, from = e7, to = e5)
        )

        val expectedEnPassantCoordinates = List(e3, e6)

        "allows" - {
          "moves 1 square forward to an empty square" in {
            testValidPattern(
              moves = oneSquareForwardMoves,
              expected = Transition()
            )
          }

          "moves 2 squares forward move to an empty square from the starting coordinate with no barriers" in {
            twoSquaresForwardMoves.zip(expectedEnPassantCoordinates).foreach {
              case (move, expectedEnPassantCoordinate) =>
                validateMove(
                  move,
                  createStateForPatternValidation(move)
                ).value shouldEqual
                  Transition(enPassantCoordinateOption =
                    Some(expectedEnPassantCoordinate)
                  )
            }
          }

          "regular attacks" in {
            val enemyPieces = Map(
              d5 -> blackPawn,
              f5 -> blackPawn,
              d4 -> whitePawn,
              f4 -> whitePawn
            )

            testByPredicate(
              attackingMoves,
              move => Attack(move.to).asRight,
              additionalPieces = enemyPieces
            )
          }

          "en passant attacks" in {
            enPassantAttackMoves.foreach {
              case (move @ Move(_, _, to), expectedAttackedPieceCoordinate) =>
                validateMove(
                  move,
                  createStateForPatternValidation(
                    move,
                    enPassantCoordinateOption = to.some
                  )
                ).value shouldEqual Attack(expectedAttackedPieceCoordinate)
            }
          }
        }

        "does not allow" - {
          "moves forward to non-empty square" in {
            val invalidMoves = List(
              Move(whitePawn, from = e2, to = e3),
              Move(blackPawn, from = e7, to = e5)
            )

            testInvalidPattern(
              moves = invalidMoves,
              additionalPieces = Map(e3 -> blackKing, e5 -> whiteKing)
            )
          }

          "moves 2 squares forward with barriers" in {
            testInvalidPattern(
              moves = twoSquaresForwardMoves,
              additionalPieces = Map(
                e3 -> blackKing,
                e6 -> whiteKing
              )
            )
          }

          "moves 2 squares forward from non-starting position" in {
            val invalidMoves = List(
              Move(whitePawn, from = e3, to = e5),
              Move(blackPawn, from = e6, to = e4)
            )

            testInvalidPattern(invalidMoves)
          }

          "empty square attacks" in {
            testInvalidPattern(attackingMoves)
          }

          "moves in the wrong direction" in {
            val invalidMoves = List(
              Move(whitePawn, b2, b1),
              Move(whitePawn, b2, a1),
              Move(whitePawn, b2, c1),
              Move(blackPawn, b7, b8),
              Move(blackPawn, b7, a8),
              Move(blackPawn, b7, c8)
            )

            testInvalidPattern(
              invalidMoves,
              // to make sure that attacks in wrong direction are impossible too
              additionalPieces = Map(
                a1 -> blackPawn,
                c1 -> blackPawn,
                a8 -> whitePawn,
                c8 -> whitePawn
              )
            )
          }
        }
      }

      "for king moves" - {
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

        val queenSideCastlings = List(
          Move(whiteKing, e1, c1),
          Move(blackKing, e8, c8)
        )

        val kingSideCastlings = List(
          Move(whiteKing, e1, g1),
          Move(blackKing, e8, g8)
        )

        "allows" - {
          "moves by 1 square to an empty square" in {
            testValidPattern(
              moves = kingMoves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            testByPredicate(
              kingMoves,
              move => Attack(move.to).asRight,
              additionalPieces =
                kingMoves.map(move => move.to -> blackPawn).toMap
            )
          }

          "castlings if available" in {
            testValidPattern(
              moves = queenSideCastlings,
              expected = Castling(QueenSide)
            )

            testValidPattern(
              moves = kingSideCastlings,
              expected = Castling(KingSide)
            )
          }
        }

        "does not allow" - {
          "moves for more that 1 square" in {
            val invalidMoves = List(
              Move(whiteKing, a1, a3),
              Move(whiteKing, a1, b3)
            )

            testInvalidPattern(moves = invalidMoves)
          }

          "castlings if not available" in {
            testInvalidPattern(
              moves = queenSideCastlings ::: kingSideCastlings,
              castlingsAvailable = Nil
            )
          }

          "castlings if there are barriers" in {
            testInvalidPattern(
              moves = queenSideCastlings ::: kingSideCastlings,
              additionalPieces = Map(
                b1 -> whitePawn,
                d8 -> whitePawn,
                f1 -> whitePawn,
                f8 -> whitePawn
              )
            )
          }

          "castlings if the king passes attacked squares" in {
            testInvalidPattern(
              moves = queenSideCastlings ::: kingSideCastlings,
              additionalPieces = Map(
                d5 -> whiteRook,
                d4 -> blackRook,
                f5 -> whiteRook,
                f4 -> blackRook
              )
            )
          }

          "castlings if the king is under attack before castling" in {
            testInvalidPattern(
              moves = queenSideCastlings ::: kingSideCastlings,
              additionalPieces = Map(
                e5 -> whiteRook,
                e4 -> blackRook
              )
            )
          }

          "castlings if the king is under attack after the castling" in {
            testInvalidPattern(
              moves = queenSideCastlings ::: kingSideCastlings,
              additionalPieces = Map(
                c3 -> blackRook,
                g3 -> blackRook,
                c6 -> whiteRook,
                g6 -> whiteRook
              )
            )
          }
        }
      }

      "for bishop moves" - {
        val moves = List(
          Move(whiteBishop, a1, h8),
          Move(blackBishop, h1, a8)
        )

        "allows" - {
          "diagonal moves" in {
            testValidPattern(
              moves = moves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            testByPredicate(
              moves,
              move => Attack(move.to).asRight,
              additionalPieces = Map(h8 -> blackPawn, a8 -> blackPawn)
            )
          }
        }

        "does not allow" - {
          "moves with barriers" in {
            testInvalidPattern(
              moves,
              additionalPieces = Map(b2 -> whitePawn, b7 -> whitePawn)
            )
          }

          "moves of invalid patterns" in {
            val invalidMoves = List(
              Move(whiteBishop, a1, a8),
              Move(whiteBishop, a1, h1),
              Move(whiteBishop, a1, b3),
              Move(whiteBishop, a2, h1)
            )

            testInvalidPattern(invalidMoves)
          }
        }
      }

      "for rook moves" - {
        "allows" - {
          "vertical moves" in {
            val moves = List(
              Move(whiteRook, a1, a8),
              Move(whiteRook, a8, a1)
            )

            testValidPattern(
              moves = moves,
              expected = Transition()
            )
          }

          "horizontal moves" in {
            val moves = List(
              Move(whiteRook, a1, h1),
              Move(whiteRook, h1, a1)
            )

            testValidPattern(
              moves = moves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            val moves = List(
              Move(whiteRook, a8, a1),
              Move(whiteRook, h1, a1)
            )

            testValidPattern(
              moves = moves,
              additionalPieces = Map(a1 -> blackPawn),
              expected = Attack(a1)
            )
          }
        }

        "does not allow" - {
          "moves with barriers" in {
            val invalidMoves = List(
              Move(whiteRook, a1, a8),
              Move(whiteRook, a1, h1)
            )

            testInvalidPattern(
              moves = invalidMoves,
              additionalPieces = Map(a2 -> whitePawn, b1 -> whitePawn)
            )
          }

          "moves of invalid patterns" in {
            val invalidMoves = List(
              Move(whiteRook, a1, h8),
              Move(whiteRook, a1, b3),
              Move(whiteRook, a2, h1)
            )

            testInvalidPattern(invalidMoves)
          }
        }
      }

      "for queen moves" - {
        "allows" - {
          "vertical moves" in {
            val moves = List(
              Move(whiteQueen, b1, b8),
              Move(whiteQueen, c8, c1)
            )

            testValidPattern(
              moves = moves,
              expected = Transition()
            )
          }

          "horizontal moves" in {
            val moves = List(
              Move(whiteQueen, b1, h1),
              Move(whiteQueen, h8, a8)
            )

            testValidPattern(
              moves = moves,
              expected = Transition()
            )
          }

          "diagonal moves" in {
            val moves = List(
              Move(whiteQueen, a1, h8),
              Move(whiteQueen, h1, a8)
            )

            testValidPattern(
              moves = moves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            val moves = List(
              Move(whiteQueen, a8, a1),
              Move(whiteQueen, h1, a1),
              Move(whiteQueen, h8, a1)
            )

            testValidPattern(
              moves = moves,
              additionalPieces = Map(a1 -> blackPawn),
              expected = Attack(a1)
            )
          }
        }

        "does not allow" - {
          "moves with barriers" in {
            val invalidMoves = List(
              Move(whiteQueen, a1, a8),
              Move(whiteQueen, a1, h8),
              Move(whiteQueen, a1, h1)
            )

            testInvalidPattern(
              moves = invalidMoves,
              additionalPieces = Map(
                a2 -> whitePawn,
                c3 -> whitePawn,
                b1 -> whitePawn
              )
            )
          }

          "moves of invalid patterns" in {
            val invalidMoves = List(
              Move(whiteQueen, a1, b3),
              Move(whiteQueen, a2, h1)
            )

            testInvalidPattern(moves = invalidMoves)
          }
        }
      }

      "for knight moves" - {
        "for knights" - {
          val moves = List(
            Move(whiteKnight, e4, f6),
            Move(whiteKnight, e4, g3),
            Move(whiteKnight, e4, d2),
            Move(whiteKnight, e4, c5)
          )

          "allows" - {
            "regular moves" in {
              testValidPattern(
                moves = moves,
                expected = Transition()
              )
            }

            "regular attacks" in {
              testByPredicate(
                moves,
                move => Attack(move.to).asRight,
                additionalPieces = Map(
                  f6 -> blackPawn,
                  g3 -> blackPawn,
                  d2 -> blackPawn,
                  c5 -> blackPawn
                )
              )
            }
          }

          "does not allow" - {
            "moves of invalid patterns" in {
              val invalidMoves = List(
                Move(whiteKnight, a1, a2),
                Move(whiteKnight, a1, b1),
                Move(whiteKnight, a1, h8),
                Move(whiteKnight, a1, a8),
                Move(whiteKnight, a1, h1),
                Move(whiteKnight, a1, h7)
              )

              testInvalidPattern(invalidMoves)
            }
          }
        }
      }

      def testValidPattern(
          moves: Seq[Move],
          additionalPieces: Map[Coordinate, Piece] = Map.empty,
          castlingsAvailable: List[CastlingType] = CastlingType.values.toList,
          expected: MovePattern
      ): Unit = testByPredicate(
        moves,
        _ => expected.asRight,
        castlingsAvailable,
        additionalPieces
      )

      def testInvalidPattern(
          moves: Seq[Move],
          castlingsAvailable: List[CastlingType] = CastlingType.values.toList,
          additionalPieces: Map[Coordinate, Piece] = Map.empty
      ): Unit = testByPredicate(
        moves,
        _ => InvalidMovePattern.asLeft,
        castlingsAvailable,
        additionalPieces
      )

      def testByPredicate(
          moves: Seq[Move],
          predicate: Move => ErrorOr[MovePattern],
          castlingsAvailable: List[CastlingType] = CastlingType.values.toList,
          additionalPieces: Map[Coordinate, Piece] = Map.empty
      ): Unit = moves.foreach { move =>
        validateMove(
          move,
          createStateForPatternValidation(
            move,
            additionalPieces = additionalPieces
          ).updateCastlings(castlingsAvailable)
        ) shouldEqual predicate(move)
      }

      def createStateForPatternValidation(
          move: Move,
          additionalPieces: Map[Coordinate, Piece] = Map.empty,
          enPassantCoordinateOption: Option[Coordinate] = None
      ): GameState =
        emptyGameState.copy(
          movesNow = move.piece.side,
          board = Chessboard(Map(move.from -> move.piece) ++ additionalPieces),
          enPassantCoordinateOption = enPassantCoordinateOption
        )
    }
  }
}
