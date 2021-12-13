package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.MovePattern._
import chess.domain.MoveValidationError._
import chess.domain.ValidateMove.ErrorOr
import chess.domain.PieceType._
import chess.domain.Side._

import cats.implicits.catsSyntaxEitherId

trait ValidateMove {
  def apply(
      move: Move,
      gameState: GameState
  ): ErrorOr[(Piece, MovePattern)]
}

object ValidateMove {
  type ErrorOr[A] = Either[MoveValidationError, A]

  def apply(): ValidateMove = new ValidateMove {
    override def apply(
        move: Move,
        gameState: GameState
    ): ErrorOr[(Piece, MovePattern)] = for {
      piece <- gameState.board
        .pieceAt(move.from)
        .toRight(NoPieceAtStartingCoordinate)
      _ <- Either.cond(
        test = piece.side == gameState.movesNow,
        right = move,
        left = WrongPieceColor
      )
      _ <- validateStartAndDestinationCoordinates(move, gameState)
      pattern <- validatePattern(piece, move, gameState)
    } yield (piece, pattern)

    def validateStartAndDestinationCoordinates(
        move: Move,
        gameState: GameState
    ): ErrorOr[Unit] = {
      def startAndDestinationCoordinatesDiffer: ErrorOr[Move] =
        Either.cond(
          test = move.from != move.to,
          right = move,
          left = IdenticalStartAndDestinationCoordinates
        )

      def destinationNotTakenByAllyPiece: ErrorOr[Move] =
        Either.cond(
          test = {
            val pieceAtDestination = gameState.board.pieceAt(move.to)
            !pieceAtDestination.exists(piece =>
              piece.side == gameState.movesNow
            )
          },
          right = move,
          left = DestinationTakenByAllyPiece
        )

      for {
        _ <- startAndDestinationCoordinatesDiffer
        _ <- destinationNotTakenByAllyPiece
      } yield ()
    }

    def validatePattern(
        piece: Piece,
        move: Move,
        gameState: GameState
    ): ErrorOr[MovePattern] = {

      val (fileDelta, rankDelta) = move.as2DVector

      def validateForPawn: ErrorOr[MovePattern] = {
        val isFirstMove = List(`2`, `7`).contains(move.from.rank)
        val isEnPassantAttack =
          gameState.enPassantCoordinateOption.contains(move.to)

        // We care only about forward moves which patterns are (_, 1) | (0, 2).
        // Move 2 squares forward for white and for black would have the following patterns respectively: (0, 2) and (0, -2).
        // Since we care only about the pattern, we transform the rank delta taking into account which side performed the move.
        val rankDeltaFromSidePerspective =
          if (piece.side == White) rankDelta
          else -rankDelta

        (fileDelta, rankDeltaFromSidePerspective) match {
          case (0, 1) if destinationIsEmpty => Transition().asRight
          case (0, 2)
              if isFirstMove && destinationIsEmpty && noPiecesBetween() =>
            val enPassantCoordinateRank = move.from.as2DPoint match {
              case (_, rankIndex) =>
                CoordinateRank.values(rankIndex + rankDelta / 2)
            }
            val enPassantCoordinate =
              move.from.copy(rank = enPassantCoordinateRank)
            Transition(Some(enPassantCoordinate)).asRight

          case (-1, 1) | (1, 1) if !destinationIsEmpty =>
            Attack(move.to).asRight

          case (-1, 1) | (1, 1) if isEnPassantAttack =>
            val toRankIndex = CoordinateRank.indexOf(move.to.rank)
            val attackedPawnRank = CoordinateRank.values(
              toRankIndex + (if (piece.side == White) -1 else 1)
            )
            Attack(move.to.copy(rank = attackedPawnRank)).asRight

          case _ => InvalidMovePattern.asLeft
        }
      }

      def validateForKing: Either[MoveValidationError, MovePattern] = {
        def castlingType: Option[CastlingType] = {
          val castlingType = (fileDelta, rankDelta) match {
            case (2, 0)  => Some(KingSide)
            case (-2, 0) => Some(QueenSide)
            case _       => None
          }
          val kingInitialCoordinate = piece.side match {
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

            // is not under attack
            kingPassesFiles = castlingType match {
              case QueenSide => List(E, D, C)
              case KingSide  => List(E, F, G)
            }
            kingPassesCoordinates = kingPassesFiles.map { file =>
              Coordinate(file, kingInitialCoordinate.rank)
            }
            if kingPassesCoordinates.forall { coordinate =>
              !underAttack(
                coordinate,
                bySide = piece.side.opposite,
                gameState
              )
            }
          } yield castlingType
        }

        (fileDelta.abs, rankDelta.abs) match {
          case (1, 0) | (0, 1) | (1, 1) =>
            transitionOrAttack

          case _ =>
            (for {
              castlingType <- castlingType
              if gameState.castingsAvailable.contains(castlingType)
            } yield Castling(castlingType)).toRight(left = InvalidMovePattern)
        }
      }

      def validateForQueen: Either[MoveValidationError, MovePattern] =
        // Available moves(AM) for Queen == AM(Rook) + AM(Bishop)
        validateForRook.orElse(validateForBishop)

      def validateForRook: Either[MoveValidationError, MovePattern] =
        (fileDelta, rankDelta) match {
          case (_, 0) | (0, _) if noPiecesBetween() => transitionOrAttack
          case _                                    => InvalidMovePattern.asLeft
        }

      def validateForBishop: Either[MoveValidationError, MovePattern] = {
        if (fileDelta.abs == rankDelta.abs && noPiecesBetween())
          transitionOrAttack
        else InvalidMovePattern.asLeft
      }

      def validateForKnight: Either[MoveValidationError, MovePattern] =
        (fileDelta.abs, rankDelta.abs) match {
          case (2, 1) | (1, 2) => transitionOrAttack
          case _               => InvalidMovePattern.asLeft
        }

      def destinationIsEmpty: Boolean = gameState.board.pieceAt(move.to).isEmpty

      def noPiecesBetween(
          from: Coordinate = move.from,
          to: Coordinate = move.to
      ): Boolean =
        Coordinate
          .allBetween(from, to)
          .forall(gameState.board.pieceAt(_).isEmpty)

      def transitionOrAttack: ErrorOr[MovePattern] =
        if (destinationIsEmpty) Transition().asRight
        else Attack(move.to).asRight

      def underAttack(
          coordinate: Coordinate,
          bySide: Side,
          gameState: GameState
      ): Boolean =
        gameState.board.pieceMap.exists { case (startingCoordinate, piece) =>
          piece.side == bySide && validatePattern(
            piece,
            Move(from = startingCoordinate, to = coordinate),
            gameState
          ).isRight
        }

      piece.pieceType match {
        case Pawn   => validateForPawn
        case King   => validateForKing
        case Rook   => validateForRook
        case Bishop => validateForBishop
        case Queen  => validateForQueen
        case Knight => validateForKnight
      }
    }
  }
}
