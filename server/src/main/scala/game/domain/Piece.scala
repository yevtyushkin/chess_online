package com.chessonline
package game.domain

/** Represents a chess game piece.
  * @param color a [[Color]] of this piece.
  * @param pieceType a [[PieceType]] of this piece.
  */
final case class Piece(color: Color, pieceType: PieceType)
