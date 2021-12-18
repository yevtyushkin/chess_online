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
    enPassantCoordinateOption: Option[Coordinate],
    halfMoveNumber: Int = 0,
    fullMoveNumber: Int = 1
) {
  def castingsAvailable: List[CastlingType] =
    if (movesNow == White) castlingsForWhite
    else castlingsForBlack

  def updateCastlings(newCastlings: List[CastlingType]): GameState =
    if (movesNow == White) copy(castlingsForWhite = newCastlings)
    else copy(castlingsForBlack = newCastlings)

  def toFEN: String = {
    val availableCastlings = {
      if (castlingsForWhite.isEmpty && castlingsForBlack.isEmpty) "-"
      else {
        val makeCastlingsString = (castlings: List[CastlingType]) ⇒
          castlings.map(castlingType ⇒ castlingType.tag).mkString

        val whiteCastlings = makeCastlingsString(castlingsForWhite).toUpperCase
        val blackCastlings = makeCastlingsString(castlingsForBlack)

        s"$whiteCastlings$blackCastlings"
      }
    }

    val enPassantCoordinate = enPassantCoordinateOption match {
      case Some(coordinate) ⇒ s"${coordinate.file.tag}${coordinate.rank.tag}"
      case None ⇒ "-"
    }

    s"${board.toFEN} ${movesNow.tag} $availableCastlings $enPassantCoordinate $halfMoveNumber $fullMoveNumber"
  }
}

object GameState {
  def initial: GameState =
    GameState(
      movesNow = White,
      status = GameContinues,
      board = Chessboard.initial,
      castlingsForWhite = CastlingType.values.toList,
      castlingsForBlack = CastlingType.values.toList,
      enPassantCoordinateOption = None
    )
}
