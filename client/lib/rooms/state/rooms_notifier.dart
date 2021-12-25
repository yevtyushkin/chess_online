import 'dart:async';

import 'package:client/players/models/player.dart';
import 'package:client/rooms/client/rooms_client.dart';
import 'package:client/rooms/models/room.dart';
import 'package:desktop/desktop.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

class RoomsNotifier extends ChangeNotifier {
  RoomsNotifier(this._roomsClient);

  final RoomsClient _roomsClient;

  List<Room> _rooms = [];

  List<Room> get rooms => UnmodifiableListView(_rooms);

  StreamSubscription<List<Room>>? _roomsSubscription;

  Player? _player;

  void onPlayerLoggedIn(Player player) {
    _player = player;

    _listenForAvailableRooms();
  }

  void _listenForAvailableRooms() {
    _roomsSubscription = _roomsClient.roomsStream().listen(
          _availableRoomsListener,
        );
  }

  void _availableRoomsListener(List<Room> availableRooms) {
    _rooms = availableRooms;

    notifyListeners();
  }

  Future<void> addRoom(String roomName) async {
    _withPlayer(
      (player) async {
        await _roomsClient.addRoom(roomName, player.id);
      },
    );
  }

  void _withPlayer<T>(T Function(Player) f) {
    final player = _player;

    if (player == null) return;

    f(player);
  }

  @override
  Future<void> dispose() async {
    _roomsSubscription?.cancel();

    super.dispose();
  }
}
