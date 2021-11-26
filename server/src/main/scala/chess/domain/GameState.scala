package com.chessonline
package chess.domain

import chess.domain.GameStatus.GameContinues
import chess.domain.Side._

final case class GameState(
    status: GameStatus,
    movesNow: Side,
    board: Chessboard,
    castlingsForWhite: List[CastlingType],
    castlingsForBlack: List[CastlingType],
    enPassantCoordinateOption: Option[Coordinate]
) {
  def castingsAvailable: List[CastlingType] =
    if (movesNow == White) castlingsForWhite
    else castlingsForBlack

  def updateCastlings(newCastlings: List[CastlingType]): GameState =
    if (movesNow == White) copy(castlingsForWhite = newCastlings)
    else copy(castlingsForBlack = newCastlings)
}

object GameState {
  def initial: GameState = GameState(
    movesNow = White,
    status = GameContinues,
    board = Chessboard.initial,
    castlingsForWhite = CastlingType.values.toList,
    castlingsForBlack = CastlingType.values.toList,
    enPassantCoordinateOption = None
  )
}
