package com.chessonline
package chess.domain

/** Represents a chess game piece.
  *
  * @param side      a [[Side]] this piece belongs to.
  * @param pieceType a [[PieceType]] of this piece.
  */
final case class Piece(side: Side, pieceType: PieceType)
