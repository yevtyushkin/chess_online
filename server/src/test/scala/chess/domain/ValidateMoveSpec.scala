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
    import TestUtils._

    val validateMove = ValidateMove.apply()

    "validate" - {
      "returns an error" - {
        "if the piece color is wrong" in {
          validateMove(
            Move(a2, a4),
            emptyGameState.copy(
              movesNow = Black,
              board = Chessboard(Map(a2 -> whitePawn))
            )
          ).left.value shouldEqual WrongPieceColor
        }

        "if the piece is not present at starting coordinate" in {
          validateMove(
            Move(a2, a4),
            emptyGameState
          ).left.value shouldEqual NoPieceAtStartingCoordinate
        }

        "if the starting coordinate is equal to the destination coordinate" in {
          validateMove(
            Move(a2, a2),
            emptyGameState.copy(board = Chessboard(Map(a2 -> whitePawn)))
          ).left.value shouldEqual IdenticalStartAndDestinationCoordinates
        }

        "if the destination coordinate is taken by an ally piece" in {
          validateMove(
            Move(a2, a4),
            emptyGameState.copy(board =
              Chessboard(Map(a2 -> whitePawn, a4 -> whiteKing))
            )
          ).left.value shouldEqual DestinationTakenByAllyPiece
        }
      }

      "for pawn moves" - {
        val attackingMoves = List(
          Move(from = e4, to = d5),
          Move(from = e4, to = f5),
          Move(from = e5, to = d4),
          Move(from = e5, to = f4)
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
          Move(from = e2, to = e3),
          Move(from = e7, to = e6)
        )

        val twoSquaresForwardMoves = List(
          Move(from = e2, to = e4),
          Move(from = e7, to = e5)
        )

        val expectedEnPassantCoordinates = List(e3, e6)

        "allows" - {
          "moves 1 square forward to an empty square" in {
            testValidPattern(
              pieces = List(whitePawn, blackPawn),
              moves = oneSquareForwardMoves,
              expected = Transition()
            )
          }

          "moves 2 squares forward move to an empty square from the starting coordinate with no barriers" in {
            twoSquaresForwardMoves
              .zip(expectedEnPassantCoordinates)
              .zip(List(whitePawn, blackPawn))
              .foreach { case ((move, expectedEnPassantCoordinate), pawn) =>
                validateMove(
                  move,
                  createStateForPatternValidation(pawn, move)
                ).value shouldEqual
                  (pawn,
                  Transition(enPassantCoordinateOption =
                    Some(expectedEnPassantCoordinate)
                  ))
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
              pieces = List(whitePawn, whitePawn, blackPawn, blackPawn),
              attackingMoves,
              (piece, move) => (piece, Attack(move.to)).asRight,
              additionalPieces = enemyPieces
            )
          }

          "en passant attacks" in {
            enPassantAttackMoves
              .zip(List(whitePawn, whitePawn, blackPawn, blackPawn))
              .foreach {
                case (
                      (move @ Move(_, to), expectedAttackedPieceCoordinate),
                      pawn
                    ) =>
                  validateMove(
                    move,
                    createStateForPatternValidation(
                      pawn,
                      move,
                      enPassantCoordinateOption = to.some
                    )
                  ).value shouldEqual (pawn, Attack(
                    expectedAttackedPieceCoordinate
                  ))
              }
          }
        }

        "does not allow" - {
          "moves forward to non-empty square" in {
            val invalidMoves = List(
              Move(from = e2, to = e3),
              Move(from = e7, to = e5)
            )

            testInvalidPattern(
              pieces = List(whitePawn, blackPawn),
              moves = invalidMoves,
              additionalPieces = Map(e3 -> blackKing, e5 -> whiteKing)
            )
          }

          "moves 2 squares forward with barriers" in {
            testInvalidPattern(
              pieces = nPieces(twoSquaresForwardMoves.length, whitePawn),
              moves = twoSquaresForwardMoves,
              additionalPieces = Map(
                e3 -> blackKing,
                e6 -> whiteKing
              )
            )
          }

          "moves 2 squares forward from non-starting position" in {
            val invalidMoves = List(
              Move(from = e3, to = e5),
              Move(from = e6, to = e4)
            )

            testInvalidPattern(
              pieces = nPieces(invalidMoves.length, whitePawn),
              invalidMoves
            )
          }

          "empty square attacks" in {
            testInvalidPattern(
              pieces = nPieces(attackingMoves.length, whitePawn),
              attackingMoves
            )
          }

          "moves in the wrong direction" in {
            val invalidMoves = List(
              Move(b2, b1),
              Move(b2, a1),
              Move(b2, c1),
              Move(b7, b8),
              Move(b7, a8),
              Move(b7, c8)
            )

            testInvalidPattern(
              pieces = List(
                whitePawn,
                whitePawn,
                whitePawn,
                blackPawn,
                blackPawn,
                blackPawn
              ),
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
          Move(e3, e4), // forward
          Move(e3, e2), // back
          Move(e3, d3), // left
          Move(e3, f3), // right
          Move(e3, d4), // left-forward
          Move(e3, f4), // right-forward
          Move(e3, d3), // left-back
          Move(e3, f3) // right-back
        )

        val queenSideCastlings = List(
          Move(e1, c1),
          Move(e8, c8)
        )

        val kingSideCastlings = List(
          Move(e1, g1),
          Move(e8, g8)
        )

        "allows" - {
          "moves by 1 square to an empty square" in {
            testValidPattern(
              pieces = nPieces(kingMoves.length, whiteKing),
              moves = kingMoves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            testByPredicate(
              pieces = nPieces(kingMoves.length, whiteKing),
              kingMoves,
              (piece, move) => (piece, Attack(move.to)).asRight,
              additionalPieces =
                kingMoves.map(move => move.to -> blackPawn).toMap
            )
          }

          "castlings if available" in {
            testValidPattern(
              pieces = List(whiteKing, blackKing),
              moves = queenSideCastlings,
              expected = Castling(QueenSide)
            )

            testValidPattern(
              pieces = List(whiteKing, blackKing),
              moves = kingSideCastlings,
              expected = Castling(KingSide)
            )
          }
        }

        "does not allow" - {
          "moves for more that 1 square" in {
            val invalidMoves = List(
              Move(a1, a3),
              Move(a1, a4)
            )

            testInvalidPattern(
              pieces = nPieces(invalidMoves.length, whiteKing),
              moves = invalidMoves
            )
          }

          "castlings if not available" in {
            testInvalidPattern(
              pieces = List(whiteKing, blackKing, whiteKing, blackKing),
              moves = queenSideCastlings ::: kingSideCastlings,
              castlingsAvailable = Nil
            )
          }

          "castlings if there are barriers" in {
            testInvalidPattern(
              pieces = List(whiteKing, blackKing, whiteKing, blackKing),
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
              pieces = List(whiteKing, blackKing, whiteKing, blackKing),
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
              pieces = List(whiteKing, blackKing, whiteKing, blackKing),
              moves = queenSideCastlings ::: kingSideCastlings,
              additionalPieces = Map(
                e5 -> whiteRook,
                e4 -> blackRook
              )
            )
          }

          "castlings if the king is under attack after the castling" in {
            testInvalidPattern(
              pieces = List(whiteKing, blackKing, whiteKing, blackKing),
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
          Move(a1, h8),
          Move(h1, a8)
        )

        "allows" - {
          "diagonal moves" in {
            testValidPattern(
              pieces = nPieces(moves.length, whiteBishop),
              moves = moves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            testByPredicate(
              pieces = nPieces(moves.length, whiteBishop),
              moves,
              (piece, move) => (piece, Attack(move.to)).asRight,
              additionalPieces = Map(h8 -> blackPawn, a8 -> blackPawn)
            )
          }
        }

        "does not allow" - {
          "moves with barriers" in {
            testInvalidPattern(
              pieces = nPieces(moves.length, whiteBishop),
              moves,
              additionalPieces = Map(b2 -> whitePawn, b7 -> whitePawn)
            )
          }

          "moves of invalid patterns" in {
            val invalidMoves = List(
              Move(a1, a8),
              Move(a1, h1),
              Move(a1, b3),
              Move(a2, h1)
            )

            testInvalidPattern(
              pieces = nPieces(invalidMoves.length, whiteBishop),
              invalidMoves
            )
          }
        }
      }

      "for rook moves" - {
        "allows" - {
          "vertical moves" in {
            val moves = List(
              Move(a1, a8),
              Move(a8, a1)
            )

            testValidPattern(
              pieces = nPieces(moves.length, whiteRook),
              moves = moves,
              expected = Transition()
            )
          }

          "horizontal moves" in {
            val moves = List(
              Move(a1, h1),
              Move(h1, a1)
            )

            testValidPattern(
              pieces = nPieces(moves.length, whiteRook),
              moves = moves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            val moves = List(
              Move(a8, a1),
              Move(h1, a1)
            )

            testValidPattern(
              pieces = nPieces(moves.length, whiteRook),
              moves = moves,
              additionalPieces = Map(a1 -> blackPawn),
              expected = Attack(a1)
            )
          }
        }

        "does not allow" - {
          "moves with barriers" in {
            val invalidMoves = List(
              Move(a1, a8),
              Move(a1, h1)
            )

            testInvalidPattern(
              pieces = nPieces(invalidMoves.length, whiteRook),
              moves = invalidMoves,
              additionalPieces = Map(a2 -> whitePawn, b1 -> whitePawn)
            )
          }

          "moves of invalid patterns" in {
            val invalidMoves = List(
              Move(a1, h8),
              Move(a1, b3),
              Move(a2, h1)
            )

            testInvalidPattern(
              pieces = nPieces(invalidMoves.length, whiteRook),
              invalidMoves
            )
          }
        }
      }

      "for queen moves" - {
        "allows" - {
          "vertical moves" in {
            val moves = List(
              Move(b1, b8),
              Move(c8, c1)
            )

            testValidPattern(
              pieces = nPieces(moves.length, whiteQueen),
              moves = moves,
              expected = Transition()
            )
          }

          "horizontal moves" in {
            val moves = List(
              Move(b1, h1),
              Move(h8, a8)
            )

            testValidPattern(
              pieces = nPieces(moves.length, whiteQueen),
              moves = moves,
              expected = Transition()
            )
          }

          "diagonal moves" in {
            val moves = List(
              Move(a1, h8),
              Move(h1, a8)
            )

            testValidPattern(
              pieces = nPieces(moves.length, whiteQueen),
              moves = moves,
              expected = Transition()
            )
          }

          "regular attacks" in {
            val moves = List(
              Move(a8, a1),
              Move(h1, a1),
              Move(h8, a1)
            )

            testValidPattern(
              pieces = nPieces(moves.length, whiteQueen),
              moves = moves,
              additionalPieces = Map(a1 -> blackPawn),
              expected = Attack(a1)
            )
          }
        }

        "does not allow" - {
          "moves with barriers" in {
            val invalidMoves = List(
              Move(a1, a8),
              Move(a1, h8),
              Move(a1, h1)
            )

            testInvalidPattern(
              pieces = nPieces(invalidMoves.length, whiteQueen),
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
              Move(a1, b3),
              Move(a2, h1)
            )

            testInvalidPattern(
              pieces = nPieces(invalidMoves.length, whiteQueen),
              moves = invalidMoves
            )
          }
        }
      }

      "for knight moves" - {
        "for knights" - {
          val moves = List(
            Move(e4, f6),
            Move(e4, g3),
            Move(e4, d2),
            Move(e4, c5)
          )

          "allows" - {
            "regular moves" in {
              testValidPattern(
                pieces = nPieces(moves.length, whiteKnight),
                moves = moves,
                expected = Transition()
              )
            }

            "regular attacks" in {
              testByPredicate(
                pieces = nPieces(moves.length, whiteKnight),
                moves,
                (piece, move) => (piece, Attack(move.to)).asRight,
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
                Move(a1, a2),
                Move(a1, b1),
                Move(a1, h8),
                Move(a1, a8),
                Move(a1, h1),
                Move(a1, h7)
              )

              testInvalidPattern(
                pieces = nPieces(invalidMoves.length, whiteKnight),
                invalidMoves
              )
            }
          }
        }
      }

      def testValidPattern(
          pieces: Seq[Piece],
          moves: Seq[Move],
          additionalPieces: Map[Coordinate, Piece] = Map.empty,
          castlingsAvailable: List[CastlingType] = CastlingType.values.toList,
          expected: MovePattern
      ): Unit = testByPredicate(
        pieces,
        moves,
        (piece, _) => (piece, expected).asRight,
        castlingsAvailable,
        additionalPieces
      )

      def testInvalidPattern(
          pieces: Seq[Piece],
          moves: Seq[Move],
          castlingsAvailable: List[CastlingType] = CastlingType.values.toList,
          additionalPieces: Map[Coordinate, Piece] = Map.empty
      ): Unit = testByPredicate(
        pieces,
        moves,
        (_, _) => InvalidMovePattern.asLeft,
        castlingsAvailable,
        additionalPieces
      )

      def testByPredicate(
          pieces: Seq[Piece],
          moves: Seq[Move],
          predicate: (Piece, Move) => ErrorOr[(Piece, MovePattern)],
          castlingsAvailable: List[CastlingType] = CastlingType.values.toList,
          additionalPieces: Map[Coordinate, Piece] = Map.empty
      ): Unit = moves.zip(pieces).foreach { case (move, piece) =>
        validateMove(
          move,
          createStateForPatternValidation(
            piece,
            move,
            additionalPieces = additionalPieces
          ).updateCastlings(castlingsAvailable)
        ) shouldEqual predicate(piece, move)
      }

      def createStateForPatternValidation(
          piece: Piece,
          move: Move,
          additionalPieces: Map[Coordinate, Piece] = Map.empty,
          enPassantCoordinateOption: Option[Coordinate] = None
      ): GameState =
        emptyGameState.copy(
          movesNow = piece.side,
          board = Chessboard(Map(move.from -> piece) ++ additionalPieces),
          enPassantCoordinateOption = enPassantCoordinateOption
        )
    }
  }
}
