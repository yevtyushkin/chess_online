package com.chessonline
package chess.domain

import chess.domain.CastlingType.{KingSide, QueenSide}
import chess.domain.GameStatus.{Draw, GameContinues, Win}
import chess.domain.MovePattern._
import chess.domain.MoveValidationError._
import chess.domain.Side._
import chess.domain.ValidateMove.ErrorOr

import cats.implicits.catsSyntaxEitherId
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class EvaluateMoveSpec extends AnyFreeSpec with MockFactory with EitherValues {
  "EvaluateMove" - {
    import TestData._
    import TestUtils._

    val validateMoveStub = stub[ValidateMove]
    val kingIsSafeStub = stub[KingIsSafe]
    val evaluateMove = EvaluateMove(validateMoveStub, kingIsSafeStub)

    val defaultMove = Move(a1, a2)
    val piece = whitePawn
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
          piece,
          defaultMove,
          patternResult = defaultError
        ) shouldEqual defaultError
      }

      "for transitions" - {
        "updates which side moves now correctly" in {
          setUpAndEvalMove(
            piece,
            defaultMove,
            state = emptyGameState
          ).value.movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" - {
          "for king moves" in {
            setUpAndEvalMove(
              whiteKing,
              Move(a1, a2),
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual Nil
          }

          "for king side rook moves" in {
            setUpAndEvalMove(
              whiteRook,
              Move(h1, h2),
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(QueenSide)
          }

          "for queen side rook moves" in {
            setUpAndEvalMove(
              whiteRook,
              Move(a1, a2),
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(KingSide)
          }

          "does not affect available castlings for rook moves from non-starting position" in {
            val expectedCastlings = List(KingSide)

            val moves = List(
              Move(e1, e2),
              Move(h2, h3),
              Move(a8, a7)
            )

            testMoves(
              nPieces(moves.length, whiteRook),
              moves,
              state =
                allCastlingsAvailableState.updateCastlings(expectedCastlings)
            )(result => result.value.castlingsForWhite == expectedCastlings)
          }
        }

        "for other piece types moves" in {
          setUpAndEvalMove(
            whitePawn,
            Move(a1, a2),
            state = allCastlingsAvailableState
          ).value.castlingsForWhite shouldEqual allCastlingsAvailableState.castlingsForWhite
        }

        "updates chessboard correctly" in {
          val pawnAtA1 = emptyGameState.copy(
            board = Chessboard(Map(a2 -> whitePawn))
          )

          setUpAndEvalMove(
            whitePawn,
            move = Move(a2, a4),
            state = pawnAtA1
          ).value.board shouldEqual Chessboard(Map(a4 -> whitePawn))
        }

        "updates en passant coordinate correctly" - {
          "sets the new en passant coordinate" in {
            setUpAndEvalMove(
              piece,
              defaultMove,
              patternResult =
                Transition(enPassantCoordinateOption = Some(a2)).asRight,
              state = emptyGameState
            ).value.enPassantCoordinateOption.value shouldEqual a2
          }

          "clears en passant coordinate" in {
            setUpAndEvalMove(
              piece,
              defaultMove,
              state = emptyGameState.copy(enPassantCoordinateOption = Some(a1))
            ).value.enPassantCoordinateOption shouldEqual None
          }
        }

        "returns an error if the king is not safe after move" in {
          setUpAndEvalMove(
            whiteKing,
            Move(a1, a2),
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
            piece,
            defaultMove,
            patternResult = attack,
            state = emptyGameState
          ).value.movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" - {
          "for king moves" in {
            setUpAndEvalMove(
              whiteKing,
              Move(a3, a4),
              patternResult = attack,
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual Nil
          }

          "for king side rook moves" in {
            setUpAndEvalMove(
              whiteRook,
              Move(h1, h2),
              patternResult = attack,
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(QueenSide)
          }

          "for queen side rook moves" in {
            setUpAndEvalMove(
              whiteRook,
              Move(a1, a2),
              patternResult = attack,
              state = allCastlingsAvailableState
            ).value.castlingsForWhite shouldEqual List(KingSide)
          }

          "does not affect available castlings for rook moves from non-starting position" in {
            val expectedCastlings = List(KingSide)

            val moves = List(
              Move(e1, e2),
              Move(h2, h3),
              Move(a8, a7)
            )

            testMoves(
              nPieces(moves.length, whiteRook),
              moves,
              moveToPattern = _ => attack,
              state = allCastlingsAvailableState.copy(castlingsForWhite =
                expectedCastlings
              )
            )(result => result.value.castlingsForWhite == expectedCastlings)
          }

          "for other piece types moves" in {
            setUpAndEvalMove(
              whitePawn,
              Move(a1, a2),
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
            whiteRook,
            move = Move(a1, e4),
            patternResult = attack,
            state = pawnAtE4State
          ).value.board shouldEqual Chessboard(Map(e4 -> whiteRook))
        }

        "clears en passant coordinate correctly" in {
          setUpAndEvalMove(
            piece,
            defaultMove,
            patternResult = attack,
            state = emptyGameState.copy(enPassantCoordinateOption = Some(a1))
          ).value.enPassantCoordinateOption shouldEqual None
        }

        "returns an error if the king is not safe after move" in {
          setUpAndEvalMove(
            whiteKing,
            Move(a1, a2),
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

        val queenSideCastlingMove = Move(e1, c1)
        val kingSideCastlingMove = Move(e1, g1)
        val queenSideCastling = Castling(QueenSide).asRight
        val kingSideCastling = Castling(KingSide).asRight

        "updates which side moves now correctly" in {
          setUpAndEvalMove(
            whiteKing,
            queenSideCastlingMove,
            patternResult = queenSideCastling,
            state = whiteCastlingsState
          ).value.movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" in {
          setUpAndEvalMove(
            whiteKing,
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state = whiteCastlingsState
          ).value.castlingsForWhite shouldEqual Nil

          setUpAndEvalMove(
            whiteKing,
            queenSideCastlingMove,
            patternResult = queenSideCastling,
            state = whiteCastlingsState
          ).value.castlingsForWhite shouldEqual Nil
        }

        "updates chessboard correctly" in {
          setUpAndEvalMove(
            whiteKing,
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state = whiteCastlingsState
          ).value.board.pieceMap shouldEqual Map(
            a1 -> whiteRook,
            g1 -> whiteKing,
            f1 -> whiteRook
          )

          setUpAndEvalMove(
            whiteKing,
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
            whiteKing,
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state =
              whiteCastlingsState.copy(enPassantCoordinateOption = Some(a1))
          ).value.enPassantCoordinateOption shouldEqual None
        }

        "returns an error if the king is not safe after move" in {
          setUpAndEvalMove(
            whiteKing,
            kingSideCastlingMove,
            patternResult = kingSideCastling,
            state = whiteCastlingsState.copy(
              board = Chessboard(Map(c8 -> blackRook))
            ),
            isKingSafeAfterMove = false
          ) shouldEqual KingNotSafeAfterMove.asLeft
        }
      }

      "updates game status correctly" - {
        val validateMove = ValidateMove()
        val pureEvaluateMove =
          EvaluateMove(validateMove, KingIsSafe(validateMove))

        "for game continues" in {
          val move = Move(e2, e4)
          val state = emptyGameState.copy(
            board = Chessboard(
              Map(
                e1 -> whiteKing,
                e2 -> whitePawn,
                e8 -> blackKing
              )
            )
          )

          pureEvaluateMove(move, state).value.status shouldEqual GameContinues
        }

        "for wins" in {
          val move = Move(b2, b7)
          val state = emptyGameState.copy(
            board = Chessboard(
              Map(
                b1 -> whiteRook,
                b2 -> whiteQueen,
                a8 -> blackKing
              )
            )
          )

          pureEvaluateMove(move, state).value.status shouldEqual Win(by = White)
        }

        "for draws" in {
          val move = Move(b3, b2)
          val state = emptyGameState.copy(
            movesNow = Black,
            board = Chessboard(
              Map(
                a8 -> whiteKing,
                a7 -> whitePawn,
                b3 -> blackQueen
              )
            )
          )

          pureEvaluateMove(move, state).value.status shouldEqual Draw
        }

        // rook d1 -> d8
        "determines whether the mate can be prevented" in {
          val move = Move(h1, h8)
          val state = emptyGameState.copy(
            board = Chessboard(
              Map(
                a8 -> blackKing,
                a7 -> blackPawn,
                b7 -> blackPawn,
                c7 -> blackPawn,
                d1 -> blackRook,
                h1 -> whiteQueen
              )
            )
          )

          pureEvaluateMove(move, state).value.status shouldEqual GameContinues
        }
      }
    }

    def testMoves(
        pieces: Seq[Piece],
        moves: Seq[Move],
        moveToPattern: Move => ErrorOr[MovePattern] = _ => transition,
        moveToIsKingSave: Move => Boolean = _ => true,
        state: GameState = emptyGameState
    )(predicate: ErrorOr[GameState] => Boolean): Unit =
      moves.zip(pieces).foreach { case (move, piece) =>
        predicate(
          setUpAndEvalMove(
            piece,
            move,
            moveToPattern(move),
            moveToIsKingSave(move),
            state
          )
        ) shouldBe true
      }

    def setUpAndEvalMove(
        piece: Piece,
        move: Move,
        patternResult: ErrorOr[MovePattern] = transition,
        isKingSafeAfterMove: Boolean = true,
        state: GameState = emptyGameState
    ): ErrorOr[GameState] = {
      // Mock EvaluateMove.apply returned pattern.
      validateMoveStub.apply _ when (move, state) returns patternResult.map(
        (piece, _)
      )

      // Mock whether the king is safe after the evaluated move.
      kingIsSafeStub.apply _ when (piece.side, *) returns isKingSafeAfterMove

      evaluateMove.apply(move, state)
    }
  }
}
