import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:install_plugin/install_plugin.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  const MethodChannel channel = MethodChannel('install_plugin');
  final List<MethodCall> log = <MethodCall>[];
  String? response;

  channel.setMockMethodCallHandler((MethodCall methodCall) async {
    log.add(methodCall);
    return response;
  });

  tearDown(() {
    log.clear();
  });

  test('install test', () async {
    response = 'Success';
    final fakePath = 'fake.apk';
    final fakeAppId = 'com.example.install';
    final dynamic result =
        await InstallPlugin.install(fakePath, appId: fakeAppId);
    expect(
      log,
      <Matcher>[
        isMethodCall('install',
            arguments: {'filePathOrUrlString': fakePath, 'appId': fakeAppId})
      ],
    );
    expect(result, response);
  });

  test('install test', () async {
    response = 'Success';
    final fakeUrl = 'fake_url';
    final dynamic result = await InstallPlugin.install(fakeUrl);
    expect(
      log,
      <Matcher>[
        isMethodCall('install', arguments: {'filePathOrUrlString': fakeUrl})
      ],
    );
    expect(result, response);
  });

  test('installApk test', () async {
    response = 'Success';
    final fakePath = 'fake.apk';
    final fakeAppId = 'com.example.install';
    final dynamic result =
        await InstallPlugin.installApk(fakePath, appId: fakeAppId);
    expect(
      log,
      <Matcher>[
        isMethodCall('installApk',
            arguments: {'filePath': fakePath, 'appId': fakeAppId})
      ],
    );
    expect(result, response);
  });

  test('gotoAppStore test', () async {
    response = null;
    final fakeUrl = 'fake_url';
    final dynamic result = await InstallPlugin.gotoAppStore(fakeUrl);
    expect(
      log,
      <Matcher>[
        isMethodCall('gotoAppStore', arguments: {'urlString': fakeUrl})
      ],
    );
    expect(result, isNull);
  });
}
