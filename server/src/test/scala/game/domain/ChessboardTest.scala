package com.chessonline
package game.domain

import game.domain.Color._
import game.domain.CoordinateFile._
import game.domain.CoordinateRank._
import game.domain.PieceType._
import game.domain.Square._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.language.implicitConversions

class ChessboardTest extends AnyFreeSpec {
  "A Chessboard" - {
    "initial" - {
      val initial = Chessboard.initial

      "should contain 64 squares" in {
        initial.squares.size shouldEqual 64
      }

      "should contain 32 squares with pieces" in {
        initial.squares.count {
          case (_, _: SquareWithPiece) => true
          case _                       => false
        } shouldEqual 32
      }

      "white side should" - {
        behave like correctlyPlacedSide(
          White,
          pawnsRank = Two,
          specialPiecesRank = One
        )
      }

      "black side should" - {
        behave like correctlyPlacedSide(
          Black,
          pawnsRank = Seven,
          specialPiecesRank = Eight
        )
      }

      def correctlyPlacedSide(
          color: Color,
          pawnsRank: CoordinateRank,
          specialPiecesRank: CoordinateRank
      ): Unit = {
        "pawns should" - {
          behave like correctlyPlacedPieces(
            Piece(color, Pawn),
            CoordinateFile.values.map(Coordinate(_, pawnsRank))
          )
        }

        val makeSpecialPieceCoordinate =
          (f: CoordinateFile) => Coordinate(f, specialPiecesRank)

        "rooks should" - {
          val rookCoordinates = List(A, H).map(makeSpecialPieceCoordinate)

          behave like correctlyPlacedPieces(
            Piece(color, Rook),
            rookCoordinates
          )
        }

        "knights should" - {
          val knightCoordinates = List(B, G).map(makeSpecialPieceCoordinate)

          behave like correctlyPlacedPieces(
            Piece(color, Knight),
            knightCoordinates
          )
        }

        "bishops should" - {
          val bishopCoordinates = List(C, F).map(makeSpecialPieceCoordinate)

          behave like correctlyPlacedPieces(
            Piece(color, Bishop),
            bishopCoordinates
          )
        }

        "queens should" - {
          behave like correctlyPlacedPieces(
            Piece(color, Queen),
            List(Coordinate(D, specialPiecesRank))
          )
        }

        "kings should" - {
          behave like correctlyPlacedPieces(
            Piece(color, King),
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
              case (_, SquareWithPiece(piece)) => piece == pieceUnderTest
              case _                           => false
            }
            .map { case (coordinate, _) =>
              coordinate
            } should contain theSameElementsAs correctCoordinates
        }
      }
    }
  }
}
