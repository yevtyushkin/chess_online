import 'dart:convert';

import 'package:client/rooms/models/game_event.dart';
import 'package:client/rooms/models/room_state.dart';
import 'package:client/utils/server_uri.dart';
import 'package:stream_transform/stream_transform.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class RoomManagerClient {
  const RoomManagerClient();

  RoomConnection connect(String roomId, String playerId) {
    final uri = ServerUri.baseWs.replace(
      path: '/rooms/connect/$roomId/$playerId',
    );

    return RoomConnection(WebSocketChannel.connect(uri));
  }
}

class RoomConnection {
  static const RoomStateConverter _converter = RoomStateConverter();

  const RoomConnection(this._channel);

  final WebSocketChannel _channel;

  Stream<RoomState> get roomStateStream => _channel.stream.map(
        (data) {
          print(data);
          try {
            return _converter.fromJson(
              jsonDecode(data) as Map<String, dynamic>,
            );
          } catch (e) {
            print(e);
          }
        },
      ).whereType<RoomState>();

  void sendEvent(GameEvent event) {
    print(event.toJson());
    _channel.sink.add(jsonEncode(event.toJson()));
  }
}
