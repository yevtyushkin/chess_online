package com.chessonline
package chess.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class MoveSpec extends AnyFreeSpec {
  "Move" - {
    import TestData._

    "as2DVector" - {
      "should return correct vector representation" in {
        Move(whitePawn, a1, a2).as2DVector shouldEqual (0, 1)
        Move(whitePawn, a1, b1).as2DVector shouldEqual (1, 0)
        Move(whitePawn, a1, a1).as2DVector shouldEqual (0, 0)
        Move(whitePawn, a1, b2).as2DVector shouldEqual (1, 1)
      }
    }
  }
}
