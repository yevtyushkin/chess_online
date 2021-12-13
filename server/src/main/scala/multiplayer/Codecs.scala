package com.chessonline
package multiplayer

import multiplayer.domain.RoomState._
import multiplayer.domain._
import multiplayer.events.PlayerManagementEvent.AddPlayer
import multiplayer.events.{GameEvent, RoomManagementEvent}
import multiplayer.events.RoomManagementEvent.{AddRoom, ConnectRoom}

import cats.implicits.toFunctorOps
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import chess.Codecs._
import multiplayer.events.GameEvent._

object Codecs {
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

  implicit val playerCodec: Codec[Player] = deriveCodec

  implicit val roomIdEncoder: Encoder[RoomId] =
    uuidStringEncoder.contramap(_.value)

  implicit val roomIdDecoder: Decoder[RoomId] = uuidStringDecoder.map(RoomId)

  implicit val roomNameEncoder: Encoder[RoomName] =
    Encoder.encodeString.contramap(_.value)

  implicit val roomNameDecoder: Decoder[RoomName] =
    Decoder.decodeString.map(RoomName)

  implicit val roomCodec: Codec[Room] = deriveCodec[Room]

  implicit val addRoomDecoder: Decoder[AddRoom] = deriveDecoder[AddRoom]

  implicit val roomStateEncoder: Encoder[RoomState] = Encoder.instance {
    state =>
      implicit val awaitingFulfillmentEncoder: Encoder[AwaitingFulfillment] =
        deriveEncoder
      implicit val awaitingPlayersReady: Encoder[AwaitingPlayersReady] =
        deriveEncoder
      implicit val gameStartedEncoder: Encoder[GameStarted] = deriveEncoder

      state match {
        case state: AwaitingFulfillment  => state.asJson
        case state: AwaitingPlayersReady => state.asJson
        case state: GameStarted          => state.asJson
      }
  }

  implicit val gameEventDecoder: Decoder[GameEvent] = {
    val moveMadeDecoder: Decoder[MoveMade] = deriveDecoder
    val passPawnSelectionDecoder: Decoder[PassPawnSelection] = deriveDecoder
    val playerReadyDecoder: Decoder[PlayerReady.type] =
      Decoder.decodeString.emap(str =>
        Either.cond(str == "ready", PlayerReady, "Invalid event")
      )

    List[Decoder[GameEvent]](
      playerReadyDecoder.widen,
      moveMadeDecoder.widen,
      passPawnSelectionDecoder.widen
    ).reduceLeft(_ or _)
  }

  implicit val errorEncoder: Encoder[Error] = deriveEncoder
}
