import 'package:client/players/client/player_client.dart';
import 'package:client/players/models/player.dart';
import 'package:flutter/cupertino.dart';

class PlayerNotifier extends ChangeNotifier {
  PlayerNotifier(this._playerClient);

  final PlayerClient _playerClient;

  Player? _player;

  Player? get player => _player;

  Future<void> addPlayer(String name) async {
    final playerId = await _playerClient.addPlayer(name);

    _player = await _playerClient.fetchPlayer(playerId);

    notifyListeners();
  }
}
