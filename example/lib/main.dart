import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:cmd/cmd.dart';
import 'package:path_provider/path_provider.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool _cmdInit = false;
  bool _wifi = false;

  @override
  void initState() {
    super.initState();
    // initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await Cmd.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  void initCmd() async {
    Directory appDocDir = await getApplicationDocumentsDirectory();
    String appDocPath = appDocDir.path;
    bool cmdInit = await Cmd.cmdInit(appDocPath);
    if (!mounted) return;

    setState(() {
      _cmdInit = cmdInit;
    });
  }

  void runRootCmd(String cmd) async {
    bool wifi = await Cmd.runRootCmd(cmd);
    if (!mounted) return;

    setState(() {
      _wifi = wifi;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            GestureDetector(
              onTap: () {
                initCmd();
              },
              child: Text('Running on: $_cmdInit\n'),
            ),
            GestureDetector(
              onTap: () {
                runRootCmd("svc wifi enable");
              },
              child: Text('开启Wi-Fi: $_wifi\n'),
            ),
            GestureDetector(
              onTap: () {
                runRootCmd("svc wifi disable");
              },
              child: Text('关闭Wi-Fi: $_wifi\n'),
            ),
          ],
        ),
      ),
    );
  }
}
