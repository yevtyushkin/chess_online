package com.chessonline
package multiplayer.events

import multiplayer.domain.Player

sealed trait RoomEvent

object RoomEvent {
  final case class PlayerJoined(player: Player) extends RoomEvent
  final case class PlayerReady(player: Player) extends RoomEvent
}
