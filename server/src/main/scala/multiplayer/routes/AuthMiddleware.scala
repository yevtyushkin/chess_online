package com.chessonline
package multiplayer.routes

import multiplayer.domain.{Player, PlayerId, UuidString}

import cats.data.{Kleisli, OptionT}
import cats.implicits.{toFlatMapOps, toFunctorOps}
import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.http4s.Request

object AuthMiddleware {
  def of[F[_]: Sync](
      players: Ref[F, Map[PlayerId, Player]]
  ): org.http4s.server.AuthMiddleware[F, Player] = {
    def authPlayer: Kleisli[OptionT[F, *], Request[F], Player] = Kleisli {
      request =>
        for {
          playerId <- OptionT.fromOption(
            for {
              idCookie <- request.cookies.find(_.name == "id")
              playerId <- UuidString
                .fromString(idCookie.content)
                .map(PlayerId)
                .toOption
            } yield playerId
          )
          player <- OptionT(players.get.map(players => players.get(playerId)))
        } yield player
    }

    org.http4s.server.AuthMiddleware.withFallThrough(authPlayer)
  }
}
