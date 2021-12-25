import 'dart:convert';

import 'package:client/rooms/models/room.dart';
import 'package:client/utils/server_uri.dart';
import 'package:dio/dio.dart';
import 'package:stream_transform/stream_transform.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class RoomsClient {
  const RoomsClient(this._client);

  final Dio _client;

  Future<void> addRoom(String roomName, String playerId) {
    final uri = ServerUri.baseHttp.replace(path: '/rooms');

    return _client.postUri(
      uri,
      data: jsonEncode({'name': roomName}),
      options: Options(headers: {'id': playerId}),
    );
  }

  Stream<List<Room>> roomsStream() {
    final uri = ServerUri.baseWs.replace(path: '/rooms');

    final channel = WebSocketChannel.connect(uri);

    return channel.stream.map(
      (message) {
        try {
          final roomsJson = jsonDecode(message);

          return Room.listFromJson(roomsJson as List<dynamic>);
        } catch (_) {}
      },
    ).whereType<List<Room>>();
  }
}
