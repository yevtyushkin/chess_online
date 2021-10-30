package com.chessonline
package game.domain

import CoordinateRank._
import CoordinateFile._
import PieceType._
import Square._
import Color._

/** Represents a chessboard.
  * @param squares holds [[Square]]s associated with [[Coordinate]]s.
  */
final case class Chessboard(squares: Map[Coordinate, Square])

/** A factory for [[Chessboard]] instances. */
object Chessboard {

  /** Constructs a [[Chessboard]] with a starting chess game position. */
  def initial: Chessboard = {
    val squares = for {
      file <- CoordinateFile.values
      rank <- CoordinateRank.values

      piece = for {
        pieceType <- rank match {
          case Two | Seven => Some(Pawn)
          case One | Eight =>
            Some(file match {
              case A | H => Rook
              case B | G => Knight
              case C | F => Bishop
              case D     => Queen
              case E     => King
            })
          case _ => None
        }

        pieceColor =
          if (rank == One || rank == Two) White
          else Black
      } yield Piece(pieceColor, pieceType)

      coordinate = Coordinate(file, rank)
      square = piece match {
        case Some(piece) => SquareWithPiece(piece)
        case None        => EmptySquare
      }
    } yield (coordinate, square)

    Chessboard(Map.from(squares))
  }
}
