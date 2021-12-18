package com.chessonline

import multiplayer.players.{AuthMiddleware, PlayerRoutes, PlayerService}
import multiplayer.rooms.RoomRoutes

import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Server {
  def start[F[_]: ConcurrentEffect: Timer]: F[ExitCode] =
    for {
      playerService ← PlayerService.of[F]

      authMiddleware = AuthMiddleware(playerService)
      playerRoutes = PlayerRoutes(playerService, authMiddleware)
      roomRoutes <- RoomRoutes.of[F]

      httpApp = (ServerRoutes.of[F] <+>
          playerRoutes <+>
          authMiddleware(roomRoutes)).orNotFound

      ec <-
        BlazeServerBuilder[F](ExecutionContext.global)
          .bindHttp(8080, "localhost")
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
    } yield ec
}
