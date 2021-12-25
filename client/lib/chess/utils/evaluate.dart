import 'package:chess/chess.dart';
import 'package:flutter_stateless_chessboard/flutter_stateless_chessboard.dart';

class Evaluate {
  static String move({
    required String fen,
    required ShortMove move,
  }) {
    final chess = Chess.fromFEN(fen);

    final moveData = {
      'from': move.from,
      'to': move.to,
      'promotion':
          move.promotion.map((pieceType) => pieceType.name).toNullable(),
    };

    if (chess.move(moveData)) {
      return chess.fen;
    }

    return fen;
  }
}
