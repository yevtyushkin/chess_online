package com.chessonline
package chess.domain

final case class Move(
    piece: Piece,
    from: Coordinate,
    to: Coordinate
) {
  def as2DVector: (Int, Int) = {
    val fileIndex: CoordinateFile => Int = CoordinateFile.indexOf
    val rankIndex: CoordinateRank => Int = CoordinateRank.indexOf

    (
      fileIndex(to.file) - fileIndex(from.file),
      rankIndex(to.rank) - rankIndex(from.rank)
    )
  }
}
