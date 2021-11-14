package com.chessonline
package chess.domain

import chess.domain.CastlingType.{KingSide, QueenSide}
import chess.domain.MovePattern._
import chess.domain.MoveValidationError.{
  InvalidMovePattern,
  KingNotSafeAfterMove,
  WrongPieceColor
}
import chess.domain.Side._

import cats.implicits.catsSyntaxEitherId
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class MoveEvaluatorSpec extends AnyFreeSpec with MockFactory with EitherValues {
  "MoveEvaluator" - {
    import MoveEvaluator._
    import TestData._

    val moveValidatorMock = mock[MoveValidator]
    val defaultMove = Move(whitePawn, a1, a2)
    val transition = Transition()
    val allCastlingsAvailableState = emptyGameState.copy(
      movesNow = White,
      castlingsForWhite = CastlingType.values.toList,
      castlingsForBlack = CastlingType.values.toList
    )

    "updateState" - {
      "for transitions" - {
        "updates which side moves now correctly" in {
          updateState(
            defaultMove,
            transition,
            emptyGameState
          ).movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" - {
          "for king moves" in {
            updateState(
              Move(whiteKing, a1, a2),
              transition,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual Nil
          }

          "for king side rook moves" in {
            updateState(
              Move(whiteRook, h1, h2),
              transition,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual List(QueenSide)
          }

          "for queen side rook moves" in {
            updateState(
              Move(whiteRook, a1, a2),
              transition,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual List(KingSide)
          }

          "does not affect available castlings for rook moves from non-starting position" in {
            val moves = List(
              Move(whiteRook, e1, e2),
              Move(whiteRook, h2, h3),
              Move(whiteRook, a8, a7)
            )

            moves.foreach { move =>
              updateState(
                move,
                transition,
                allCastlingsAvailableState.copy(castlingsForWhite =
                  List(KingSide)
                )
              ).castlingsForWhite shouldEqual List(KingSide)
            }
          }

          "for other piece types moves" in {
            updateState(
              Move(whitePawn, a1, a2),
              transition,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual allCastlingsAvailableState.castlingsForWhite
          }
        }

        "updates chessboard correctly" in {
          val pawnAtA1 = emptyGameState.copy(
            board = Chessboard(Map(a1 -> whitePawn))
          )

          updateState(
            move = Move(whitePawn, a1, h8),
            transition,
            pawnAtA1
          ).board shouldEqual Chessboard(Map(h8 -> whitePawn))
        }

        "updates en passant coordinate correctly" - {
          "sets the new en passant coordinate" in {
            updateState(
              defaultMove,
              Transition(enPassantCoordinateOption = Some(a2)),
              emptyGameState
            ).enPassantCoordinateOption.value shouldEqual a2
          }

          "clears en passant coordinate" in {
            updateState(
              defaultMove,
              Transition(),
              emptyGameState.copy(enPassantCoordinateOption = Some(a1))
            ).enPassantCoordinateOption shouldEqual None
          }
        }
      }

      "for attacks" - {
        val attack = Attack(e4)
        val pawnAtE4State = emptyGameState.copy(
          board = Chessboard(Map(e4 -> blackPawn))
        )

        "updates which side moves now correctly" in {
          updateState(
            defaultMove,
            attack,
            emptyGameState
          ).movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" - {
          "for king moves" in {
            updateState(
              Move(whiteKing, a1, a2),
              attack,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual Nil
          }

          "for king side rook moves" in {
            updateState(
              Move(whiteRook, h1, h2),
              attack,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual List(QueenSide)
          }

          "for queen side rook moves" in {
            updateState(
              Move(whiteRook, a1, a2),
              attack,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual List(KingSide)
          }

          "does not affect available castlings for rook moves from non-starting position" in {
            val moves = List(
              Move(whiteRook, e1, e2),
              Move(whiteRook, h2, h3),
              Move(whiteRook, a8, a7)
            )

            moves.foreach { move =>
              updateState(
                move,
                Attack(move.to),
                allCastlingsAvailableState.copy(castlingsForWhite =
                  List(KingSide)
                )
              ).castlingsForWhite shouldEqual List(KingSide)
            }
          }

          "for other piece types moves" in {
            updateState(
              Move(whitePawn, a1, a2),
              attack,
              allCastlingsAvailableState
            ).castlingsForWhite shouldEqual allCastlingsAvailableState.castlingsForWhite
          }
        }

        "updates chessboard correctly" in {
          updateState(
            move = Move(whitePawn, a1, e4),
            attack,
            pawnAtE4State
          ).board shouldEqual Chessboard(Map(e4 -> whitePawn))
        }

        "clears en passant coordinate correctly" in {
          updateState(
            defaultMove,
            attack,
            emptyGameState.copy(enPassantCoordinateOption = Some(a1))
          ).enPassantCoordinateOption shouldEqual None
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

        "updates which side moves now correctly" in {
          updateState(
            defaultMove,
            Castling(KingSide),
            whiteCastlingsState
          ).movesNow shouldEqual emptyGameState.movesNow.opposite
        }

        "updates available castlings correctly" in {
          updateState(
            Move(whiteKing, e1, g1),
            Castling(KingSide),
            whiteCastlingsState
          ).castlingsForWhite shouldEqual Nil

          updateState(
            Move(whiteKing, e1, c1),
            Castling(QueenSide),
            whiteCastlingsState
          ).castlingsForWhite shouldEqual Nil
        }

        "updates chessboard correctly" in {
          updateState(
            Move(whiteKing, e1, g1),
            Castling(KingSide),
            whiteCastlingsState
          ).board shouldEqual Chessboard(
            Map(a1 -> whiteRook, g1 -> whiteKing, f1 -> whiteRook)
          )

          updateState(
            Move(whiteKing, e1, c1),
            Castling(QueenSide),
            whiteCastlingsState
          ).board shouldEqual Chessboard(
            Map(d1 -> whiteRook, c1 -> whiteKing, h1 -> whiteRook)
          )
        }

        "clears en passant coordinate correctly" in {
          updateState(
            defaultMove,
            Castling(KingSide),
            whiteCastlingsState.copy(enPassantCoordinateOption = Some(a1))
          ).enPassantCoordinateOption shouldEqual None
        }
      }
    }

    "kingIsSafe" - {
      val state = emptyGameState.copy(
        board = Chessboard(
          Map(
            e1 -> whiteKing,
            e8 -> blackRook
          )
        )
      )

      "returns true if any of the enemy pieces can't attack the king" in {
        (moveValidatorMock.validatePattern _)
          .expects(Move(blackRook, e8, e1), state)
          .returning(InvalidMovePattern.asLeft)

        kingIsSafe(kingSide = White, gameState = state)(moveValidator =
          moveValidatorMock
        ) shouldBe true
      }

      "returns false if any of the enemy pieces can attack the king" in {
        (moveValidatorMock.validatePattern _)
          .expects(Move(blackRook, e8, e1), state)
          .returning(Attack(e1).asRight)

        kingIsSafe(kingSide = White, gameState = state)(moveValidator =
          moveValidatorMock
        ) shouldBe false
      }
    }

    "evaluate" - {
      val move = Move(whitePawn, e2, e4)

      def createEvaluatorStub(
          stateUpdateResult: GameState = emptyGameState,
          kingIsSafeResult: Boolean = true
      ): MoveEvaluator =
        new MoveEvaluator {
          override def updateState(
              move: Move,
              movePattern: MovePattern,
              gameState: GameState
          ): GameState = stateUpdateResult

          override def kingIsSafe(forSide: Side, gameState: GameState)(
              moveValidator: MoveValidator
          ): Boolean = kingIsSafeResult
        }

      "returns an error if move validation fails" in {
        (moveValidatorMock.validate _)
          .expects(move, emptyGameState)
          .returning(WrongPieceColor.asLeft)

        createEvaluatorStub()
          .evaluate(move, emptyGameState)(
            moveValidatorMock
          )
          .left
          .value shouldEqual WrongPieceColor
      }

      "returns an error if the king is not safe after the move" in {
        (moveValidatorMock.validate _)
          .expects(move, emptyGameState)
          .returning((move, Transition()).asRight)

        createEvaluatorStub(kingIsSafeResult = false)
          .evaluate(move, emptyGameState)(moveValidatorMock)
          .left
          .value shouldEqual KingNotSafeAfterMove
      }

      "returns the updated state if the move is valid and the king is safe after move" in {
        (moveValidatorMock.validate _)
          .expects(move, emptyGameState)
          .returning((move, Transition()).asRight)
        val expectedState = emptyGameState.copy(movesNow = Black)

        createEvaluatorStub(
          stateUpdateResult = expectedState
        ).evaluate(move, emptyGameState)(moveValidatorMock)
          .value shouldEqual expectedState
      }
    }
  }
}
