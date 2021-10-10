package com.chessonline
package game.domain

sealed trait Square

object Square {
  case object EmptySquare extends Square
  final case class SquareWithPiece(piece: Piece) extends Square
}
