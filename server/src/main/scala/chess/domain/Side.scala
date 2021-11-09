package com.chessonline
package chess.domain

import enumeratum._

sealed trait Side extends EnumEntry {

  import Side._

  def opposite: Side = this match {
    case White => Black
    case Black => White
  }
}

object Side extends Enum[Side] {
  val values: IndexedSeq[Side] = findValues

  case object White extends Side
  case object Black extends Side
}
