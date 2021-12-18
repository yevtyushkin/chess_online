package com.chessonline
package multiplayer.rooms

import multiplayer.rooms.domain.RoomName

sealed trait RoomEvent

object RoomEvent {
  final case class RoomAdded(name: RoomName) extends RoomEvent
}
