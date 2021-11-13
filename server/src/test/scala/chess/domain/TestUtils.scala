package com.chessonline
package chess.domain

import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._
import chess.domain.Side._

import TestData._

object TestUtils {
  def createGameState(
      movesNow: Side = White,
      board: Chessboard = Chessboard(Map.empty),
      castlingsForWhite: List[CastlingType] = Nil,
      castlingsForBlack: List[CastlingType] = Nil,
      enPassantCoordinateOption: Option[Coordinate] = None
  ): GameState =
    GameState(
      movesNow,
      board,
      castlingsForWhite,
      castlingsForBlack,
      enPassantCoordinateOption
    )

  def createPiece(side: Side = White, pieceType: PieceType = Pawn): Piece =
    Piece(side, pieceType)

  def createCoordinate(
      file: CoordinateFile = A,
      rank: CoordinateRank = `1`
  ): Coordinate = Coordinate(file, rank)

  def stateWithPiece(piece: Piece, at: Coordinate)(
      state: GameState = emptyGameState
  ): GameState =
    state.copy(
      board = Chessboard(
        state.board.pieceMap ++ Map(
          at -> piece
        )
      )
    )
}
