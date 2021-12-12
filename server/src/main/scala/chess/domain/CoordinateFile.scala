package com.chessonline
package chess.domain

import enumeratum._

sealed abstract class CoordinateFile(val tag: String) extends EnumEntry

object CoordinateFile extends Enum[CoordinateFile] {
  val values: IndexedSeq[CoordinateFile] = findValues

  case object A extends CoordinateFile("A")
  case object B extends CoordinateFile("B")
  case object C extends CoordinateFile("C")
  case object D extends CoordinateFile("D")
  case object E extends CoordinateFile("E")
  case object F extends CoordinateFile("F")
  case object G extends CoordinateFile("G")
  case object H extends CoordinateFile("H")
}
