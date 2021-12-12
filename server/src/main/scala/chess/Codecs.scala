package com.chessonline
package chess

import chess.domain._

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax.EncoderOps

object Codecs {
  implicit val fileCodec: Codec[CoordinateFile] = Codec.from(
    decodeA = Decoder.decodeString.emap(tag =>
      CoordinateFile.values
        .find(_.tag == tag)
        .toRight("Invalid coordinate file")
    ),
    encodeA = Encoder.encodeString.contramap(_.tag)
  )

  implicit val rankCodec: Codec[CoordinateRank] = Codec.from(
    decodeA = Decoder.decodeString.emap(tag =>
      CoordinateRank.values
        .find(_.tag == tag)
        .toRight("Invalid coordinate rank")
    ),
    encodeA = Encoder.encodeString.contramap(_.tag)
  )

  implicit val coordinateCodec: Codec[Coordinate] = deriveCodec

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

  implicit val chessboardEncoder: Encoder[Chessboard] = {
    type PieceAtCoordinate = (Coordinate, Piece)

    implicit val pieceAtCoordinateEncoder: Encoder[PieceAtCoordinate] =
      Encoder.forProduct2("coordinate", "piece")(identity)

    Encoder.encodeList[PieceAtCoordinate].contramap(_.pieceMap.toList)
  }

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
