package com.chessonline
package chess.domain

import chess.domain.Side._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._

final case class Chessboard(squares: Map[Coordinate, Square]) {
  def pieceAt(c: Coordinate): Option[Piece] =
    squares.getOrElse(c, Square(None)).pieceOption
}

object Chessboard {
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
