import 'package:auto_route/auto_route.dart';
import 'package:client/common/navigation/chess_router.gr.dart';
import 'package:client/common/widgets/time_widget.dart';
import 'package:client/players/models/player.dart';
import 'package:client/players/state/player_notifier.dart';
import 'package:client/rooms/client/room_manager_client.dart';
import 'package:client/rooms/models/room.dart';
import 'package:client/rooms/state/room_manager.dart';
import 'package:client/rooms/state/rooms_notifier.dart';
import 'package:client/rooms/widgets/add_room_card.dart';
import 'package:client/rooms/widgets/available_room_card.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class RoomsPage extends StatelessWidget {
  const RoomsPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final player = context.select((PlayerNotifier notifier) => notifier.player);
    final rooms = context.select((RoomsNotifier notifier) => notifier.rooms);

    if (player == null) {
      return const Center(
        child: Text("Something may went wrong. Please, reload the app."),
      );
    }

    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Hi, ${player.name}',
                style: const TextStyle(fontSize: 24),
              ),
              const TimeWidget(),
            ],
          ),
          const Divider(color: Colors.white),
          const Text('Available rooms:', style: TextStyle(fontSize: 24)),
          const SizedBox(height: 16),
          SingleChildScrollView(
            child: Wrap(
              runSpacing: 12.0,
              children: [
                const AddRoomCard(),
                ...rooms.map(
                  (room) => AvailableRoomCard(
                    room: room,
                    onPressed: () => _onRoomPressed(context, player, room),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _onRoomPressed(BuildContext context, Player player, Room room) {
    AutoRouter.of(context).push(
      Game(
        roomManager: RoomManager(
          const RoomManagerClient(),
          player,
          room,
        ),
      ),
    );
  }
}
