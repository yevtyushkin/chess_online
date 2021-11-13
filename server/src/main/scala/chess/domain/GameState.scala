package com.chessonline
package chess.domain

import chess.domain.Side._

final case class GameState(
    movesNow: Side,
    board: Chessboard,
    castlingsForWhite: List[CastlingType],
    castlingsForBlack: List[CastlingType],
    enPassantCoordinateOption: Option[Coordinate]
) {
  def castingAvailable(forSide: Side, castlingType: CastlingType): Boolean = {
    val selectFrom =
      if (forSide == White) castlingsForWhite
      else castlingsForBlack

    selectFrom.contains(castlingType)
  }
}

object GameState {
  def initial: GameState = GameState(
    movesNow = White,
    board = Chessboard.initial,
    castlingsForWhite = CastlingType.values.toList,
    castlingsForBlack = CastlingType.values.toList,
    enPassantCoordinateOption = None
  )
}
