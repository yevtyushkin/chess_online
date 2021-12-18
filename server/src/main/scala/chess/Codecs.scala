package com.chessonline
package chess

import chess.domain._

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps

object Codecs {
  implicit val coordinateCodec: Decoder[Coordinate] =
    Decoder.decodeString.emap(
      Coordinate.fromString(_).toRight("Invalid coordinate")
    )

  implicit val sideCodec: Encoder[Side] = Encoder.encodeString.contramap(_.tag)

  implicit val pieceTypeCodec: Decoder[PieceType] =
    Decoder.decodeString.emap(tag =>
      PieceType.values
        .find(_.tag == tag)
        .toRight("Invalid piece type")
    )

  implicit val gameStatusEncoder: Encoder[GameStatus] = Encoder.instance {
    status =>
      implicit val winEncoder: Encoder.AsObject[GameStatus.Win] =
        Encoder.forProduct2("tag", "by")(status => (status.tag, status.by))

      status match {
        case status @ (GameStatus.GameContinues | GameStatus.Draw) =>
          status.tag.asJson
        case status: GameStatus.Win => status.asJson
      }
  }

  implicit val gameStateEncoder: Encoder[GameState] =
    Encoder.encodeString.contramap(_.toFEN)

  implicit val moveDecoder: Decoder[Move] = deriveDecoder
}
