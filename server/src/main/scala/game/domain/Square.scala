package com.chessonline
package game.domain

/** Represents a square on a [[Chessboard]]. */
sealed trait Square

/** Contains all [[Square]] instances. */
object Square {

  /** Represents an empty [[Square]]. */
  case object EmptySquare extends Square

  /** Represents a [[Square]] with some [[Piece]].
    * @param piece a [[Piece]] this square contains.
    */
  final case class SquareWithPiece(piece: Piece) extends Square
}
