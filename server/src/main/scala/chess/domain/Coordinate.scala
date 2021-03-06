package com.chessonline
package chess.domain

final case class Coordinate(
    file: CoordinateFile,
    rank: CoordinateRank
) {
  def as2DPoint: (Int, Int) =
    (CoordinateFile.indexOf(file), CoordinateRank.indexOf(rank))
}

object Coordinate {
  def fromString(s: String): Option[Coordinate] = s.toList match {
    case file :: rank :: Nil =>
      for {
        file <- CoordinateFile.values.find(_.tag == file.toString)
        rank <- CoordinateRank.values.find(_.tag == rank.toString)
      } yield Coordinate(file, rank)
    case _ => None
  }

  /** Handles only vertical, horizontal and diagonal patterns. */
  def allBetween(from: Coordinate, to: Coordinate): Seq[Coordinate] = {
    val (fromFileIndex, fromRankIndex) = from.as2DPoint
    val (toFileIndex, toRankIndex) = to.as2DPoint
    val (fileDelta, rankDelta) =
      (toFileIndex - fromFileIndex, toRankIndex - fromRankIndex)

    val maxDelta = math.max(fileDelta.abs, rankDelta.abs)

    if (maxDelta == 0) Seq.empty
    else {
      val fileStepWeight = fileDelta / maxDelta
      val rankStepWeight = rankDelta / maxDelta
      for {
        step <- 1 until maxDelta
      } yield Coordinate(
        CoordinateFile.values(fromFileIndex + (step * fileStepWeight)),
        CoordinateRank.values(fromRankIndex + (step * rankStepWeight))
      )
    }
  }
}
