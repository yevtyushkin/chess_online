package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.MovePattern._
import chess.domain.MoveValidationError._
import chess.domain.PieceType._
import chess.domain.Side._

import cats.implicits.catsSyntaxEitherId

trait MoveValidator {
  type ErrorOr[A] = Either[MoveValidationError, A]

  def validatePieceColor(
      move: Move,
      gameState: GameState
  ): ErrorOr[Move]

  def validateStartAndDestinationCoordinates(
      move: Move,
      gameState: GameState
  ): ErrorOr[Move]

  def validatePattern(
      move: Move,
      gameState: GameState
  ): ErrorOr[MovePattern]
}

object MoveValidator extends MoveValidator {
  override def validateStartAndDestinationCoordinates(
      move: Move,
      gameState: GameState
  ): ErrorOr[Move] = {
    def pieceIsPresentAtStartingCoordinate: ErrorOr[Move] =
      Either.cond(
        test = gameState.board.pieceAt(move.from).contains(move.piece),
        right = move,
        left = AbsentOrWrongPieceAtStartingCoordinate
      )

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
          !pieceAtDestination.exists(_.side == gameState.movesNow)
        },
        right = move,
        left = DestinationSquareTakenByAllyPiece
      )

    for {
      _ <- pieceIsPresentAtStartingCoordinate
      _ <- startAndDestinationCoordinatesDiffer
      _ <- destinationNotTakenByAllyPiece
    } yield move
  }

  override def validatePieceColor(
      move: Move,
      gameState: GameState
  ): ErrorOr[Move] =
    Either.cond(
      test = move.piece.side == gameState.movesNow,
      right = move,
      left = WrongPieceColor
    )

  override def validatePattern(
      move: Move,
      gameState: GameState
  ): ErrorOr[MovePattern] = {
    def noPiecesBetween(
        from: Coordinate = move.from,
        to: Coordinate = move.to
    ): Boolean =
      Coordinate.allBetween(from, to).forall(gameState.board.pieceAt(_).isEmpty)

    lazy val side = move.piece.side
    lazy val oppositeSide = side.opposite
    lazy val destinationIsEmpty = gameState.board.pieceAt(move.to).isEmpty
    lazy val transitionOrAttack =
      if (destinationIsEmpty) Transition().asRight
      else Attack(move.to).asRight
    lazy val (fileDelta, rankDelta) = move.as2DVector
    lazy val absFileDelta = fileDelta.abs
    lazy val absRankDelta = rankDelta.abs

    def validateForPawn: ErrorOr[MovePattern] = {
      lazy val isFirstMove = List(`2`, `7`).contains(move.from.rank)
      lazy val isEnPassantAttack =
        gameState.enPassantSquareOption.contains(move.to)

      // We care only about forward moves which patterns are (_, 1) | (0, 2).
      // Move 2 squares forward for white and for black would have the following patterns respectively: (0, 2) and (0, -2).
      // Since we care only about the pattern, we transform the rank delta taking into account which side performed the move.
      val rankDeltaFromSidePerspective =
        if (side == White) rankDelta
        else -rankDelta

      (fileDelta, rankDeltaFromSidePerspective) match {
        case (0, 1) if destinationIsEmpty => Transition().asRight
        case (0, 2) if isFirstMove && destinationIsEmpty && noPiecesBetween() =>
          val enPassantCoordinateRank = move.from.as2DPoint match {
            case (_, rankIndex) =>
              CoordinateRank.values(rankIndex + rankDelta / 2)
          }
          val enPassantCoordinate =
            move.from.copy(rank = enPassantCoordinateRank)
          Transition(Some(enPassantCoordinate)).asRight
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

          // is not under attack
          kingPassesFiles = castlingType match {
            case QueenSide => List(E, D, C)
            case KingSide  => List(E, F, G)
          }
          kingPassesCoordinates = kingPassesFiles.map { file =>
            Coordinate(file, kingInitialCoordinate.rank)
          }
          if kingPassesCoordinates.forall { coordinate =>
            !underAttack(coordinate, bySide = oppositeSide, gameState)
          }
        } yield castlingType
      }

      (absFileDelta, absRankDelta) match {
        case (1, 0) | (0, 1) | (1, 1) =>
          transitionOrAttack

        case _ =>
          (for {
            castlingType <- castlingType
            if gameState.castingAvailable(side, castlingType)
          } yield Castling(castlingType)).toRight(left = InvalidMovePattern)
      }
    }

    def validateForRook: Either[MoveValidationError, MovePattern] =
      (fileDelta, rankDelta) match {
        case (_, 0) | (0, _) if noPiecesBetween() => transitionOrAttack
        case _                                    => InvalidMovePattern.asLeft
      }

    def validateForBishop: Either[MoveValidationError, MovePattern] = {
      if (absFileDelta == absRankDelta && noPiecesBetween()) transitionOrAttack
      else InvalidMovePattern.asLeft
    }

    def validateForQueen: Either[MoveValidationError, MovePattern] =
      // Available moves(AM) for Queen == AM(Rook) + AM(Bishop)
      validateForRook.orElse(validateForBishop)

    def validateForKnight: Either[MoveValidationError, MovePattern] =
      (absFileDelta, absRankDelta) match {
        case (2, 1) | (1, 2) => transitionOrAttack
        case _               => InvalidMovePattern.asLeft
      }

    move.piece.pieceType match {
      case Pawn   => validateForPawn
      case King   => validateForKing
      case Rook   => validateForRook
      case Bishop => validateForBishop
      case Queen  => validateForQueen
      case Knight => validateForKnight
    }
  }

  private def underAttack(
      coordinate: Coordinate,
      bySide: Side,
      gameState: GameState
  ): Boolean =
    gameState.board.squares.exists {
      case (startingCoordinate, Square(Some(piece))) =>
        piece.side == bySide && validatePattern(
          Move(piece, from = startingCoordinate, to = coordinate),
          gameState
        ).isRight
      case _ => false
    }
}
