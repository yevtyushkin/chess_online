import 'package:client/rooms/models/room.dart';
import 'package:client/rooms/widgets/room_card.dart';
import 'package:desktop/desktop.dart';

class AvailableRoomCard extends StatelessWidget {
  const AvailableRoomCard({
    Key? key,
    required this.room,
    required this.onPressed,
  }) : super(key: key);

  final Room room;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    final numberOfPlayers = room.players.length;

    return RoomCard(
      onPressed: onPressed,
      title: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(room.name),
          Text('$numberOfPlayers/2 player${numberOfPlayers != 1 ? 's' : ''}')
        ],
      ),
    );
  }
}
