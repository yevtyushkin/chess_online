import 'package:client/chess/models/game_state.dart';
import 'package:client/chess/models/side.dart';
import 'package:client/players/models/player.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'room_state.freezed.dart';
part 'room_state.g.dart';

@freezed
class RoomState with _$RoomState {
  const factory RoomState.awaitingFulfillment(
    List<Player> connectedPlayers,
  ) = AwaitingFulfillment;

  const factory RoomState.awaitingPlayersReady(
    List<Player> connectedPlayers,
    List<Player> playersReady,
  ) = AwaitingPlayersReady;

  const factory RoomState.gameStarted(
    Player whiteSidePlayer,
    Player blackSidePlayer,
    GameState gameState,
  ) = GameStarted;

  const factory RoomState.loading() = Loading;

  const factory RoomState.error(
    String message,
  ) = ErrorOccurred;

  factory RoomState.fromJson(Map<String, dynamic> json) =>
      _$RoomStateFromJson(json);
}

class RoomStateConverter
    implements JsonConverter<RoomState, Map<String, dynamic>> {
  const RoomStateConverter();

  @override
  RoomState fromJson(Map<String, dynamic> json) {
    if (json.containsKey('connectedPlayers') &&
        json.containsKey('playersReady')) {
      return AwaitingPlayersReady.fromJson(json);
    }

    if (json.containsKey('connectedPlayers')) {
      return AwaitingFulfillment.fromJson(json);
    }

    if (json.containsKey('gameState')) return GameStarted.fromJson(json);
    if (json.containsKey('message')) return ErrorOccurred.fromJson(json);

    throw StateError("Can't handle the given json: $json");
  }

  @override
  Map<String, dynamic> toJson(RoomState object) => object.toJson();
}

extension IsMovingNow on GameStarted {
  Side playerSide(Player player) {
    if (whiteSidePlayer == player) return Side.white;

    return Side.black;
  }

  bool movesNow(Player player) {
    return (whiteSidePlayer == player && gameState.movesNow == Side.white) ||
        (blackSidePlayer == player && gameState.movesNow == Side.black);
  }
}
