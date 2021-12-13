package com.chessonline
package multiplayer.domain

import cats.Show

final case class Error(message: String)

object Error {
  object syntax {
    implicit class ErrorOps[A: Show](val value: A) {
      def toError: Error = Error(Show[A].show(value))
    }
  }
}
