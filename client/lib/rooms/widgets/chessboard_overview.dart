import 'dart:async';
import 'dart:math';

import 'package:auto_route/auto_route.dart';
import 'package:chess_vectors_flutter/chess_vectors_flutter.dart';
import 'package:client/chess/models/game_state.dart';
import 'package:client/chess/utils/evaluate.dart';
import 'package:client/common/navigation/chess_router.gr.dart';
import 'package:client/players/models/player.dart';
import 'package:client/rooms/models/game_event.dart';
import 'package:client/rooms/models/game_result.dart';
import 'package:client/rooms/models/game_status.dart';
import 'package:client/rooms/models/room_state.dart';
import 'package:client/rooms/state/room_manager.dart';
import 'package:collection/collection.dart';
import 'package:desktop/desktop.dart';
import 'package:flutter/material.dart' show Material, InkWell, Colors;
import 'package:flutter_stateless_chessboard/flutter_stateless_chessboard.dart';
import 'package:provider/provider.dart';

class ChessboardOverview extends StatefulWidget {
  const ChessboardOverview({Key? key}) : super(key: key);

  @override
  State<ChessboardOverview> createState() => _ChessboardOverviewState();
}

class _ChessboardOverviewState extends State<ChessboardOverview> {
  late String _fen = GameState.initialFEN;
  late RoomState _roomState;
  late final Player _currentPlayer;
  late final RoomManager _roomManager;

  @override
  void initState() {
    super.initState();

    _roomManager = context.read<RoomManager>();
    _roomManager.addListener(_roomManagerListener);
    _roomManager.errorMessageNotifier.addListener(_errorListener);

    _currentPlayer = _roomManager.player;
    _roomState = _roomManager.state;
  }

  BoardColor get _boardOrientation {
    final state = _roomState;

    if (state is GameStarted) {
      return _currentPlayer == state.whiteSidePlayer
          ? BoardColor.WHITE
          : BoardColor.BLACK;
    }

    return BoardColor.WHITE;
  }

  bool get _canMakeMove {
    final state = _roomState;

    return state is GameStarted && state.movesNow(_currentPlayer);
  }

  @override
  Widget build(BuildContext context) {
    final screenSize = MediaQuery.of(context).size;
    final width = screenSize.width;
    final height = screenSize.height;
    final size = min(width * 0.8, height * 0.8);

    return Material(
      child: Chessboard(
        fen: _fen,
        size: size,
        orientation: _boardOrientation,
        onMove: (move) => _onMoveMade(context, move),
        onPromote: () => _onPromotion(size / 8),
      ),
    );
  }

  void _roomManagerListener() {
    final state = _roomManager.state;

    setState(() {
      _roomState = state;

      if (state is GameStarted) {
        _fen = state.gameState.fen;
      }
    });

    if (state is GameStarted) {
      final gameStatus = state.gameState.status;

      if (gameStatus is GameContinues) return;

      if (gameStatus is Draw) {
        AutoRouter.of(context).replace(Result(result: GameResult.draw));
      }

      if (gameStatus is Win) {
        final result = state.playerSide(_currentPlayer) == gameStatus.by
            ? GameResult.win
            : GameResult.lose;

        AutoRouter.of(context).replace(Result(result: result));
      }
    }
  }

  void _errorListener() {
    final error = _roomManager.errorMessageNotifier.value;

    Messenger.showMessage(
      context,
      message: error.text,
      kind: MessageKind.error,
    );
  }

  void _onMoveMade(BuildContext context, ShortMove move) {
    if (!_canMakeMove) return;

    setState(() {
      _fen = Evaluate.move(fen: _fen, move: move);
    });

    context.read<RoomManager>().sendEvent(GameEvent.moveMade(move));
  }

  Future<PieceType> _onPromotion(double pieceOptionSize) {
    final pieceTypeCompleter = Completer<PieceType>();

    final List<Widget> pieceTypeWidgets = _boardOrientation == BoardColor.WHITE
        ? [WhiteQueen(), WhiteRook(), WhiteKnight(), WhiteBishop()]
        : [BlackQueen(), BlackRook(), BlackKnight(), BlackBishop()];

    final pieceTypeChoices = [
      PieceType.QUEEN,
      PieceType.ROOK,
      PieceType.KNIGHT,
      PieceType.BISHOP,
    ];

    late DialogController dialogController;
    dialogController = showDialog(
      context,
      builder: (_) {
        return Dialog(
          body: Material(
            color: Colors.black,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: pieceTypeChoices.mapIndexed(
                (i, pieceType) {
                  return InkWell(
                    child: SizedBox(
                      height: pieceOptionSize,
                      width: pieceOptionSize,
                      child: DecoratedBox(
                        decoration: BoxDecoration(
                          color: _boardOrientation == BoardColor.WHITE
                              ? null
                              : Colors.white,
                        ),
                        child: pieceTypeWidgets[i],
                      ),
                    ),
                    onTap: () {
                      dialogController.close();
                      pieceTypeCompleter.complete(pieceType);
                    },
                  );
                },
              ).toList(),
            ),
          ),
          title: const Text('Please, select the piece to promote'),
        );
      },
      dismissible: false,
    );

    return pieceTypeCompleter.future;
  }

  @override
  void dispose() {
    _roomManager.removeListener(_roomManagerListener);
    _roomManager.errorMessageNotifier.removeListener(_errorListener);

    super.dispose();
  }
}
