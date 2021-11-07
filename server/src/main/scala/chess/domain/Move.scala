package com.chessonline
package chess.domain

/** Represents a move in the chess game.
  * @param piece a [[Piece]] used in this move.
  * @param from a starting [[Coordinate]] of this move.
  * @param to a destination [[Coordinate]] of this move.
  */
final case class Move(
    piece: Piece,
    from: Coordinate,
    to: Coordinate
) {

  /** Returns the coordinate shift this [[Move]] represents.
    * @return [[(Int, Int)]] tuple where the first value is the [[CoordinateFile]] shift, and the second value is
    * the [[CoordinateRank]] shift.
    */
  def coordinateShift: (Int, Int) = {
    val fileIndex: CoordinateFile => Int = CoordinateFile.indexOf
    val rankIndex: CoordinateRank => Int = CoordinateRank.indexOf

    (
      fileIndex(to.file) - fileIndex(from.file),
      rankIndex(to.rank) - rankIndex(from.rank)
    )
  }
}
