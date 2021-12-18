package com.chessonline
package multiplayer.players

import multiplayer.domain.UuidString
import multiplayer.players.domain.{Player, PlayerId}

import cats.Monad
import cats.data.{Kleisli, OptionT}
import org.http4s.Request

object AuthMiddleware {
  def apply[F[_]: Monad](
      playerService: PlayerService[F]
  ): org.http4s.server.AuthMiddleware[F, Player] = {
    def authPlayer: Kleisli[OptionT[F, *], Request[F], Player] =
      Kleisli { request =>
        for {
          playerId <- OptionT.fromOption {
            for {
              idCookie <- request.cookies.find(_.name == "id")
              playerId <-
                UuidString
                  .fromString(idCookie.content)
                  .map(PlayerId)
                  .toOption
            } yield playerId
          }
          player <- OptionT(playerService.getPlayerById(playerId))
        } yield player
      }

    org.http4s.server.AuthMiddleware.withFallThrough(authPlayer)
  }
}
