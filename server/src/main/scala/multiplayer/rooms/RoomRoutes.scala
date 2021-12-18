package com.chessonline
package multiplayer.rooms

import multiplayer.players.domain.Player
import multiplayer.rooms.RoomEvent._
import multiplayer.rooms.domain.{Room, RoomId}

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.{toFlatMapOps, toFunctorOps}
import fs2.Pipe
import io.circe.syntax.EncoderOps
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{AuthedRoutes, EntityDecoder, EntityEncoder}

object RoomRoutes {
  def apply[F[_]: Concurrent](
      roomService: RoomService[F]
  ): AuthedRoutes[Player, F] = {
    val dsl = Http4sDsl[F]
    import RoomCodecs._
    import dsl._

    implicit val decodeRoom: EntityDecoder[F, Room] = jsonOf[F, Room]
    implicit val encodeRoomId: EntityEncoder[F, RoomId] =
      jsonEncoderOf[F, RoomId]
    implicit val decodeRoomAdded: EntityDecoder[F, RoomAdded] =
      jsonOf[F, RoomAdded]

    AuthedRoutes.of[Player, F] {
      case GET -> Root / "rooms" as _ =>
        val send: Pipe[F, List[Room], WebSocketFrame] =
          stream =>
            stream.map(rooms => WebSocketFrame.Text(rooms.asJson.toString))

        for {
          ws <- WebSocketBuilder[F].build(
            send = roomService.subscribeForAvailableRooms.through(send),
            receive = _ => fs2.Stream.eval(Concurrent[F].never)
          )
        } yield ws

      case request @ POST -> Root / "rooms" as _ =>
        for {
          roomName <- request.req.as[RoomAdded].map(_.name)
          roomId ← roomService.addRoom(roomName)

          response <- Created(roomId)
        } yield response

      case GET -> Root / "rooms" / "connect" / RoomId(roomId) as player =>
        (for {
          roomManager ← EitherT.fromOptionF(
            roomService.getRoomManager(roomId),
            ifNone = "A room with such id does not exist"
          )
          connectionResult ← EitherT(roomManager.connect(player))
        } yield connectionResult).valueOrF(error ⇒ BadRequest(error))
    }
  }
}
