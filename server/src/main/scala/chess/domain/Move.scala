package com.chessonline
package chess.domain

/** Represents a move in the chess game.
  * @param piece a [[Piece]] used in this move.
  * @param from a starting [[Coordinate]] of this move.
  * @param to a destination [[Coordinate]] of this move.
  */
final case class Move(
    piece: Piece,
    from: Coordinate,
    to: Coordinate
)
