import 'dart:math';

import 'package:auto_route/auto_route.dart';
import 'package:client/common/navigation/chess_router.gr.dart';
import 'package:client/rooms/models/game_result.dart';
import 'package:desktop/desktop.dart';
import 'package:flutter/material.dart';
import 'package:rive/rive.dart';

class ResultPage extends StatelessWidget {
  const ResultPage({Key? key, required this.result}) : super(key: key);

  final GameResult result;

  String get _title {
    switch (result) {
      case GameResult.win:
        return 'You won!';
      case GameResult.lose:
        return 'You lost :(';
      case GameResult.draw:
        return 'Draw!';
    }
  }

  String get _animationArtboard {
    switch (result) {
      case GameResult.win:
        return 'Tada';
      case GameResult.lose:
        return 'Mindblown';
      case GameResult.draw:
        return 'Onfire';
    }
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final animationSize = min(size.width * 0.5, size.height * 0.5);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.center,
      mainAxisAlignment: MainAxisAlignment.center,
      mainAxisSize: MainAxisSize.min,
      children: [
        SizedBox(
          height: animationSize,
          width: animationSize,
          child: RiveAnimation.asset(
            'assets/animations/1714-3385-rives-animated-emojis.riv',
            artboard: _animationArtboard,
            fit: BoxFit.scaleDown,
            controllers: [SimpleAnimation('idle')],
          ),
        ),
        Button.text(
          _title,
          fontSize: 26,
          tooltip: 'Press to proceed',
          color: Colors.white,
          hoverColor: Colors.grey,
          highlightColor: Colors.blue,
          onPressed: () => _navigateToRooms(context),
        ),
      ],
    );
  }

  void _navigateToRooms(BuildContext context) {
    AutoRouter.of(context).replace(const Rooms());
  }
}
