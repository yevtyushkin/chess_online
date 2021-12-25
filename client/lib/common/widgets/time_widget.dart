import 'dart:async';

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class TimeWidget extends StatefulWidget {
  const TimeWidget({Key? key}) : super(key: key);

  @override
  _TimeWidgetState createState() => _TimeWidgetState();
}

class _TimeWidgetState extends State<TimeWidget> {
  static final DateFormat _format = DateFormat.Hms();

  late Timer _timer;
  late DateTime _now;

  @override
  void initState() {
    super.initState();

    _now = DateTime.now();
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      _updateTime();
    });
  }

  void _updateTime() {
    setState(() {
      _now = DateTime.now();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Text(_format.format(_now), style: const TextStyle(fontSize: 20));
  }

  @override
  void dispose() {
    _timer.cancel();
    super.dispose();
  }
}
