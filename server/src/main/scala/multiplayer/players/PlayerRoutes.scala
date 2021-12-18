package com.chessonline
package multiplayer.players

import multiplayer.domain.UuidString
import multiplayer.players.PlayerManagementEvent.PlayerAdded
import multiplayer.players.domain.{Player, PlayerId}

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthedRoutes, EntityDecoder, HttpRoutes, ResponseCookie}

object PlayerRoutes {
  def of[F[_]: Sync](
      players: Ref[F, Map[PlayerId, Player]]
  ): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    import PlayerCodecs._

    implicit val decodeAddPlayer: EntityDecoder[F, PlayerAdded] =
      jsonOf[F, PlayerAdded]

    HttpRoutes.of[F] {
      case GET -> Root / "players" / "debug" =>
        for {
          allPlayers <- players.get
          response <- Ok(allPlayers.values)
        } yield response

      case request @ POST -> Root / "players" =>
        for {
          addPlayer <- request.as[PlayerAdded]
          id <- UuidString.of[F].map(PlayerId)
          player = Player(id, addPlayer.name)

          _ <- players.update(_ + (id -> player))

          response <- Created().map(
            _.addCookie(ResponseCookie(name = "id", content = id.value.value))
          )
        } yield response
    } <+> AuthMiddleware
      .of[F](players)
      .apply(AuthedRoutes.of[Player, F] {
        case GET -> Root / "players" as player => Ok(player.asJson)
      })
  }
}
