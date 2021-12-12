package com.chessonline
package multiplayer.domain

import chess.domain.GameState

sealed trait RoomState

object RoomState {
  final case class AwaitingFulfillment(connectedPlayers: List[Player])
      extends RoomState

  final case class AwaitingPlayersReady(
      connectedPlayers: List[Player],
      playersReady: List[Player]
  ) extends RoomState

  final case class GameStarted(
      whiteSidePlayer: Player,
      blackSidePlayer: Player,
      gameState: GameState
  ) extends RoomState
}
