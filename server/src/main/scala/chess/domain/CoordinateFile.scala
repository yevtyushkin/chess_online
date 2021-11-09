package com.chessonline
package chess.domain

import enumeratum._

sealed trait CoordinateFile extends EnumEntry

object CoordinateFile extends Enum[CoordinateFile] {
  val values: IndexedSeq[CoordinateFile] = findValues

  case object A extends CoordinateFile
  case object B extends CoordinateFile
  case object C extends CoordinateFile
  case object D extends CoordinateFile
  case object E extends CoordinateFile
  case object F extends CoordinateFile
  case object G extends CoordinateFile
  case object H extends CoordinateFile
}
