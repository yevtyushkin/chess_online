package com.chessonline
package multiplayer.rooms

import chess.domain.EvaluateMove
import multiplayer.RandomService
import multiplayer.domain.UuidString
import multiplayer.rooms.domain.{Room, RoomId, RoomManager, RoomName}

import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.implicits.{toFlatMapOps, toFunctorOps, toTraverseOps}
import fs2.concurrent.Topic

trait RoomService[F[_]] {
  def subscribeForAvailableRooms: fs2.Stream[F, List[Room]]

  def addRoom(roomName: RoomName): F[RoomId]

  def getRoomManager(roomId: RoomId): F[Option[RoomManager[F]]]
}

object RoomService {
  def of[F[_]: Concurrent](
      evaluateMove: EvaluateMove,
      randomService: RandomService[F]
  ): F[RoomService[F]] =
    for {
      roomManagers <- Ref.of[F, Map[RoomId, RoomManager[F]]](Map.empty)
      availableRoomsTopic <- Topic[F, List[Room]](Nil)
    } yield new RoomService[F] {
      override def subscribeForAvailableRooms: fs2.Stream[F, List[Room]] =
        availableRoomsTopic.subscribe(1)

      override def addRoom(roomName: RoomName): F[RoomId] =
        for {
          roomId ← UuidString.of[F].map(RoomId.apply)
          room = Room(roomId, roomName, players = Nil)

          roomManager ← RoomManager.of[F](
            room,
            evaluateMove,
            randomService,
            onPlayerConnected = makeAvailableRoomsUpdate
          )

          _ ← roomManagers.update { roomManagers ⇒
            roomManagers + (roomId → roomManager)
          }
          _ ← makeAvailableRoomsUpdate
        } yield roomId

      override def getRoomManager(roomId: RoomId): F[Option[RoomManager[F]]] =
        roomManagers.get.map(roomManagers ⇒ roomManagers.get(roomId))

      def makeAvailableRoomsUpdate: F[Unit] =
        for {
          availableRooms <-
            roomManagers.get
              .flatMap(roomManagers ⇒
                roomManagers
                  .map { case (_, roomManager) ⇒ roomManager.room }
                  .toList
                  .sequence
              )
              .map(rooms ⇒ rooms.filter(room ⇒ room.players.size < 2))

          _ <- availableRoomsTopic.publish1(availableRooms)
        } yield ()
    }
}
