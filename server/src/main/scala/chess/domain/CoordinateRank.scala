package com.chessonline
package chess.domain

import enumeratum._

/** Represents a rank of a [[Coordinate]]. */
sealed trait CoordinateRank extends EnumEntry

/** Contains all [[CoordinateRank]] instances. */
object CoordinateRank extends Enum[CoordinateRank] {
  val values: IndexedSeq[CoordinateRank] = findValues

  case object `1` extends CoordinateRank
  case object `2` extends CoordinateRank
  case object `3` extends CoordinateRank
  case object `4` extends CoordinateRank
  case object `5` extends CoordinateRank
  case object `6` extends CoordinateRank
  case object `7` extends CoordinateRank
  case object `8` extends CoordinateRank
}
