package com.chessonline
package chess.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class MoveSpec extends AnyFreeSpec {
  "Move" - {
    import TestData._

    "coordinateShift" - {
      "should return correct coordinate shift" in {
        Move(whitePawn, a1, a2).coordinateShift shouldEqual (0, 1)
        Move(whitePawn, a1, b1).coordinateShift shouldEqual (1, 0)
        Move(whitePawn, a1, a1).coordinateShift shouldEqual (0, 0)
        Move(whitePawn, a1, b2).coordinateShift shouldEqual (1, 1)
      }
    }
  }
}
