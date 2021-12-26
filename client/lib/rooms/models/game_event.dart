import 'package:flutter_stateless_chessboard/flutter_stateless_chessboard.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'game_event.freezed.dart';

@freezed
class GameEvent with _$GameEvent {
  const factory GameEvent.playerReady() = PlayerReady;

  const factory GameEvent.moveMade(ShortMove move) = MoveMade;

  const GameEvent._();

  dynamic toJson() {
    return when(
      playerReady: () => "ready",
      moveMade: (ShortMove move) => {
        'move': {
          'from': move.from,
          'to': move.to,
          'promoteTo': move.promotion
              .map((pieceType) => pieceType.toUpperCase())
              .toNullable(),
        },
      },
    );
  }
}
