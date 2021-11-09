package com.chessonline
package chess.domain

import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._
import chess.domain.Side._

object TestUtils {
  def createGameState(
      movesNow: Side = White,
      board: Chessboard = Chessboard(Map.empty),
      castlingsForWhite: List[CastlingType] = Nil,
      castlingsForBlack: List[CastlingType] = Nil,
      enPassantSquareOption: Option[Coordinate] = None
  ): GameState =
    GameState(
      movesNow,
      board,
      castlingsForWhite,
      castlingsForBlack,
      enPassantSquareOption
    )

  def createPiece(side: Side = White, pieceType: PieceType = Pawn): Piece =
    Piece(side, pieceType)

  def createSquare(pieceOption: Option[Piece] = Some(createPiece())): Square =
    Square(pieceOption)

  def createCoordinate(
      file: CoordinateFile = A,
      rank: CoordinateRank = `1`
  ): Coordinate = Coordinate(file, rank)
}
