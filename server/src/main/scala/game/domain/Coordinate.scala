package com.chessonline
package game.domain

/** Represents a coordinate on a [[Chessboard]] that is located on an intersection
  * of some row (called `rank`) and some column (called `file`). Used to identify
  * [[Square]]s on a [[Chessboard]].
  * @param file a chessboard column (file) this coordinate belongs to.
  * @param rank a chessboard row (rank) this coordinate belongs to.
  */
final case class Coordinate(
    file: CoordinateFile,
    rank: CoordinateRank
)
