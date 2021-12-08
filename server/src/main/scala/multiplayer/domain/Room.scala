package com.chessonline
package multiplayer.domain

import cats.implicits.catsSyntaxEitherId

final case class Room(
    id: RoomId,
    name: RoomName,
    players: List[Player]
) {
  def connect(player: Player): Either[String, Room] =
    if (players.contains(player)) this.asRight
    else if (players.size < 2) copy(players = player :: players).asRight
    else "Lobby is already fulfilled".asLeft
}
