package com.chessonline
package chess.domain

import chess.domain.CastlingType.{KingSide, QueenSide}
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.MoveValidationError.InvalidMovePattern
import chess.domain.PieceType._
import chess.domain.Side._

import cats.implicits.catsSyntaxEitherId

trait MovePatternValidator {

  def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, MovePattern]
}

object MovePatternValidator extends MovePatternValidator {
  import MovePattern._
  type MovePatternOrError = Either[MoveValidationError, MovePattern]

  override def validate(
      move: Move,
      gameState: GameState
  ): MovePatternOrError = {
    def side: Side = move.piece.side

    def destinationIsEmpty: Boolean = gameState.board.pieceAt(move.to).isEmpty

    def noPiecesBetween(
        from: Coordinate = move.from,
        to: Coordinate = move.to
    ): Boolean =
      Coordinate.allBetween(from, to).forall(gameState.board.pieceAt(_).isEmpty)

    def validateForPawn: MovePatternOrError = {
      lazy val isFirstMove = List(`2`, `7`).contains(move.from.rank)
      lazy val isEnPassantAttack =
        gameState.enPassantSquareOption.contains(move.to)

      val moveAsVector = move.as2DVector

      // We care only about forward moves which patterns are (_, 1) | (0, 2).
      // Move 2 squares forward for white and for black would have the following patterns respectively: (0, 2) and (0, -2).
      // Since we care only about the pattern, we transform the rank delta taking into account which side performed the move.
      val moveAsVectorFromSidePerspective = moveAsVector match {
        case (fileDelta, rankDelta) =>
          val newRankDelta = if (side == White) rankDelta else -rankDelta
          (fileDelta, newRankDelta)
      }

      moveAsVectorFromSidePerspective match {
        case (0, 1) if destinationIsEmpty => Transition.asRight
        case (0, 2) if isFirstMove && destinationIsEmpty && noPiecesBetween() =>
          Transition.asRight
        case (-1, 1) | (1, 1) if !destinationIsEmpty => Attack(move.to).asRight
        case (-1, 1) | (1, 1) if isEnPassantAttack =>
          val toRankIndex = CoordinateRank.indexOf(move.to.rank)
          val attackedPawnRank = CoordinateRank.values(
            toRankIndex + (if (side == White) -1 else 1)
          )
          Attack(move.to.copy(rank = attackedPawnRank)).asRight

        case _ => InvalidMovePattern.asLeft
      }
    }

    def validateForKing: Either[MoveValidationError, MovePattern] = {
      val (fileDelta, rankDelta) = move.as2DVector

      lazy val castlingType: Option[CastlingType] = {
        val castlingType = (fileDelta, rankDelta) match {
          case (2, 0)  => Some(KingSide)
          case (-2, 0) => Some(QueenSide)
          case _       => None
        }
        val kingInitialCoordinate = side match {
          case White => Coordinate(E, `1`)
          case Black => Coordinate(E, `8`)
        }

        for {
          castlingType <- castlingType
          if move.from == kingInitialCoordinate

          rookFile = castlingType match {
            case QueenSide => A
            case KingSide  => H
          }
          rookCoordinate = move.from.copy(file = rookFile)
          if noPiecesBetween(to = rookCoordinate)
        } yield castlingType
      }

      (fileDelta.abs, rankDelta.abs) match {
        case (1, 0) | (0, 1) | (1, 1) =>
          if (destinationIsEmpty) Transition.asRight
          else Attack(move.to).asRight

        case _ =>
          (for {
            castlingType <- castlingType
            if gameState.castingAvailable(side, castlingType)
          } yield Castling(castlingType)).toRight(left = InvalidMovePattern)
      }
    }

    move.piece.pieceType match {
      case Pawn   => validateForPawn
      case King   => validateForKing
      case Queen  => ???
      case Rook   => ???
      case Bishop => ???
      case Knight => ???
    }
  }
}
