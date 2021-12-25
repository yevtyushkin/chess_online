import 'package:freezed_annotation/freezed_annotation.dart';

enum Side {
  @JsonValue('b')
  black,

  @JsonValue('w')
  white,
}
