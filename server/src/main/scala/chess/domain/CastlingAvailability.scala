package com.chessonline
package chess.domain

/** Represents an availability to perform a castling move.
  * @param side         a [[Side]] for which the castling is available.
  * @param castlingType an available [[CastlingType]].
  */
final case class CastlingAvailability(side: Side, castlingType: CastlingType)

/** A factory for [[CastlingAvailability]] instances. */
object CastlingAvailability {

  /** Returns all [[CastlingAvailability]]s. */
  def all: List[CastlingAvailability] =
    (for {
      side <- Side.values
      castlingType <- CastlingType.values
    } yield CastlingAvailability(side, castlingType)).toList
}
