package com.chessonline
package multiplayer.events

import chess.domain.{Move, PieceType}

sealed trait GameEvent

object GameEvent {
  case object PlayerReady extends GameEvent
  final case class MoveMade(move: Move) extends GameEvent
  final case class PassPawnSelection(pieceType: PieceType) extends GameEvent
}
