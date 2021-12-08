package com.chessonline
package multiplayer.routes

import multiplayer.Codec._
import multiplayer.domain.{Player, PlayerId, UuidString}
import multiplayer.events.PlayerManagementEvent.AddPlayer

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import io.circe.syntax._
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthedRoutes, EntityDecoder, HttpRoutes, ResponseCookie}

object PlayerRoutes {
  def of[F[_]: Sync](
      players: Ref[F, Map[PlayerId, Player]]
  ): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    implicit val decodeAddPlayer: EntityDecoder[F, AddPlayer] =
      jsonOf[F, AddPlayer]

    HttpRoutes.of[F] {
      case GET -> Root / "players" / "debug" =>
        for {
          allPlayers <- players.get
          response <- Ok(allPlayers.values.asJson)
        } yield response

      case request @ POST -> Root / "players" =>
        for {
          addPlayer <- request.as[AddPlayer]
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
