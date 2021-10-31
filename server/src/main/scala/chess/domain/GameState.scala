package com.chessonline
package chess.domain

import chess.domain.Side.White

/** Represents a chess game state.
  *
  * @param movesNow a [[Side]] which moves now.
  * @param board the current [[Chessboard]] state.
  * @param castlingsAvailable a [[List]] with all available castlings for both [[Side]]s.
  * @param enPassantSquareOption an absence or a presence of an en passant [[Square]].
  */
final case class GameState(
    movesNow: Side,
    board: Chessboard,
    castlingsAvailable: List[Castling],
    enPassantSquareOption: Option[Square]
)

/** A factory for [[GameState]] instances. */
object GameState {

  /** Constructs a [[GameState]] representing a start of the chess game. */
  def initial: GameState = GameState(
    movesNow = White,
    board = Chessboard.initial,
    castlingsAvailable = Castling.values.toList,
    enPassantSquareOption = None
  )
}
