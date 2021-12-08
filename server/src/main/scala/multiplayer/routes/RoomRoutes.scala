package com.chessonline
package multiplayer.routes

import multiplayer.domain._
import multiplayer.events.RoomManagementEvent
import multiplayer.events.RoomManagementEvent._

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.implicits.{toFlatMapOps, toFunctorOps, toTraverseOps}
import fs2.concurrent.Topic
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{AuthedRoutes, EntityDecoder, Response}

object RoomRoutes {
  def of[F[_]: Concurrent]: F[AuthedRoutes[Player, F]] = {
    val dsl = Http4sDsl[F]
    import multiplayer.Codec._

    import dsl._
    implicit val encodeRoom: EntityDecoder[F, Room] = jsonOf[F, Room]
    implicit val decodeRoomManagementEvent
        : EntityDecoder[F, RoomManagementEvent] = jsonOf[F, RoomManagementEvent]

    for {
      roomManagers <- Ref.of[F, Map[RoomId, RoomManager[F]]](Map.empty)
      roomsTopic <- Topic[F, List[Room]](Nil)
    } yield {
      def connectToRoom(
          roomManager: RoomManager[F],
          player: Player
      ): F[Response[F]] =
        for {
          connectionResult <- roomManager.connect(player)
          response <- connectionResult match {
            case Left(errorDescription) => BadRequest(errorDescription)
            case Right(connection) =>
              for {
                _ <- updateAvailableRooms()
              } yield connection
          }
        } yield response

      def updateAvailableRooms(): F[Unit] = for {
        rooms <- roomManagers.get
          .map(_.values.map(_.room).toList.sequence)
          .flatten

        _ <- roomsTopic.publish1(rooms)
      } yield ()

      AuthedRoutes.of[Player, F] {
        case GET -> Root / "rooms" as _ =>
          for {
            ws <- WebSocketBuilder[F].build(
              send = roomsTopic
                .subscribe(1)
                .through(
                  _.map(rooms => WebSocketFrame.Text(rooms.asJson.toString))
                ),
              receive = _ => fs2.Stream.eval(Concurrent[F].never)
            )
          } yield ws

        case request @ POST -> Root / "rooms" as player =>
          def onAddRoom(roomName: RoomName): F[Response[F]] = for {
            id <- UuidString.of[F]
            roomId = RoomId(id)
            room = Room(roomId, roomName, Nil)

            roomManager <- RoomManager.of[F](room)
            response <- connectToRoom(
              roomManager,
              player
            )
          } yield response

          def onConnectRoom(roomId: RoomId): F[Response[F]] =
            for {
              roomManagerOpt <- roomManagers.get.map(_.get(roomId))
              request <- roomManagerOpt match {
                case Some(roomManager) =>
                  connectToRoom(roomManager, player)
                case None => BadRequest("A room with such id does not exist")
              }
            } yield request

          for {
            roomManagementEvent <- request.req.as[RoomManagementEvent]
            response <- roomManagementEvent match {
              case AddRoom(roomName)   => onAddRoom(roomName)
              case ConnectRoom(roomId) => onConnectRoom(roomId)
            }
          } yield response
      }
    }
  }
}
