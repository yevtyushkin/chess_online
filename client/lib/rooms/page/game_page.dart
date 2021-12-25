import 'package:client/rooms/state/room_manager.dart';
import 'package:client/rooms/widgets/game_overview.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class GamePage extends StatefulWidget {
  const GamePage({
    Key? key,
    required this.roomManager,
  }) : super(key: key);

  final RoomManager roomManager;

  @override
  State<GamePage> createState() => _GamePageState();
}

class _GamePageState extends State<GamePage> {
  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider.value(
      value: widget.roomManager,
      child: const Center(child: GameOverview()),
    );
  }

  @override
  void dispose() {
    widget.roomManager.dispose();
    super.dispose();
  }
}
