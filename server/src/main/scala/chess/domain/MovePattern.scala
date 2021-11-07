package com.chessonline
package chess.domain

import chess.domain.PieceType._
import enumeratum._

/** Represents a pattern of a chess [[Move]]. */
sealed trait MovePattern

/** Contains all [[MovePattern]]s. */
object MovePattern {

  /** Represents a [[Piece]] transition between [[Move.from]] and [[Move.to]]. */
  case object Transition extends MovePattern

  /** Represents an attack of a piece located at `attackedCoordinate`. Note, [[Attack.attackedCoordinate]] may not
    * be equal to [[Move.to]] in case of the en passant attack.
    */
  final case class Attack(attackedCoordinate: Coordinate) extends MovePattern

  /** Represents a castling [[Move]] of the given `castlingType`. */
  final case class Castling(castlingType: CastlingType) extends MovePattern
}
