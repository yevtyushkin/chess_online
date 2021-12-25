import 'dart:async';

import 'package:client/players/models/player.dart';
import 'package:client/rooms/client/room_manager_client.dart';
import 'package:client/rooms/models/error_message.dart';
import 'package:client/rooms/models/game_event.dart';
import 'package:client/rooms/models/room.dart';
import 'package:client/rooms/models/room_state.dart';
import 'package:desktop/desktop.dart';

class RoomManager extends ChangeNotifier {
  RoomManager(this._roomManagerClient, this.player, this.room) {
    _connection = _roomManagerClient.connect(room.id, player.id);
    _listenToUpdates();
  }

  final RoomManagerClient _roomManagerClient;
  final Player player;
  final Room room;

  late final RoomConnection _connection;
  final ValueNotifier<ErrorMessage> errorMessageNotifier = ValueNotifier(
    const ErrorMessage(''),
  );
  StreamSubscription<RoomState>? _roomStateSubscription;

  RoomState _roomState = const RoomState.loading();

  RoomState get state => _roomState;

  void _listenToUpdates() {
    _roomStateSubscription = _connection.roomStateStream.listen(
      (roomState) {
        if (roomState is AwaitingFulfillment ||
            roomState is AwaitingPlayersReady ||
            roomState is GameStarted) {
          _roomState = roomState;
          notifyListeners();
        }

        if (roomState is ErrorOccurred) {
          errorMessageNotifier.value = ErrorMessage(roomState.message);
        }
      },
    );
  }

  void sendEvent(GameEvent event) {
    _connection.sendEvent(event);
  }

  @override
  void dispose() {
    _roomStateSubscription?.cancel();
    super.dispose();
  }
}
