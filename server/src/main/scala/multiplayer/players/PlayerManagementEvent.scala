package com.chessonline
package multiplayer.players

import multiplayer.players.domain.PlayerName

sealed trait PlayerManagementEvent

object PlayerManagementEvent {
  final case class PlayerAdded(name: PlayerName)
}
