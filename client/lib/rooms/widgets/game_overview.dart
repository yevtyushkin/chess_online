import 'package:client/players/models/player.dart';
import 'package:client/rooms/models/game_event.dart';
import 'package:client/rooms/models/room_state.dart';
import 'package:client/rooms/state/room_manager.dart';
import 'package:client/rooms/widgets/chessboard_overview.dart';
import 'package:collection/collection.dart';
import 'package:desktop/desktop.dart';
import 'package:provider/provider.dart';

class GameOverview extends StatelessWidget {
  const GameOverview({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = context.select((RoomManager manager) => manager.state);
    final currentPlayer = context.read<RoomManager>().player;
    final enemyPlayer = _getEnemyPlayer(currentPlayer, state);
    final needsSendReadyButton = state is AwaitingPlayersReady &&
        !state.playersReady.contains(currentPlayer);

    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          enemyPlayer?.name ?? '???',
          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),
        const ChessboardOverview(),
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              currentPlayer.name,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            if (needsSendReadyButton)
              Padding(
                padding: const EdgeInsets.only(left: 4.0),
                child: Button.text(
                  'ready?',
                  onPressed: () => _onPlayerReady(context),
                ),
              )
          ],
        ),
      ],
    );
  }

  void _onPlayerReady(BuildContext context) {
    context.read<RoomManager>().sendEvent(const GameEvent.playerReady());
  }

  Player? _getEnemyPlayer(Player currentPlayer, RoomState state) {
    if (state is AwaitingPlayersReady) {
      return state.connectedPlayers.firstWhereOrNull(
        (player) => player != currentPlayer,
      );
    }

    if (state is GameStarted) {
      return state.whiteSidePlayer != currentPlayer
          ? state.whiteSidePlayer
          : state.blackSidePlayer;
    }
  }
}
