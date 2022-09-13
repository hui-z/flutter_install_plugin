import 'dart:async';

import 'package:flutter/services.dart';

typedef ValueCallback = void Function(String);

class InstallPlugin {
  static const STATUS_REQUESTING_PERMISSION = "1";
  static const STATUS_INSTALLING = "2";
  static const STATUS_INSTALL_SUCCESS = "3";
  static const STATUS_INSTALL_FAIL = "4";

  static InstallPlugin? _instance;

  static InstallPlugin get instance {
    if (_instance == null) {
      _instance = InstallPlugin._internal();
    }
    return _instance!;
  }

  late MethodChannel _channel;

  ValueCallback? _listener;

  InstallPlugin._internal() {
    _channel = const MethodChannel('install_plugin');
    _channel.setMethodCallHandler((call) async {
      String method = call.method;
      if (method == 'onInstallStatusChange') {
        String code = call.arguments;
        _listener?.call(code);
      }
    });
  }

  void setListener(ValueCallback? callback) {
    _listener = callback;
  }

  void removeListener() {
    _listener = null;
  }

  /// for Android : install apk by its file absolute path;
  /// if the target platform is higher than android 24:
  /// a [appId] is required
  /// (the caller's applicationId which is defined in build.gradle)
  static Future<String> installApk(String filePath, String appId) async {
    Map<String, String> params = {'filePath': filePath, 'appId': appId};
    return await instance._channel.invokeMethod('installApk', params);
  }

  /// for iOS: go to app store by the url
  static Future<String> gotoAppStore(String urlString) async {
    Map<String, String> params = {'urlString': urlString};
    return await instance._channel.invokeMethod('gotoAppStore', params);
  }
}
