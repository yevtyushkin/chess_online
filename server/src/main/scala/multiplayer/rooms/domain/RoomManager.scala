package com.chessonline
package multiplayer.rooms.domain

import chess.domain.Side.White
import chess.domain.{EvaluateMove, GameState, Move}
import multiplayer.RandomService
import multiplayer.domain.Error
import multiplayer.domain.Error.syntax.ErrorOps
import multiplayer.players.domain.Player
import multiplayer.rooms.GameEvent
import multiplayer.rooms.GameEvent._
import multiplayer.rooms.RoomCodecs._
import multiplayer.rooms.domain.RoomState._

import cats.data.EitherT
import cats.effect.concurrent.Ref
import cats.effect.Concurrent
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import fs2.Pipe
import fs2.concurrent.SignallingRef
import io.circe.parser._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

trait RoomManager[F[_]] {
  def room: F[Room]

  def connect(player: Player): F[Either[String, Response[F]]]
}

object RoomManager {
  def of[F[_]: Concurrent](
      room: Room,
      evaluateMove: EvaluateMove,
      randomService: RandomService[F],
      onPlayerConnected: F[Unit]
  ): F[RoomManager[F]] =
    for {
      roomRef <- Ref.of[F, Room](room)
      roomStateRef <- SignallingRef[F, RoomState](
        AwaitingFulfillment(room.players)
      )
    } yield new RoomManager[F] {
      override def room: F[Room] = roomRef.get

      override def connect(player: Player): F[Either[String, Response[F]]] = {
        import multiplayer.MultiplayerCodecs._

        val reply: Pipe[F, RoomState, WebSocketFrame] = stream =>
          stream.map(state => WebSocketFrame.Text(state.asJson.toString))

        def receive(
            errorQueue: fs2.concurrent.Queue[F, Error]
        ): Pipe[F, WebSocketFrame, Unit] =
          _.evalMap {
            case WebSocketFrame.Text(message, _) =>
              (for {
                gameEvent <-
                  EitherT
                    .fromEither(
                      decode[GameEvent](message).left.map(error =>
                        Error(error.getMessage)
                      )
                    )
                action <- handleGameEvent(gameEvent, player)
              } yield action).valueOrF(errorQueue.enqueue1)

            case _ => errorQueue.enqueue1("Unknown message format".toError)
          }

        def onPlayerConnect(newRoom: Room): F[Unit] =
          for {
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

                roomStateRef.set(newState)

              case _ => Concurrent[F].unit
            }
          } yield action

        (for {
          room <- EitherT.liftF(room)
          errorQueue <- EitherT.liftF(fs2.concurrent.Queue.bounded[F, Error](5))
          roomWithPlayerAdded <- EitherT.fromEither(room.connect(player))
          websocketConnection <- EitherT.right[String](
            for {
              _ <- onPlayerConnect(roomWithPlayerAdded)
              _ <- roomRef.set(roomWithPlayerAdded)
              _ ← onPlayerConnected

              response <- WebSocketBuilder[F].build(
                send = roomStateRef.discrete
                  .through(reply)
                  .merge(
                    errorQueue.dequeue
                      .map(error => WebSocketFrame.Text(error.asJson.toString))
                  ),
                receive = receive(errorQueue)
              )
            } yield response
          )
        } yield websocketConnection).value
      }

      def handleGameEvent(
          event: GameEvent,
          player: Player
      ): EitherT[F, Error, Unit] = {
        def onPlayerReady(
            state: AwaitingPlayersReady
        ): EitherT[F, Error, Unit] = {
          def startGame(
              firstPlayer: Player,
              secondPlayer: Player
          ): EitherT[F, Error, Unit] =
            for {
              firstPlayerPlaysWhiteSide <- EitherT.liftF(randomService.nextBool)

              shuffle =
                if (firstPlayerPlaysWhiteSide) (firstPlayer, secondPlayer)
                else (secondPlayer, firstPlayer)
              state = (GameStarted(_, _, GameState.initial)).tupled(shuffle)

              result <- EitherT.right[Error](roomStateRef.set(state))
            } yield result

          state.playersReady match {
            case List(opponent) if player != opponent =>
              startGame(opponent, player)
            case Nil =>
              EitherT.right[Error](
                roomStateRef.set(state.copy(playersReady = List(player)))
              )
            case _ =>
              EitherT.left("You are already marked as ready".toError.pure)
          }
        }

        def onMoveMade(
            state: GameStarted,
            move: Move
        ): EitherT[F, Error, Unit] =
          for {
            _ <- EitherT.cond(
              {
                val movesNow = state.gameState.movesNow
                val playerThatMovesNow =
                  if (movesNow == White) state.whiteSidePlayer
                  else state.blackSidePlayer

                playerThatMovesNow == player
              },
              (),
              "Move not in turn".toError
            )

            gameStateAfterMove <- EitherT.fromEither(
              evaluateMove(move, state.gameState).left.map(_.toError)
            )
            newRoomState = state.copy(gameState = gameStateAfterMove)
            result <- EitherT.right[Error](
              roomStateRef.set(newRoomState)
            )
          } yield result

        for {
          roomState <- EitherT.liftF(roomStateRef.get)
          result <- (roomState, event) match {
            case (state: AwaitingPlayersReady, PlayerReady) =>
              onPlayerReady(state)

            case (state: GameStarted, MoveMade(move)) => onMoveMade(state, move)
            case _                                    => EitherT.left("Can't handle the given event".toError.pure)
          }
        } yield result
      }
    }
}
