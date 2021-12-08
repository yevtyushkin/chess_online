package com.chessonline
package multiplayer.routes

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object ServerRoutes {
  def of[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    def healthCheck: HttpRoutes[F] =
      HttpRoutes.of[F] { case GET -> Root / "health" =>
        Ok("Server up and running")
      }

    healthCheck
  }
}
