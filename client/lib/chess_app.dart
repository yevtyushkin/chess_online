import 'package:client/common/navigation/chess_router.gr.dart';
import 'package:client/players/client/player_client.dart';
import 'package:client/players/state/player_notifier.dart';
import 'package:client/rooms/client/rooms_client.dart';
import 'package:client/rooms/state/rooms_notifier.dart';
import 'package:desktop/desktop.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class ChessApp extends StatefulWidget {
  const ChessApp({Key? key}) : super(key: key);

  @override
  _ChessAppState createState() => _ChessAppState();
}

class _ChessAppState extends State<ChessApp> {
  final Dio _dio = Dio();
  late final PlayerClient _playerClient;
  late final RoomsClient _roomsClient;

  final ChessRouter _router = ChessRouter();

  @override
  void initState() {
    super.initState();

    _playerClient = PlayerClient(_dio);
    _roomsClient = RoomsClient(_dio);
  }

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider<PlayerNotifier>(
          create: (_) => PlayerNotifier(_playerClient),
          lazy: false,
        ),
        ChangeNotifierProxyProvider<PlayerNotifier, RoomsNotifier>(
          create: (_) => RoomsNotifier(_roomsClient),
          lazy: false,
          update: (_, playerNotifier, oldRoomsNotifier) {
            final roomsNotifier =
                oldRoomsNotifier ?? RoomsNotifier(_roomsClient);

            final player = playerNotifier.player;
            if (player != null) {
              roomsNotifier.onPlayerLoggedIn(player);
            }

            return roomsNotifier;
          },
        ),
      ],
      child: DesktopApp.router(
        routeInformationParser: _router.defaultRouteParser(),
        routerDelegate: _router.delegate(),
        debugShowCheckedModeBanner: false,
      ),
    );
  }
}
