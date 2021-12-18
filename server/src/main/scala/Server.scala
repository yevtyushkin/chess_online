package com.chessonline

import chess.domain.{EvaluateMove, KingIsSafe, ValidateMove}
import multiplayer.players.{AuthMiddleware, PlayerRoutes, PlayerService}
import multiplayer.rooms.{RoomRoutes, RoomService}

import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Server {
  def start[F[_]: ConcurrentEffect: Timer]: F[ExitCode] =
    for {
      evaluateMove ← ConcurrentEffect[F].pure {
        val validateMove = ValidateMove()
        EvaluateMove(validateMove, KingIsSafe(validateMove))
      }

      playerService ← PlayerService.of[F]
      roomService ← RoomService.of[F](evaluateMove)
      authMiddleware = AuthMiddleware(playerService)

      httpApp = List(
        ServerHealthRoutes[F],
        PlayerRoutes(playerService, authMiddleware),
        authMiddleware(RoomRoutes(roomService))
      ).reduce(_ <+> _).orNotFound

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
