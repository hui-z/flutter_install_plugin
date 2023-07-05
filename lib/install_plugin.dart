import 'dart:async';

import 'package:flutter/services.dart';

class InstallPlugin {
  static const MethodChannel _channel = const MethodChannel('install_plugin');

  // static const STATUS_REQUESTING_PERMISSION = "1";
  // static const STATUS_INSTALLING = "2";
  // static const STATUS_INSTALL_SUCCESS = "3";
  // static const STATUS_INSTALL_FAIL = "4";

  /// for Android : install apk by its file absolute path;
  /// if the target platform is higher than android 24:
  /// a [appId] is required
  /// (the caller's applicationId which is defined in build.gradle)
  static Future<dynamic> installApk(String filePath, String appId) async {
    Map<String, String> params = {'filePath': filePath, 'appId': appId};
    final result = await _channel.invokeMethod('installApk', params);
    return result;
  }

  /// for iOS: go to app store by the url
  static Future<dynamic> gotoAppStore(String urlString) async {
    Map<String, String> params = {'urlString': urlString};
    dynamic result = await _channel.invokeMethod('gotoAppStore', params);
    return result;
  }
}
