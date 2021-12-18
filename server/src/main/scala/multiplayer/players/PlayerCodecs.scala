package com.chessonline
package multiplayer.players

import multiplayer.MultiplayerCodecs.uuidStringCodec
import multiplayer.players.PlayerEvent.PlayerAdded
import multiplayer.players.domain.{Player, PlayerId, PlayerName}

import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}
import io.circe.{Codec, Decoder, Encoder}

object PlayerCodecs {
  implicit val playerIdCodec: Codec[PlayerId] = Codec.from(
    encodeA = uuidStringCodec.contramap[PlayerId](_.value),
    decodeA = uuidStringCodec.map(PlayerId)
  )

  implicit val playerNameCodec: Codec[PlayerName] = Codec.from(
    encodeA = Encoder.encodeString.contramap[PlayerName](_.value),
    decodeA = Decoder.decodeString.map(PlayerName)
  )

  implicit val playerCodec: Codec[Player] = deriveCodec

  implicit val playerAddedDecoder: Decoder[PlayerAdded] = deriveDecoder
}
