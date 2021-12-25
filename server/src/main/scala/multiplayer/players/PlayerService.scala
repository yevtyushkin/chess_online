package com.chessonline
package multiplayer.players

import multiplayer.domain.UuidString
import multiplayer.players.domain._

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits.{toFlatMapOps, toFunctorOps}

trait PlayerService[F[_]] {
  def getAllPlayers: F[List[Player]]

  def addPlayer(playerName: PlayerName): F[PlayerId]

  def getPlayerById(playerId: PlayerId): F[Option[Player]]
}

object PlayerService {
  def of[F[_]: Sync]: F[PlayerService[F]] =
    for {
      allPlayers ← Ref.of[F, Map[PlayerId, Player]](Map.empty)
    } yield new PlayerService[F] {
      override def getAllPlayers: F[List[Player]] =
        allPlayers.get.map(allPlayers ⇒ allPlayers.values.toList)

      override def addPlayer(playerName: PlayerName): F[PlayerId] =
        for {
          playerId ← UuidString.of[F].map(PlayerId.apply)
          player = Player(playerId, playerName)

          _ ← allPlayers.update(allPlayers ⇒ allPlayers + (playerId → player))
        } yield playerId

      override def getPlayerById(playerId: PlayerId): F[Option[Player]] =
        allPlayers.get.map(allPlayers ⇒ allPlayers.get(playerId))
    }
}
