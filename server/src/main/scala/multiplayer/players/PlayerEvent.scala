package com.chessonline
package multiplayer.players

import multiplayer.players.domain.PlayerName

sealed trait PlayerEvent

object PlayerEvent {
  final case class PlayerAdded(name: PlayerName)
}
