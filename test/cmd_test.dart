import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:cmd/cmd.dart';

void main() {
  const MethodChannel channel = MethodChannel('cmd');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await Cmd.platformVersion, '42');
  });
}
