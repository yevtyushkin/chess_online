package com.chessonline
package chess.domain

import chess.domain.Side._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._

/** Represents a chessboard.
  * @param squares holds [[Square]]s associated with [[Coordinate]]s.
  */
final case class Chessboard(squares: Map[Coordinate, Square]) {
//  def apply(c: Coordinate) TODO
}

/** A factory for [[Chessboard]] instances. */
object Chessboard {

  /** Constructs a [[Chessboard]] with a starting chess game position. */
  def initial: Chessboard = {
    val squares = for {
      file <- CoordinateFile.values
      rank <- CoordinateRank.values

      pieceOption = for {
        pieceType <- rank match {
          case `2` | `7` => Some(Pawn)
          case `1` | `8` =>
            Some(file match {
              case A | H => Rook
              case B | G => Knight
              case C | F => Bishop
              case D     => Queen
              case E     => King
            })
          case _ => None
        }

        pieceSide = if (rank == `1` || rank == `2`) White else Black
      } yield Piece(pieceSide, pieceType)
    } yield (Coordinate(file, rank), Square(pieceOption))

    Chessboard(Map.from(squares))
  }
}
