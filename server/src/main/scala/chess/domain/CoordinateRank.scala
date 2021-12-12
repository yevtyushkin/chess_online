package com.chessonline
package chess.domain

import enumeratum._

sealed abstract class CoordinateRank(val tag: String) extends EnumEntry

object CoordinateRank extends Enum[CoordinateRank] {
  val values: IndexedSeq[CoordinateRank] = findValues

  case object `1` extends CoordinateRank("1")
  case object `2` extends CoordinateRank("2")
  case object `3` extends CoordinateRank("3")
  case object `4` extends CoordinateRank("4")
  case object `5` extends CoordinateRank("5")
  case object `6` extends CoordinateRank("6")
  case object `7` extends CoordinateRank("7")
  case object `8` extends CoordinateRank("8")
}
