package com.chessonline
package multiplayer.players.domain

sealed trait PlayerEvent

object PlayerEvent {
  final case class PlayerAdded(name: PlayerName)
}
