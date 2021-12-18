package com.chessonline
package multiplayer

import multiplayer.domain._

import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Codec, Decoder, Encoder}

object MultiplayerCodecs {
  implicit val uuidStringCodec: Codec[UuidString] = Codec.from(
    encodeA = Encoder.encodeString.contramap[UuidString](_.value),
    decodeA = Decoder.decodeString.emap(UuidString.fromString)
  )

  implicit val errorEncoder: Encoder[Error] = deriveEncoder
}
