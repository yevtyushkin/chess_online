# Change Log

All notable changes to this project will be documented in this file.

### 10.10.2021

#### Added

- Base models: `Chessboard`, `Color`, `Coordinate`, `CoordinateFile`, `CoordinateRank`, `Piece`, `PieceType`, `Square`.
- Tests for `Chessboard.initial`

### 28.10.2021

### Changed

- ChessBoard.initial with more DRY tests

### 30.10.2021

### Added

- Documentation for existing classes and objects: `Chessboard`, `Side`, `Coordinate`, `CoordinateFile`
  , `CoordinateRank`, `Piece`, `PieceType`, `Square`;
- New classes `CastlingAvailability`, `CastlingType`, `GameState` with documentation and test.

### Changed

- `Color` -> `Side` naming;
- `Square` structure to reuse `Option`.
