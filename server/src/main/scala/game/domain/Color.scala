package com.chessonline
package game.domain

import enumeratum._

/** Represents a color. */
sealed trait Color extends EnumEntry

/** Contains all [[Color]] instances. */
object Color extends Enum[Color] {
  val values: IndexedSeq[Color] = findValues

  case object White extends Color
  case object Black extends Color
}
