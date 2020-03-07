import 'dart:async';

import 'package:flutter/services.dart';

class Cmd {
  static const MethodChannel _channel = const MethodChannel('cmd');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> cmdInit(String packageCodePath) async {
    final bool cmd = await _channel
        .invokeMethod('init', {"packageCodePath": packageCodePath});
    return cmd;
  }

  static Future<bool> runRootCmd(String command) async {
    final bool cmd =
        await _channel.invokeMethod('runRootCmd', {"command": command});
    return cmd;
  }
}
