package com.chessonline
package multiplayer.events

import multiplayer.domain.PlayerName

sealed trait PlayerManagementEvent

object PlayerManagementEvent {
  final case class AddPlayer(name: PlayerName)
}
