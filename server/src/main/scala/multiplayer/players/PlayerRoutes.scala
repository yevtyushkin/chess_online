package com.chessonline
package multiplayer.players

import multiplayer.players.PlayerEvent.PlayerAdded
import multiplayer.players.domain.{Player, PlayerId}

import cats.effect.Sync
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, EntityDecoder, HttpRoutes}

object PlayerRoutes {
  def apply[F[_]: Sync](
      playerService: PlayerService[F],
      authMiddleware: AuthMiddleware[F, Player]
  ): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    import PlayerCodecs._

    implicit val decodePlayerAdded: EntityDecoder[F, PlayerAdded] =
      jsonOf[F, PlayerAdded]

    HttpRoutes.of[F] {
      case GET -> Root / "players" / "debug" =>
        playerService.getAllPlayers.flatMap(Ok.apply(_))

      case request @ POST -> Root / "players" =>
        for {
          playerName <- request.as[PlayerAdded].map(_.name)

          playerId ← playerService.addPlayer(playerName)

          response <- Created(playerId.value.value)
        } yield response
    } <+> authMiddleware(
      AuthedRoutes.of[Player, F] {
        case GET -> Root / "players" as player => Ok(player)
      }
    )
  }
}
