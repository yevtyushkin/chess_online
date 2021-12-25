package com.chessonline
package multiplayer.rooms

import multiplayer.players.PlayerService
import multiplayer.players.domain.{Player, PlayerId}
import multiplayer.rooms.RoomEvent._
import multiplayer.rooms.domain.{Room, RoomId}

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.{toFlatMapOps, toFunctorOps, toSemigroupKOps}
import fs2.Pipe
import io.circe.syntax.EncoderOps
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{AuthedRoutes, EntityDecoder, EntityEncoder, HttpRoutes}

object RoomRoutes {
  def apply[F[_]: Concurrent](
      roomService: RoomService[F],
      playerService: PlayerService[F],
      authMiddleware: AuthMiddleware[F, Player]
  ): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import RoomCodecs._
    import dsl._

    implicit val decodeRoom: EntityDecoder[F, Room] = jsonOf[F, Room]
    implicit val encodeRoomId: EntityEncoder[F, RoomId] =
      jsonEncoderOf[F, RoomId]
    implicit val decodeRoomAdded: EntityDecoder[F, RoomAdded] =
      jsonOf[F, RoomAdded]

    HttpRoutes.of[F] {
      case GET -> Root / "rooms" =>
        val send: Pipe[F, List[Room], WebSocketFrame] =
          stream =>
            stream.map(rooms => WebSocketFrame.Text(rooms.asJson.toString))

        for {
          ws <- WebSocketBuilder[F].build(
            send = roomService.subscribeForAvailableRooms.through(send),
            receive = _ => fs2.Stream.eval(Concurrent[F].never)
          )
        } yield ws

      case GET -> Root / "rooms" / "connect" / RoomId(roomId) / PlayerId(
            playerId
          ) =>
        (for {
          player ← EitherT.fromOptionF(
            playerService.getPlayerById(playerId),
            ifNone = "A player with such id does not exist"
          )
          roomManager ← EitherT.fromOptionF(
            roomService.getRoomManager(roomId),
            ifNone = "A room with such id does not exist"
          )
          connectionResult ← EitherT(roomManager.connect(player))
        } yield connectionResult).valueOrF(error ⇒ BadRequest(error))
    } <+> authMiddleware(
      AuthedRoutes.of[Player, F] {
        case request @ POST -> Root / "rooms" as _ =>
          for {
            roomName <- request.req.as[RoomAdded].map(_.name)
            roomId ← roomService.addRoom(roomName)

            response <- Created(roomId)
          } yield response
      }
    )
  }
}
