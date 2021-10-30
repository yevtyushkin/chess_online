package com.chessonline
package chess.domain

import chess.domain.Side.{Black, White}

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class SideSpec extends AnyFreeSpec {
  "Side" - {
    "opposite" - {
      "correctly returns the opposite side" in {
        Black.opposite shouldEqual White
        White.opposite shouldEqual Black
      }
    }
  }
}
