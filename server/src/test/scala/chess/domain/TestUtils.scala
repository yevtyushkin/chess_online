package com.chessonline
package chess.domain

import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._
import chess.domain.Side._

/** Holds methods used in testing. */
object TestUtils {

  /** Creates a [[GameState]] using provided parameters. Creates an 'empty' [[GameState]] if default
    * parameters are used.
    */
  def createGameState(
      movesNow: Side = White,
      board: Chessboard = Chessboard(Map.empty),
      castlingsAvailable: List[CastlingType] = Nil,
      enPassantSquareOption: Option[Coordinate] = None
  ): GameState =
    GameState(movesNow, board, castlingsAvailable, enPassantSquareOption)

  /** Creates a [[Piece]] using provided parameters. Creates a [[White]] [[Pawn]] if default
    * parameters are used.
    */
  def createPiece(side: Side = White, pieceType: PieceType = Pawn): Piece =
    Piece(side, pieceType)

  /** Creates a [[Square]] using provided parameters. Creates a [[Square]] with a [[Piece]] created using
    * [[TestUtils.createPiece]] if default parameters are used.
    */
  def createSquare(pieceOption: Option[Piece] = Some(createPiece())): Square =
    Square(pieceOption)

  /** Creates a [[Coordinate]] using provided parameters. Creates a [[Coordinate]] with [[A]] [[Coordinate.file]]
    * and [[`1`]] [[Coordinate.rank]] if default parameters are used.
    */
  def createCoordinate(
      file: CoordinateFile = A,
      rank: CoordinateRank = `1`
  ): Coordinate = Coordinate(file, rank)
}
