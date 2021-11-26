package com.chessonline
package chess.domain

import chess.domain.CastlingType.{KingSide, QueenSide}
import chess.domain.MovePattern._
import chess.domain.MoveValidationError._
import chess.domain.ValidateMove.ErrorOr
import chess.domain.Side._

import cats.implicits.catsSyntaxEitherId
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class EvaluateMoveSpec extends AnyFreeSpec with MockFactory with EitherValues {
  "EvaluateMove" - {
    import TestData._

    val evaluateMoveStub = stub[ValidateMove]
    val kingIsSafeStub = stub[KingIsSafe]
    val evaluateMove = EvaluateMove(evaluateMoveStub, kingIsSafeStub)

    val defaultMove = Move(whitePawn, a1, a2)
    val defaultError = WrongPieceColor.asLeft
    val transition = Transition().asRight

    val allCastlingsAvailableState = emptyGameState.copy(
      movesNow = White,
      castlingsForWhite = CastlingType.values.toList,
      castlingsForBlack = CastlingType.values.toList
    )

    val kingNotSafeAfterMoveState = emptyGameState.copy(
      board = Chessboard(
        Map(
          a1 -> whiteKing,
          a8 -> blackRook
        )
      )
    )

    "evaluate" - {
      "returns an error if move validation fails" in {
        setUpAndEvalMove(
          defaultMove,
          patternResult = defaultError
        ) shouldEqual defaultError
      }

      "for transitions" - {
        "updates which side moves now correctly" in {
          setUpAndEvalMove(
            defaultMove,
            state = emptyGameState
          ).value.movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" - {
          "for king moves" in {
            setUpAndEvalMove(
              Move(whiteKing, a1, a2),
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual Nil
          }

          "for king side rook moves" in {
            setUpAndEvalMove(
              Move(whiteRook, h1, h2),
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(QueenSide)
          }

          "for queen side rook moves" in {
            setUpAndEvalMove(
              Move(whiteRook, a1, a2),
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(KingSide)
          }

          "does not affect available castlings for rook moves from non-starting position" in {
            val expectedCastlings = List(KingSide)

            val moves = List(
              Move(whiteRook, e1, e2),
              Move(whiteRook, h2, h3),
              Move(whiteRook, a8, a7)
            )

            testMoves(
              moves,
              state =
                allCastlingsAvailableState.updateCastlings(expectedCastlings)
            )(result => result.value.castlingsForWhite == expectedCastlings)
          }
        }

        "for other piece types moves" in {
          setUpAndEvalMove(
            Move(whitePawn, a1, a2),
            state = allCastlingsAvailableState
          ).value.castlingsForWhite shouldEqual allCastlingsAvailableState.castlingsForWhite
        }

        "updates chessboard correctly" in {
          val pawnAtA1 = emptyGameState.copy(
            board = Chessboard(Map(a2 -> whitePawn))
          )

          setUpAndEvalMove(
            move = Move(whitePawn, a2, a4),
            state = pawnAtA1
          ).value.board shouldEqual Chessboard(Map(a4 -> whitePawn))
        }

        "updates en passant coordinate correctly" - {
          "sets the new en passant coordinate" in {
            setUpAndEvalMove(
              defaultMove,
              patternResult =
                Transition(enPassantCoordinateOption = Some(a2)).asRight,
              state = emptyGameState
            ).value.enPassantCoordinateOption.value shouldEqual a2
          }

          "clears en passant coordinate" in {
            setUpAndEvalMove(
              defaultMove,
              state = emptyGameState.copy(enPassantCoordinateOption = Some(a1))
            ).value.enPassantCoordinateOption shouldEqual None
          }
        }

        "returns an error if the king is not safe after move" in {
          setUpAndEvalMove(
            Move(whiteKing, a1, a2),
            patternResult = transition,
            state = kingNotSafeAfterMoveState,
            isKingSafeAfterMove = false
          ) shouldEqual KingNotSafeAfterMove.asLeft
        }
      }

      "for attacks" - {
        val attack = Attack(e4).asRight

        "updates which side moves now correctly" in {
          setUpAndEvalMove(
            defaultMove,
            patternResult = attack,
            state = emptyGameState
          ).value.movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" - {
          "for king moves" in {
            setUpAndEvalMove(
              Move(whiteKing, a3, a4),
              patternResult = attack,
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual Nil
          }

          "for king side rook moves" in {
            setUpAndEvalMove(
              Move(whiteRook, h1, h2),
              patternResult = attack,
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(QueenSide)
          }

          "for queen side rook moves" in {
            setUpAndEvalMove(
              Move(whiteRook, a1, a2),
              patternResult = attack,
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(KingSide)
          }

          "does not affect available castlings for rook moves from non-starting position" in {
            val expectedCastlings = List(KingSide)

            val moves = List(
              Move(whiteRook, e1, e2),
              Move(whiteRook, h2, h3),
              Move(whiteRook, a8, a7)
            )

            testMoves(
              moves,
              moveToPattern = _ => attack,
              state = allCastlingsAvailableState.copy(castlingsForWhite =
                expectedCastlings
              )
            )(result => result.value.castlingsForWhite == expectedCastlings)
          }

          "for other piece types moves" in {
            setUpAndEvalMove(
              Move(whitePawn, a1, a2),
              patternResult = attack,
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual allCastlingsAvailableState.castlingsForWhite
          }
        }

        "updates chessboard correctly" in {
          val pawnAtE4State = emptyGameState.copy(
            board = Chessboard(Map(e4 -> blackPawn))
          )

          setUpAndEvalMove(
            move = Move(whiteRook, a1, e4),
            patternResult = attack,
            state = pawnAtE4State
          ).value.board shouldEqual Chessboard(Map(e4 -> whiteRook))
        }

        "clears en passant coordinate correctly" in {
          setUpAndEvalMove(
            defaultMove,
            patternResult = attack,
            state = emptyGameState.copy(enPassantCoordinateOption = Some(a1))
          ).value.enPassantCoordinateOption shouldEqual None
        }

        "returns an error if the king is not safe after move" in {
          setUpAndEvalMove(
            Move(whiteKing, a1, a2),
            patternResult = attack,
            state = kingNotSafeAfterMoveState,
            isKingSafeAfterMove = false
          ) shouldEqual KingNotSafeAfterMove.asLeft
        }
      }

      "for castlings" - {
        val whiteCastlingsState = emptyGameState.copy(
          board = Chessboard(
            Map(
              e1 -> whiteKing,
              a1 -> whiteRook,
              h1 -> whiteRook
            )
          )
        )

        val queenSideCastlingMove = Move(whiteKing, e1, c1)
        val kingSideCastlingMove = Move(whiteKing, e1, g1)
        val queenSideCastling = Castling(QueenSide).asRight
        val kingSideCastling = Castling(KingSide).asRight

        "updates which side moves now correctly" in {
          setUpAndEvalMove(
            queenSideCastlingMove,
            patternResult = queenSideCastling,
            state = whiteCastlingsState
          ).value.movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" in {
          setUpAndEvalMove(
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state = whiteCastlingsState
          ).value.castlingsForWhite shouldEqual Nil

          setUpAndEvalMove(
            queenSideCastlingMove,
            patternResult = queenSideCastling,
            state = whiteCastlingsState
          ).value.castlingsForWhite shouldEqual Nil
        }

        "updates chessboard correctly" in {
          setUpAndEvalMove(
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state = whiteCastlingsState
          ).value.board.pieceMap shouldEqual Map(
            a1 -> whiteRook,
            g1 -> whiteKing,
            f1 -> whiteRook
          )

          setUpAndEvalMove(
            queenSideCastlingMove,
            patternResult = queenSideCastling,
            state = whiteCastlingsState
          ).value.board.pieceMap shouldEqual Map(
            d1 -> whiteRook,
            c1 -> whiteKing,
            h1 -> whiteRook
          )
        }

        "clears en passant coordinate correctly" in {
          setUpAndEvalMove(
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state =
              whiteCastlingsState.copy(enPassantCoordinateOption = Some(a1))
          ).value.enPassantCoordinateOption shouldEqual None
        }

        "returns an error if the king is not safe after move" in {
          setUpAndEvalMove(
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state = whiteCastlingsState.copy(
              board = Chessboard(Map(c8 -> blackRook))
            ),
            isKingSafeAfterMove = false
          ) shouldEqual KingNotSafeAfterMove.asLeft
        }
      }
    }

    def setUpAndEvalMove(
        move: Move,
        patternResult: ErrorOr[MovePattern] = transition,
        isKingSafeAfterMove: Boolean = true,
        state: GameState = emptyGameState
    ): ErrorOr[GameState] = {
      // Mock EvaluateMove.apply returned pattern.
      evaluateMoveStub.apply _ when (move, state) returns patternResult

      // Mock whether the king is safe after the evaluated move.
      kingIsSafeStub.apply _ when (*, *) returns isKingSafeAfterMove

      evaluateMove.apply(move, state)
    }

    def testMoves(
        moves: Seq[Move],
        moveToPattern: Move => ErrorOr[MovePattern] = _ => transition,
        moveToIsKingSave: Move => Boolean = _ => true,
        state: GameState = emptyGameState
    )(predicate: ErrorOr[GameState] => Boolean): Unit =
      moves.foreach { move =>
        predicate(
          setUpAndEvalMove(
            move,
            moveToPattern(move),
            moveToIsKingSave(move),
            state
          )
        ) shouldBe true
      }
  }
}