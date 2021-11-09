package com.chessonline
package chess.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.language.postfixOps

class CoordinateSpec extends AnyFreeSpec {
  "Coordinate" - {
    import TestData._

    "as2DPoint" - {
      "should return correct 2d representation" in {
        a1.as2DPoint shouldEqual (0, 0)
        b2.as2DPoint shouldEqual (1, 1)
        h8.as2DPoint shouldEqual (7, 7)
      }
    }

    "allBetween" - {
      import Coordinate._

      "returns correct coordinates" - {
        "for vertical patterns" in {
          allBetween(a1, a1) shouldBe empty
          allBetween(a1, a2) shouldBe empty
          allBetween(a1, a3) shouldEqual List(a2)
          allBetween(a1, a8) should contain theSameElementsAs List(
            a2,
            a3,
            a4,
            a5,
            a6,
            a7
          )
        }

        "for horizontal patterns" in {
          allBetween(a1, a1) shouldBe empty
          allBetween(a1, b1) shouldBe empty
          allBetween(a1, c1) shouldEqual List(b1)
          allBetween(a1, h1) should contain theSameElementsAs List(
            b1,
            c1,
            d1,
            e1,
            f1,
            g1
          )
        }

        "for diagonal patterns" in {
          allBetween(a1, a1) shouldBe empty
          allBetween(a1, b2) shouldBe empty
          allBetween(a1, c3) shouldEqual List(b2)
          allBetween(a1, h8) should contain theSameElementsAs List(
            b2,
            c3,
            d4,
            e5,
            f6,
            g7
          )
        }
      }
    }
  }
}
