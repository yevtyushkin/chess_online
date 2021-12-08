package com.chessonline
package multiplayer.events

import multiplayer.domain.{RoomId, RoomName}

sealed trait RoomManagementEvent

object RoomManagementEvent {
  final case class AddRoom(name: RoomName) extends RoomManagementEvent
  final case class ConnectRoom(id: RoomId) extends RoomManagementEvent
}
