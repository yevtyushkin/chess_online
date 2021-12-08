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
        val playerOpt = for {
          id <- Sync[F].pure(
            request.cookies
              .find(_.name == "id")
              .flatMap(cookie =>
                UuidString.fromString(cookie.content).map(PlayerId).toOption
              )
          )
          playerOpt <- players.get.map { players =>
            for {
              id <- id
              player <- players.get(id)
            } yield player
          }
        } yield playerOpt

        OptionT(playerOpt)
    }

    org.http4s.server.AuthMiddleware.withFallThrough(authPlayer)
  }
}
