import 'package:client/chess/models/side.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'game_status.freezed.dart';
part 'game_status.g.dart';

@Freezed(unionKey: 'tag')
class GameStatus with _$GameStatus {
  const factory GameStatus.gameContinues() = GameContinues;

  const factory GameStatus.draw() = Draw;

  const factory GameStatus.win(Side by) = Win;

  factory GameStatus.fromJson(Map<String, dynamic> json) =>
      _$GameStatusFromJson(json);
}
