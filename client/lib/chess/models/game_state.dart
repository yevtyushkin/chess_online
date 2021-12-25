import 'package:client/chess/models/side.dart';
import 'package:client/rooms/models/game_status.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'game_state.freezed.dart';
part 'game_state.g.dart';

@freezed
class GameState with _$GameState {
  static const initialFEN =
      'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

  @JsonSerializable(explicitToJson: true)
  const factory GameState({
    required GameStatus status,
    required Side movesNow,
    required String fen,
  }) = _GameState;

  factory GameState.fromJson(Map<String, dynamic> json) =>
      _$GameStateFromJson(json);
}
