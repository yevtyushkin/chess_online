package com.chessonline
package game.domain

import enumeratum._

sealed trait Color extends EnumEntry

object Color extends Enum[Color] {
  val values: IndexedSeq[Color] = findValues

  case object White extends Color
  case object Black extends Color
}
