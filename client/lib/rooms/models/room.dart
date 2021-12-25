import 'package:client/players/models/player.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'room.freezed.dart';
part 'room.g.dart';

@freezed
class Room with _$Room {
  @JsonSerializable(fieldRename: FieldRename.none, explicitToJson: true)
  const factory Room({
    required String id,
    required String name,
    required List<Player> players,
  }) = _Room;

  static List<Room> listFromJson(List<dynamic> list) {
    return list
        .map((json) => Room.fromJson(json as Map<String, dynamic>))
        .toList();
  }

  factory Room.fromJson(Map<String, dynamic> json) => _$RoomFromJson(json);
}
