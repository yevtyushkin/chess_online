package com.chessonline

import cats.{Applicative, Defer}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object ServerHealthRoutes {
  def apply[F[_]: Defer: Applicative]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    def healthCheck: HttpRoutes[F] =
      HttpRoutes.of[F] {
        case GET -> Root / "health" => Ok("Server up and running")
      }

    healthCheck
  }
}
