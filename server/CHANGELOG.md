# Change Log

All notable changes to this project will be documented in this file.

### 10.10.2021

#### Added

- Base models: `Chessboard`, `Color`, `Coordinate`, `CoordinateFile`, `CoordinateRank`, `Piece`, `PieceType`, `Square`;
- Tests for `Chessboard.initial`.

### 28.10.2021

### Changed

- ChessBoard.initial with more DRY tests.

### 30.10.2021

### Added

- Documentation for existing classes and objects: `Chessboard`, `Side`, `Coordinate`, `CoordinateFile`
  , `CoordinateRank`, `Piece`, `PieceType`, `Square`;
- New classes `CastlingAvailability`, `CastlingType`, `GameState` with documentation and test.

### Changed

- `Color` -> `Side` naming;
- `Square` structure to reuse `Option`.

### 31.10.2021

### Added

- `Move`, `MoveValidator`, `MoveValidationError`;
- Started working on tests for `MoveValidator`.

### Changed

- (`CastlingType` + `CastlingAvailability`) => `Castling` enum.

### 01.11.2021

### Added

- `Chessboard.apply` that makes the API a bit prettier and convenient-to-use;

### 02.11.2021

### Added

- Pawn move pattern validation with tests;
- A bunch of test data for testing.

### 07.11.2021

### Added

- `MovePattern`.

### Changed

- `MovePatternValidator` API.

### 09.11.2021

### Added

- King's `MovePattern` validation with tests;
- Queen's `MovePattern` validation with tests;
- Rook's `MovePattern` validation with tests;
- Bishop's `MovePattern` validation with tests;
- Knight's `MovePattern` validation with tests;
- Some useful methods for the domain API.
