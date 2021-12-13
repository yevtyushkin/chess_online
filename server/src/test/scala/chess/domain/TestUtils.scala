package com.chessonline
package chess.domain

import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.GameStatus.GameContinues
import chess.domain.PieceType._
import chess.domain.Side._
import chess.domain.TestData._

object TestUtils {
  def createGameState(
      movesNow: Side = White,
      status: GameStatus = GameContinues,
      board: Chessboard = Chessboard(Map.empty),
      castlingsForWhite: List[CastlingType] = Nil,
      castlingsForBlack: List[CastlingType] = Nil,
      enPassantCoordinateOption: Option[Coordinate] = None
  ): GameState =
    GameState(
      status,
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

  def nPieces(n: Int, piece: Piece): Seq[Piece] = Seq.fill(n)(piece)

}
