package com.chessonline
package chess.domain

import chess.domain.CoordinateRank._
import chess.domain.MoveValidationError.InvalidMovePattern
import chess.domain.PieceType._
import chess.domain.Side._

import cats.implicits.catsSyntaxEitherId

/** Responsible for validating [[Move]] patterns. */
trait MovePatternValidator {

  /** Validates the given [[Move]] pattern using the given [[GameState]].
    * @param move the [[Move]] to validate.
    * @param gameState the current [[GameState]].
    * @return [[MoveValidationError.InvalidMovePattern]] if the [[Move]] pattern is not valid, or [[MovePattern]] otherwise.
    */
  def validate(
      move: Move,
      gameState: GameState
  ): Either[MoveValidationError, MovePattern]
}

/** The default [[MovePatternValidator]] implementation. */
object MovePatternValidator extends MovePatternValidator {
  import MovePattern._
  type MovePatternOrError = Either[MoveValidationError, MovePattern]

  override def validate(
      move: Move,
      gameState: GameState
  ): MovePatternOrError = {

    /** Returns the [[Side]] performed the given `move`. */
    def side: Side = move.piece.side

    /** Checks whether the [[Move.to]] is empty. */
    def destinationIsEmpty: Boolean = gameState.board(move.to).isEmpty

    /** Checks whether the [[Move.to]] is taken by an enemy piece. */
    def isAttack: Boolean = gameState.board(move.to) match {
      case Some(attackedPiece) => side != attackedPiece.side
      case _                   => false
    }

    /** Checks whether there are any [[Piece]]s between the given `from` and `to`.
      * Handles both diagonal and vertical [[Move]]s.
      */
    def noBarriers(forMove: Move = move): Boolean = {
      val (fileChange, rankChange) = forMove.coordinateShift
      val maxChange = math.max(fileChange.abs, rankChange.abs)

      val initialFileIndex = CoordinateFile.indexOf(forMove.from.file)
      val initialRankIndex = CoordinateRank.indexOf(forMove.from.rank)

      val fileChangePerStep = fileChange / maxChange
      val rankChangePerStep = rankChange / maxChange
      val coordinatesBetween = for {
        step <- 1 until maxChange
      } yield Coordinate(
        CoordinateFile.values(initialFileIndex + step * fileChangePerStep),
        CoordinateRank.values(initialRankIndex + step * rankChangePerStep)
      )

      coordinatesBetween.forall(gameState.board(_).isEmpty)
    }

    /** Validates the [[Pawn]]'s move pattern. */
    def validateForPawn: MovePatternOrError = {
      val isFirstMove = List(`2`, `7`).contains(move.from.rank)
      val isEnPassantAttack = gameState.enPassantSquareOption.contains(move.to)

      val coordinateShift = move.coordinateShift
      val absoluteShift = coordinateShift match {
        case (fileDelta, rankDelta) =>
          val newRankDelta = if (side == White) rankDelta else -rankDelta
          (fileDelta, newRankDelta)
      }

      absoluteShift match {
        case (0, 1) if destinationIsEmpty => Transition.asRight
        case (0, 2) if isFirstMove && destinationIsEmpty && noBarriers() =>
          Transition.asRight
        case (-1, 1) | (1, 1) if isAttack => Attack(move.to).asRight
        case (-1, 1) | (1, 1) if isEnPassantAttack =>
          val toRankIndex = CoordinateRank.indexOf(move.to.rank)
          val attackedPawnRank = CoordinateRank.values(
            toRankIndex + (if (side == White) -1 else 1)
          )
          Attack(move.to.copy(rank = attackedPawnRank)).asRight

        case _ => InvalidMovePattern.asLeft
      }
    }

//    /** Validates the move pattern for [[King]]. */
//    def validateKingMovePattern: Either[MoveValidationError, Move] = {
//      lazy val castlingTypeAndRookCoordinate
//          : Option[(CastlingType, Coordinate)] =
//        (side, move.from, move.to) match {
//          case (White, Coordinate(E, `1`), Coordinate(C, `1`)) =>
//            Some(WhiteQueenSide, Coordinate(A, `1`))
//          case (White, Coordinate(E, `1`), Coordinate(G, `1`)) =>
//            Some(WhiteKingSide, Coordinate(H, `1`))
//          case (Black, Coordinate(E, `8`), Coordinate(C, `8`)) =>
//            Some(BlackQueenSide, Coordinate(A, `8`))
//          case (Black, Coordinate(E, `8`), Coordinate(G, `8`)) =>
//            Some(BlackKingSide, Coordinate(H, `8`))
//          case _ => None
//        }
//
//      val (fileChange, rankChange) = moveDistance()
//      (fileChange.abs, rankChange.abs) match {
//        case (1, 0) | (0, 1) if destinationIsEmpty || isAttack =>
//          Right(move)
//
//        case _ =>
//          castlingTypeAndRookCoordinate match {
//            case Some((castling, rookCoordinate))
//                if gameState.castlingsAvailable.contains(
//                  castling
//                ) && noBarriers(to = rookCoordinate) =>
//              Right(move)
//            case _ => Left(InvalidMovePattern)
//          }
//      }
//    }

    move.piece.pieceType match {
      case Pawn => validateForPawn
//      case King => validateKingMovePattern
      case _ => Left(InvalidMovePattern)
//        case PieceType.King => ???
//        case PieceType.Queen => ???
//        case PieceType.Rook => ???
//        case PieceType.Bishop => ???
//        case PieceType.Knight => ???
    }
  }

}
