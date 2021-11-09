package com.chessonline
package chess.domain

import chess.domain.CoordinateFile._

import chess.domain.PieceType._
import chess.domain.Side._

object TestData {
  private def makeChessRow(file: CoordinateFile): Seq[Coordinate] = for {
    rank <- CoordinateRank.values
  } yield Coordinate(file, rank)

  val Seq(a1, a2, a3, a4, a5, a6, a7, a8) = makeChessRow(A)
  val Seq(b1, b2, b3, b4, b5, b6, b7, b8) = makeChessRow(B)
  val Seq(c1, c2, c3, c4, c5, c6, c7, c8) = makeChessRow(C)
  val Seq(d1, d2, d3, d4, d5, d6, d7, d8) = makeChessRow(D)
  val Seq(e1, e2, e3, e4, e5, e6, e7, e8) = makeChessRow(E)
  val Seq(f1, f2, f3, f4, f5, f6, f7, f8) = makeChessRow(F)
  val Seq(g1, g2, g3, g4, g5, g6, g7, g8) = makeChessRow(G)
  val Seq(h1, h2, h3, h4, h5, h6, h7, h8) = makeChessRow(H)

  val whitePawn: Piece = Piece(White, Pawn)
  val whitePawnSquare: Square = Square(Some(whitePawn))

  val blackPawn: Piece = Piece(Black, Pawn)
  val blackPawnSquare: Square = Square(Some(blackPawn))

  val whiteKing: Piece = Piece(White, King)
  val blackKing: Piece = Piece(Black, King)

  val whiteQueen: Piece = Piece(White, Queen)

  val emptyGameState: GameState = TestUtils.createGameState()
}
