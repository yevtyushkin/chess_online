package com.chessonline

import multiplayer.domain._

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ExitCode, Timer}
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import com.chessonline.multiplayer.players.{AuthMiddleware, PlayerRoutes}
import com.chessonline.multiplayer.players.domain.{Player, PlayerId}
import com.chessonline.multiplayer.rooms.RoomRoutes
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Server {
  def start[F[_]: ConcurrentEffect: Timer]: F[ExitCode] =
    for {
      players <- Ref.of[F, Map[PlayerId, Player]](Map.empty)

      authMiddleware = AuthMiddleware.of[F](players)
      roomRoutes <- RoomRoutes.of[F]

      httpApp = (ServerRoutes.of[F] <+>
          PlayerRoutes.of[F](players) <+>
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
