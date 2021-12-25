import 'package:auto_route/auto_route.dart';
import 'package:client/players/page/login_page.dart';
import 'package:client/rooms/page/game_page.dart';
import 'package:client/rooms/page/result_page.dart';
import 'package:client/rooms/page/rooms_page.dart';

@CustomAutoRouter(
  transitionsBuilder: TransitionsBuilders.noTransition,
  replaceInRouteName: 'Page,Route',
  routes: <AutoRoute>[
    AutoRoute(
      page: LoginPage,
      name: 'login',
      path: '/login',
      initial: true,
    ),
    AutoRoute(
      page: RoomsPage,
      name: 'rooms',
      path: '/rooms',
    ),
    AutoRoute(
      page: GamePage,
      name: 'game',
      path: '/game',
    ),
    AutoRoute(
      page: ResultPage,
      name: 'result',
      path: '/result',
    ),
  ],
)
class $ChessRouter {}
