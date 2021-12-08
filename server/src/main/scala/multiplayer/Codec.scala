package com.chessonline
package multiplayer

import multiplayer.domain._
import multiplayer.events.PlayerManagementEvent.AddPlayer
import multiplayer.events.RoomManagementEvent
import multiplayer.events.RoomManagementEvent.{AddRoom, ConnectRoom}

import cats.implicits.toFunctorOps
import com.chessonline.multiplayer.domain.RoomState.{
  AwaitingFulfillment,
  AwaitingPlayersReady,
  GameStarted
}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

object Codec {
  implicit val addPlayerDecoder: Decoder[AddPlayer] =
    deriveDecoder[AddPlayer]

  implicit val uuidStringDecoder: Decoder[UuidString] =
    Decoder.decodeString.emap(UuidString.fromString)

  implicit val uuidStringEncoder: Encoder[UuidString] =
    Encoder.encodeString.contramap[UuidString](_.value)

  implicit val playerIdEncoder: Encoder[PlayerId] =
    uuidStringEncoder.contramap[PlayerId](_.value)

  implicit val playerIdDecoder: Decoder[PlayerId] =
    uuidStringDecoder.map(PlayerId)

  implicit val playerNameEncoder: Encoder[PlayerName] =
    Encoder.encodeString.contramap[PlayerName](_.value)

  implicit val playerNameDecoder: Decoder[PlayerName] =
    Decoder.decodeString.map(PlayerName.apply)

  implicit val playerEncoder: Encoder[Player] = deriveEncoder[Player]

  implicit val playerDecoder: Decoder[Player] = deriveDecoder[Player]

  implicit val roomIdEncoder: Encoder[RoomId] =
    uuidStringEncoder.contramap(_.value)

  implicit val roomIdDecoder: Decoder[RoomId] = uuidStringDecoder.map(RoomId)

  implicit val roomNameEncoder: Encoder[RoomName] =
    Encoder.encodeString.contramap(_.value)

  implicit val roomNameDecoder: Decoder[RoomName] =
    Decoder.decodeString.map(RoomName)

  implicit val roomEncoder: Encoder[Room] = deriveEncoder[Room]

  implicit val roomDecoder: Decoder[Room] = deriveDecoder[Room]

  implicit val addRoomDecoder: Decoder[AddRoom] = deriveDecoder[AddRoom]

  implicit val connectRoomDecoder: Decoder[ConnectRoom] =
    deriveDecoder[ConnectRoom]

  implicit val decodeRoomManagementEvent: Decoder[RoomManagementEvent] =
    List[Decoder[RoomManagementEvent]](
      Decoder[AddRoom].widen,
      Decoder[ConnectRoom].widen
    ).reduceLeft(_ or _)

  implicit val awaitingFulfillmentEncoder: Encoder[AwaitingFulfillment] =
    deriveEncoder

  implicit val awaitingPlayersReady: Encoder[AwaitingPlayersReady] =
    deriveEncoder

  implicit val gameStartedEncoder: Encoder[GameStarted] = deriveEncoder
}
