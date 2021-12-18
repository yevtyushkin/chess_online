package com.chessonline
package multiplayer.rooms

import chess.domain.{EvaluateMove, KingIsSafe, ValidateMove}
import multiplayer.domain.UuidString
import multiplayer.players.domain.Player
import multiplayer.rooms.RoomEvent._
import multiplayer.rooms.domain.{Room, RoomId, RoomManager}

import cats.data.{EitherT, OptionT}
import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.implicits.{toFunctorOps, toFlatMapOps, toTraverseOps}
import fs2.Pipe
import fs2.concurrent.Topic
import io.circe.syntax.EncoderOps
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{AuthedRoutes, EntityDecoder, EntityEncoder, Response}

object RoomRoutes {
  def of[F[_]: Concurrent]: F[AuthedRoutes[Player, F]] = {
    val dsl = Http4sDsl[F]
    import RoomCodecs._
    import dsl._

    implicit val decodeRoom: EntityDecoder[F, Room] = jsonOf[F, Room]

    implicit val encodeRoomId: EntityEncoder[F, RoomId] =
      jsonEncoderOf[F, RoomId]
    implicit val decodeAddRoom: EntityDecoder[F, RoomAdded] =
      jsonOf[F, RoomAdded]

    for {
      roomManagers <- Ref.of[F, Map[RoomId, RoomManager[F]]](Map.empty)
      roomsTopic <- Topic[F, List[Room]](Nil)
    } yield {
      def connectToRoom(
          roomManager: RoomManager[F],
          player: Player
      ): F[Response[F]] =
        for {
          connection <- EitherT(roomManager.connect(player))
            .valueOrF(BadRequest.apply(_))
        } yield connection

      def updateAvailableRooms(): F[Unit] =
        for {
          rooms <-
            roomManagers.get
              .map(_.values.map(_.room).toList.sequence)
              .flatten

          availableRooms = rooms.filter(_.players.size < 2)

          _ <- roomsTopic.publish1(availableRooms)
        } yield ()

      AuthedRoutes.of[Player, F] {
        case GET -> Root / "rooms" as _ =>
          val send: Pipe[F, List[Room], WebSocketFrame] =
            stream =>
              stream.map(rooms => WebSocketFrame.Text(rooms.asJson.toString))

          for {
            ws <- WebSocketBuilder[F].build(
              send = roomsTopic
                .subscribe(1)
                .through(send),
              receive = _ => fs2.Stream.eval(Concurrent[F].never)
            )
          } yield ws

        case request @ POST -> Root / "rooms" as _ =>
          for {
            addRoom <- request.req.as[RoomAdded]

            id <- UuidString.of[F]
            roomId = domain.RoomId(id)
            room = Room(roomId, addRoom.name, Nil)

            validateMove = ValidateMove()
            evaluateMove = EvaluateMove(
              validateMove,
              KingIsSafe(validateMove)
            )

            roomManager <- RoomManager.of[F](room, evaluateMove)

            _ <- roomManagers.update(_ + (roomId -> roomManager))
            _ <- updateAvailableRooms()

            response <- Created(roomId)
          } yield response

        case request @ GET -> Root / "rooms" / "connect" as player =>
          (for {
            roomId <- OptionT.fromOption {
              request.req.headers.headers
                .collectFirst {
                  case header if header.name.toString == "room" => header.value
                }
                .flatMap(UuidString.fromString(_).toOption)
                .map(RoomId)
            }
            roomManager <- OptionT(
              roomManagers.get.map(managers => managers.get(roomId))
            )
            response <- OptionT.liftF(connectToRoom(roomManager, player))
          } yield response)
            .getOrElseF(BadRequest("A room with such id does not exist"))
      }
    }
  }
}
