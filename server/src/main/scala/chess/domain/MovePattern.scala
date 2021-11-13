package com.chessonline
package chess.domain

sealed trait MovePattern

object MovePattern {
  final case class Transition(
      enPassantCoordinateOption: Option[Coordinate] = None
  ) extends MovePattern

  /** [[Attack.attackedCoordinate]] may not be equal to [[Move.to]] in case of the en passant attack. */
  final case class Attack(attackedCoordinate: Coordinate) extends MovePattern

  final case class Castling(castlingType: CastlingType) extends MovePattern
}
