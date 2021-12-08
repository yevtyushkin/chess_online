package com.chessonline
package multiplayer.domain

import multiplayer.domain.RoomState.AwaitingFulfillment
import multiplayer.events.GameEvent

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import fs2.concurrent.SignallingRef
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

trait RoomManager[F[_]] {
  def room: F[Room]

  def connect(player: Player): F[Either[String, Response[F]]]

  def handleGameEvent(event: GameEvent): F[Unit]
}

object RoomManager {
  def of[F[_]: Concurrent](room: Room): F[RoomManager[F]] = for {
    roomRef <- Ref.of[F, Room](room)
    roomState <- SignallingRef[F, RoomState](AwaitingFulfillment(room.players))
  } yield new RoomManager[F] {
    override def room: F[Room] = roomRef.get

    override def connect(player: Player): F[Either[String, Response[F]]] = {
      for {
        room <- room
        responseOrError <- room
          .connect(player)
          .map { roomWithPlayer =>
            for {
              _ <- roomRef.set(roomWithPlayer)
            } yield WebSocketBuilder[F].build(
              send = roomState.discrete.map(state =>
                WebSocketFrame.Text(state.asJson.toString)
              ),
              receive = ???
            )
          }
          .pure
      } yield responseOrError
    }

    override def handleGameEvent(event: GameEvent): F[Unit] = {
      event match {
        case GameEvent.MoveMade(move)               => ???
        case GameEvent.PassPawnSelection(pieceType) => ???
      }

      ???
    }
  }
}
