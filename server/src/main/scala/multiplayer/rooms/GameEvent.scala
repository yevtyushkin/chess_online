package com.chessonline
package multiplayer.rooms

import chess.domain.{Move, PieceType}

sealed trait GameEvent

object GameEvent {
  case object PlayerReady extends GameEvent
  final case class MoveMade(move: Move) extends GameEvent
}
