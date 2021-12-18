package com.chessonline
package multiplayer.rooms

import multiplayer.MultiplayerCodecs.uuidStringCodec
import multiplayer.rooms.GameEvent._
import multiplayer.rooms.RoomEvent.RoomAdded
import multiplayer.rooms.domain.RoomState._
import multiplayer.rooms.domain.{Room, RoomId, RoomName, RoomState}

import cats.implicits.toFunctorOps
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}
import multiplayer.MultiplayerCodecs._
import multiplayer.players.PlayerCodecs._
import chess.Codecs._

object RoomCodecs {
  implicit val roomIdCodec: Codec[RoomId] = Codec.from(
    encodeA = uuidStringCodec.contramap(_.value),
    decodeA = uuidStringCodec.map(RoomId.apply)
  )

  implicit val roomNameCodec: Codec[RoomName] = Codec.from(
    encodeA = Encoder.encodeString.contramap(_.value),
    decodeA = Decoder.decodeString.map(RoomName)
  )

  implicit val roomCodec: Codec[Room] = deriveCodec[Room]

  implicit val roomAddedDecoder: Decoder[RoomAdded] = deriveDecoder[RoomAdded]

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
    val passPawnSelectionDecoder: Decoder[PassPawnSelected] = deriveDecoder
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
}
