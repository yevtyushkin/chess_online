package com.chessonline
package multiplayer

import cats.effect.Sync
import cats.implicits.toFunctorOps

import scala.util.Random

trait RandomService[F[_]] {
  def nextBool: F[Boolean]
}

object RandomService {
  def of[F[_]: Sync]: F[RandomService[F]] =
    for {
      random ← Sync[F].delay(new Random)
    } yield new RandomService[F] {
      override def nextBool: F[Boolean] = Sync[F].delay(random.nextBoolean())
    }
}
