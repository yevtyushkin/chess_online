package com.chessonline
package multiplayer.domain

import chess.domain.{EvaluateMove, GameState}
import multiplayer.domain.RoomState._
import multiplayer.events.GameEvent
import multiplayer.events.GameEvent._

import cats.data.EitherT
import cats.effect.{Concurrent, Sync}
import cats.effect.concurrent.Ref
import cats.implicits.{toFlatMapOps, toFunctorOps}
import fs2.Pipe
import fs2.concurrent.Topic
import io.circe.parser._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

import scala.util.Random

trait RoomManager[F[_]] {
  def room: F[Room]

  def connect(player: Player): F[Either[String, Response[F]]]
}

object RoomManager {
  def of[F[_]: Concurrent](
      room: Room,
      evaluateMove: EvaluateMove
  ): F[RoomManager[F]] = for {
    roomRef <- Ref.of[F, Room](room)
    roomStateTopic <- Topic[F, RoomState](AwaitingFulfillment(room.players))
    roomStateRef <- Ref.of[F, RoomState](AwaitingFulfillment(room.players))
  } yield new RoomManager[F] {
    override def room: F[Room] = roomRef.get

    override def connect(player: Player): F[Either[String, Response[F]]] = {
      import multiplayer.Codecs._

      val reply: Pipe[F, RoomState, WebSocketFrame] = stream =>
        stream.map(state => WebSocketFrame.Text(state.asJson.toString))

      val receive: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
        case WebSocketFrame.Text(message, _) =>
          (for {
            gameEvent <- EitherT.fromEither(decode[GameEvent](message))
            action <- EitherT.right[io.circe.Error](
              handleGameEvent(gameEvent, player)
            )
          } yield action).valueOr(_ => ())

        case _ => Concurrent[F].unit
      }

      def onPlayerConnect(newRoom: Room): F[Unit] = for {
        roomState <- roomStateRef.get
        action <- roomState match {
          case _: AwaitingFulfillment =>
            val newState =
              if (newRoom.players.size == 2)
                AwaitingPlayersReady(
                  connectedPlayers = newRoom.players,
                  playersReady = List.empty
                )
              else AwaitingFulfillment(newRoom.players)

            for {
              _ <- roomRef.set(newRoom)
              _ <- roomStateRef.set(newState)
              _ <- roomStateTopic.publish1(newState)
            } yield ()

          case _ => Concurrent[F].unit
        }
      } yield action

      (for {
        room <- EitherT.liftF(room)
        roomWithPlayerAdded <- EitherT.fromEither(room.connect(player))
        websocketConnection <- EitherT.right[String](
          for {
            _ <- onPlayerConnect(roomWithPlayerAdded)

            response <- WebSocketBuilder[F].build(
              send = roomStateTopic
                .subscribe(1)
                .through(reply),
              receive = receive
            )
          } yield response
        )
      } yield websocketConnection).value
    }

    def handleGameEvent(event: GameEvent, player: Player): F[Unit] = {
      def startGame(firstPlayer: Player, secondPlayer: Player): F[Unit] = for {
        firstPlayerPlaysWhiteSide <- Sync[F].delay(new Random().nextBoolean())
        shuffle =
          if (firstPlayerPlaysWhiteSide) (firstPlayer, secondPlayer)
          else (secondPlayer, firstPlayer)

        state = (GameStarted(_, _, GameState.initial)).tupled(shuffle)

        _ <- roomStateRef.set(state)
        _ <- roomStateTopic.publish1(state)
      } yield ()

      for {
        roomState <- roomStateRef.get
        action <- (roomState, event) match {
          case (
                state @ AwaitingPlayersReady(_, playersReady),
                PlayerReady
              ) =>
            playersReady match {
              case List(opponent) if player != opponent =>
                startGame(opponent, player)
              case Nil =>
                val newState = state.copy(playersReady = List(player))
                for {
                  _ <- roomStateRef.set(newState)
                  _ <- roomStateTopic.publish1(newState)
                } yield ()

              case _ => Concurrent[F].unit
            }

          case (gameStarted: GameStarted, MoveMade(player))          => ???
          case (gameStarted: GameStarted, PassPawnSelection(player)) => ???
          case _                                                     => Concurrent[F].unit
        }
      } yield action
    }
  }
}
