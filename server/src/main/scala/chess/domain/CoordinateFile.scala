package com.chessonline
package chess.domain

import enumeratum._

sealed abstract class CoordinateFile(val tag: String) extends EnumEntry

object CoordinateFile extends Enum[CoordinateFile] {
  val values: IndexedSeq[CoordinateFile] = findValues

  case object A extends CoordinateFile("a")
  case object B extends CoordinateFile("b")
  case object C extends CoordinateFile("c")
  case object D extends CoordinateFile("d")
  case object E extends CoordinateFile("e")
  case object F extends CoordinateFile("f")
  case object G extends CoordinateFile("g")
  case object H extends CoordinateFile("h")
}
