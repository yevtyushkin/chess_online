package com.chessonline
package chess.domain

final case class Move(
    from: Coordinate,
    to: Coordinate,
    promoteTo: Option[PieceType] = None
) {
  def as2DVector: (Int, Int) =
    (
      CoordinateFile.indexOf(to.file) - CoordinateFile.indexOf(from.file),
      CoordinateRank.indexOf(to.rank) - CoordinateRank.indexOf(from.rank)
    )
}
