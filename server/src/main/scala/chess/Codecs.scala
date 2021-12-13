package com.chessonline
package chess

import chess.domain._

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps

object Codecs {
  implicit val coordinateCodec: Codec[Coordinate] = Codec.from(
    decodeA = Decoder.decodeString.emap(
      Coordinate.fromString(_).toRight("Invalid coordinate")
    ),
    encodeA = Encoder.encodeString.contramap(coordinate =>
      s"${coordinate.file.tag}${coordinate.rank.tag}"
    )
  )

  implicit val sideCodec: Codec[Side] = Codec.from(
    decodeA = Decoder.decodeString.emap(s =>
      Side.values.find(_.tag == s).toRight("Invalid side")
    ),
    encodeA = Encoder.encodeString.contramap(_.tag)
  )

  implicit val pieceTypeCodec: Codec[PieceType] = Codec.from(
    decodeA = Decoder.decodeString.emap(tag =>
      PieceType.values
        .find(_.tag == tag)
        .toRight("Invalid piece type")
    ),
    encodeA = Encoder.encodeString.contramap(_.tag)
  )

  implicit val pieceCodec: Codec[Piece] = deriveCodec

  implicit val chessboardEncoder: Encoder[Chessboard] =
    Encoder.encodeString.contramap(_.toFEN)

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
    Encoder.forProduct3("status", "movesNow", "board")(state =>
      (state.status, state.movesNow, state.board)
    )

  implicit val moveDecoder: Decoder[Move] = deriveDecoder
}
