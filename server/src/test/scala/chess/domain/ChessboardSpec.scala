package com.chessonline
package chess.domain

import chess.domain.Side._
import chess.domain.CoordinateFile._
import chess.domain.CoordinateRank._
import chess.domain.PieceType._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{contain, have}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.language.{implicitConversions, postfixOps}

class ChessboardSpec extends AnyFreeSpec {
  import TestData._

  "Chessboard" - {
    "pieceAt" - {
      val piece = TestUtils.createPiece()
      val chessboard = Chessboard(Map(a1 -> piece))

      "should return a piece located at the given coordinate" in {
        chessboard.pieceAt(a1) shouldEqual Some(piece)
      }

      "should return none if there is no piece at the given coordinate" in {
        chessboard.pieceAt(b1) shouldEqual None
      }
    }

    "initial" - {
      val initial = Chessboard.initial

      "should contain 32 pieces" in {
        initial.pieceMap should have size 32
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
          initial.pieceMap.collect {
            case square @ (_, piece) if piece == pieceUnderTest => square
          }.keys should contain theSameElementsAs correctCoordinates
        }
      }
    }
  }
}
