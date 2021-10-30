package com.chessonline
package chess.domain

/** Represents a square on a [[Chessboard]].
  * @param pieceOption represents a presence or an absence of a piece on this square.
  */
final case class Square(pieceOption: Option[Piece])
