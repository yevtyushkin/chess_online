package com.chessonline
package game.domain

import game.domain.Color._
import game.domain.CoordinateFile._
import game.domain.CoordinateRank._
import game.domain.PieceType._
import game.domain.Square._

import org.scalatest.Inspectors.forAll
import org.scalatest.freespec.AnyFreeSpec
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

      def correctPiecePlacement(
          pieceUnderTest: Piece,
          correctCoordinates: List[Coordinate],
          correctCount: Int
      ): Unit = {
        s"contain $correctCount piece(s)" in {
          initial.squares.count {
            case (_, SquareWithPiece(piece)) if piece == pieceUnderTest => true
            case _                                                      => false
          } shouldEqual correctCount
        }

        "have correct coordinates" in {
          val expectedSquare = SquareWithPiece(pieceUnderTest)

          forAll(correctCoordinates) { coordinate =>
            initial.squares(coordinate) shouldEqual expectedSquare
          }
        }
      }

      import TestUtils._
      "for white side" - {
        "pawns should" - {
          behave like correctPiecePlacement(
            Piece(White, Pawn),
            List("2a", "2b", "2c", "2d", "2e", "2f", "2g", "2h"),
            8
          )
        }

        "rooks should" - {
          behave like correctPiecePlacement(
            Piece(White, Rook),
            List("1a", "1h"),
            2
          )
        }

        "knights should" - {
          behave like correctPiecePlacement(
            Piece(White, Knight),
            List("1b", "1g"),
            2
          )
        }

        "bishops should" - {
          behave like correctPiecePlacement(
            Piece(White, Bishop),
            List("1c", "1f"),
            2
          )
        }

        "queens should" - {
          behave like correctPiecePlacement(
            Piece(White, Queen),
            List("1d"),
            1
          )
        }

        "kings should" - {
          behave like correctPiecePlacement(
            Piece(White, King),
            List("1e"),
            1
          )
        }
      }

      "for black side" - {
        "pawns should" - {
          behave like correctPiecePlacement(
            Piece(Black, Pawn),
            List("7a", "7b", "7c", "7d", "7e", "7f", "7g", "7h"),
            8
          )
        }

        "rooks should" - {
          behave like correctPiecePlacement(
            Piece(Black, Rook),
            List("8a", "8h"),
            2
          )
        }

        "knights should" - {
          behave like correctPiecePlacement(
            Piece(Black, Knight),
            List("8b", "8g"),
            2
          )
        }

        "bishops should" - {
          behave like correctPiecePlacement(
            Piece(Black, Bishop),
            List("8c", "8f"),
            2
          )
        }

        "queens should" - {
          behave like correctPiecePlacement(
            Piece(Black, Queen),
            List("8d"),
            1
          )
        }

        "kings should" - {
          behave like correctPiecePlacement(
            Piece(Black, King),
            List("8e"),
            1
          )
        }
      }
    }
  }

  private object TestUtils {
    implicit def toCoordinate(s: String): Coordinate = {
      val rank = s(0) match {
        case '1' => One
        case '2' => Two
        case '3' => Three
        case '4' => Four
        case '5' => Five
        case '6' => Six
        case '7' => Seven
        case '8' => Eight
      }
      val file = s(1).toLower match {
        case 'a' => A
        case 'b' => B
        case 'c' => C
        case 'd' => D
        case 'e' => E
        case 'f' => F
        case 'g' => G
        case 'h' => H
      }
      Coordinate(file, rank)
    }
  }
}
