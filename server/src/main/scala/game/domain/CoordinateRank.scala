package com.chessonline
package game.domain

import enumeratum._

sealed trait CoordinateRank extends EnumEntry

object CoordinateRank extends Enum[CoordinateRank] {
  val values: IndexedSeq[CoordinateRank] = findValues

  case object One extends CoordinateRank
  case object Two extends CoordinateRank
  case object Three extends CoordinateRank
  case object Four extends CoordinateRank
  case object Five extends CoordinateRank
  case object Six extends CoordinateRank
  case object Seven extends CoordinateRank
  case object Eight extends CoordinateRank
}
