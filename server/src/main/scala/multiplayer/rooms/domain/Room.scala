package com.chessonline
package multiplayer.rooms.domain

import multiplayer.players.domain.Player

import cats.implicits.catsSyntaxEitherId

final case class Room(
    id: RoomId,
    name: RoomName,
    players: List[Player]
) {
  def connect(player: Player): Either[String, Room] =
    if (players.contains(player)) this.asRight
    else if (players.size < 2) copy(players = player :: players).asRight
    else "Room is already fulfilled".asLeft
}
