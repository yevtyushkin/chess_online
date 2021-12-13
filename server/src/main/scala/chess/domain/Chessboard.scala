package com.chessonline
package chess.domain

import chess.domain.Side._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._

import cats.Monoid

import scala.annotation.tailrec

final case class Chessboard(pieceMap: Map[Coordinate, Piece]) {
  def pieceAt(c: Coordinate): Option[Piece] = pieceMap.get(c)

  def toFEN: String = {
    def formatRow(rank: CoordinateRank): String = {
      @tailrec
      def loop(
          acc: String,
          files: List[CoordinateFile],
          empty: Int
      ): String = {
        files match {
          case file :: tail =>
            pieceMap.get(Coordinate(file, rank)) match {
              case Some(piece) =>
                val newAcc = acc + s"${formatEmpty(empty)}${formatPiece(piece)}"
                loop(newAcc, tail, 0)

              case None => loop(acc, tail, empty + 1)
            }

          case Nil => acc + formatEmpty(empty) + (if (rank != `1`) "/" else "")
        }
      }

      def formatPiece(piece: Piece): String = {
        val tag = piece.pieceType.tag
        if (piece.side == White) tag else tag.toLowerCase
      }

      def formatEmpty(empty: Int) = if (empty > 0) empty.toString else ""

      loop("", CoordinateFile.values.toList, 0)
    }

    Monoid.combineAll(CoordinateRank.values.reverse.map(formatRow))
  }
}

object Chessboard {
  def initial: Chessboard = {
    val pieces = for {
      file <- CoordinateFile.values
      rank <- CoordinateRank.values
      piece <- for {
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
    } yield (Coordinate(file, rank), piece)

    Chessboard(Map.from(pieces))
  }
}
