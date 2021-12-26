package com.chessonline

import chess.domain.{EvaluateMove, KingIsSafe, ValidateMove}
import multiplayer.RandomService
import multiplayer.players.{AuthMiddleware, PlayerRoutes, PlayerService}
import multiplayer.rooms.{RoomRoutes, RoomService}

import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.typelevel.ci.CIString

import scala.concurrent.ExecutionContext
import scala.util.Properties

object Server {
  def start[F[_]: ConcurrentEffect: Timer]: F[ExitCode] =
    for {
      evaluateMove ← ConcurrentEffect[F].pure {
        val validateMove = ValidateMove()
        EvaluateMove(validateMove, KingIsSafe(validateMove))
      }

      playerService ← PlayerService.of[F]
      randomService ← RandomService.of[F]
      roomService ← RoomService.of[F](evaluateMove, randomService)
      authMiddleware = AuthMiddleware(playerService)

      httpApp =
        CORS.policy
          .withAllowHeadersIn(
            Set("Content-Type", "Authorization", "*").map(CIString.apply)
          )
          .apply(
            List(
              ServerHealthRoutes[F],
              PlayerRoutes(playerService, authMiddleware),
              RoomRoutes(roomService, playerService, authMiddleware)
            ).reduce(_ <+> _).orNotFound
          )

      port ← ConcurrentEffect[F].delay(
        sys.env.get("PORT").flatMap(_.toIntOption).getOrElse(8080)
      )

      ec <-
        BlazeServerBuilder[F](ExecutionContext.global)
          .bindHttp(port = port, "0.0.0.0")
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
    } yield ec
}
