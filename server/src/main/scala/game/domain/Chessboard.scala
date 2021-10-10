package com.chessonline
package game.domain

import CoordinateRank._
import CoordinateFile._
import PieceType._
import Square._
import Color._

final case class Chessboard(squares: Map[Coordinate, Square])

object Chessboard {
  def initial: Chessboard = {
    val squares = for {
      file <- CoordinateFile.values
      rank <- CoordinateRank.values

      piece = for {
        pieceColor <- rank match {
          case One | Two     => Some(White)
          case Seven | Eight => Some(Black)
          case _             => None
        }

        pieceType = rank match {
          case Two | Seven => Pawn
          case _ =>
            file match {
              case A | H => Rook
              case B | G => Knight
              case C | F => Bishop
              case D     => Queen
              case E     => King
            }
        }
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
