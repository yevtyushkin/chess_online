package com.chessonline
package chess.domain

import chess.domain.CastlingType._
import chess.domain.Side._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class CastlingAvailabilitySpec extends AnyFreeSpec {
  "CastlingAvailability" - {
    "all" - {
      "returns all castling availabilities" in {
        CastlingAvailability.all should contain theSameElementsAs
          List(
            CastlingAvailability(White, KingSide),
            CastlingAvailability(White, QueenSide),
            CastlingAvailability(Black, KingSide),
            CastlingAvailability(Black, QueenSide)
          )
      }
    }
  }
}
