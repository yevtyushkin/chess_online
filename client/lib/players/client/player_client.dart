import 'dart:convert';

import 'package:client/players/models/player.dart';
import 'package:client/utils/server_uri.dart';
import 'package:dio/dio.dart';

class PlayerClient {
  const PlayerClient(this._client);

  final Dio _client;

  static final Uri _playersUri =
      ServerUri.baseHttp.replace(pathSegments: ['players']);

  Future<String> addPlayer(String name) async {
    final response = await _client.postUri(
      _playersUri,
      data: jsonEncode({'name': name}),
    );

    return '${response.data}';
  }

  Future<Player> fetchPlayer(String id) async {
    final response = await _client.getUri(
      _playersUri,
      options: Options(headers: {'id': id}),
    );

    final playerJson = response.data as Map<String, dynamic>;

    return Player.fromJson(playerJson);
  }
}
