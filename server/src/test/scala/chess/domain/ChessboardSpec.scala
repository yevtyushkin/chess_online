package com.chessonline
package chess.domain

import chess.domain.Side._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.language.implicitConversions

class ChessboardSpec extends AnyFreeSpec {
  "A Chessboard" - {
    "initial" - {
      val initial = Chessboard.initial

      "should contain 64 squares" in {
        initial.squares.size shouldEqual 64
      }

      "should contain 32 squares with pieces" in {
        initial.squares.count {
          case (_, Square(Some(_))) => true
          case _                    => false
        } shouldEqual 32
      }

      "white side should" - {
        behave like correctlyPlacedSide(
          White,
          pawnsRank = `2`,
          specialPiecesRank = `1`
        )
      }

      "black side should" - {
        behave like correctlyPlacedSide(
          Black,
          pawnsRank = `7`,
          specialPiecesRank = `8`
        )
      }

      def correctlyPlacedSide(
          side: Side,
          pawnsRank: CoordinateRank,
          specialPiecesRank: CoordinateRank
      ): Unit = {
        "pawns should" - {
          behave like correctlyPlacedPieces(
            Piece(side, Pawn),
            CoordinateFile.values.map(Coordinate(_, pawnsRank))
          )
        }

        val makeSpecialPieceCoordinate =
          (f: CoordinateFile) => Coordinate(f, specialPiecesRank)

        "rooks should" - {
          val rookCoordinates = List(A, H).map(makeSpecialPieceCoordinate)

          behave like correctlyPlacedPieces(
            Piece(side, Rook),
            rookCoordinates
          )
        }

        "knights should" - {
          val knightCoordinates = List(B, G).map(makeSpecialPieceCoordinate)

          behave like correctlyPlacedPieces(
            Piece(side, Knight),
            knightCoordinates
          )
        }

        "bishops should" - {
          val bishopCoordinates = List(C, F).map(makeSpecialPieceCoordinate)

          behave like correctlyPlacedPieces(
            Piece(side, Bishop),
            bishopCoordinates
          )
        }

        "queens should" - {
          behave like correctlyPlacedPieces(
            Piece(side, Queen),
            List(Coordinate(D, specialPiecesRank))
          )
        }

        "kings should" - {
          behave like correctlyPlacedPieces(
            Piece(side, King),
            List(Coordinate(E, specialPiecesRank))
          )
        }
      }

      def correctlyPlacedPieces(
          pieceUnderTest: Piece,
          correctCoordinates: Seq[Coordinate]
      ): Unit = {
        "have correct coordinates" in {
          initial.squares
            .filter {
              case (_, Square(Some(piece))) => piece == pieceUnderTest
              case _                        => false
            }
            .map { case (coordinate, _) =>
              coordinate
            } should contain theSameElementsAs correctCoordinates
        }
      }
    }
  }
}
