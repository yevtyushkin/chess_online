package com.chessonline
package chess.domain

import enumeratum._

/** Represents a side of a chess game. */
sealed trait Side extends EnumEntry {

  import Side._

  /** Returns a [[Side]] opposite to this side. */
  def opposite: Side = this match {
    case White => Black
    case Black => White
  }
}

/** Contains all [[Side]] instances. */
object Side extends Enum[Side] {
  val values: IndexedSeq[Side] = findValues

  case object White extends Side
  case object Black extends Side
}
