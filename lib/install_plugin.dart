import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class InstallPlugin {
  static const MethodChannel _channel = const MethodChannel('install_plugin');

  /// for Android : install apk by its file absolute path | for iOS: go to app store by the url;
  /// if the target platform is higher than android 24:
  /// a [appId] is not required
  /// (the caller's applicationId which is defined in build.gradle)
  static Future<dynamic> install(String filePathOrUrlString,
      {String appId = ''}) async {
    if (Platform.isAndroid) {
      return installApk(filePathOrUrlString, appId: appId);
    } else if (Platform.isIOS) {
      return gotoAppStore(filePathOrUrlString);
    }
    return {'isSuccess': false, 'errorMessage': 'unsupported device'};
  }

  /// for Android : install apk by its file absolute path;
  /// if the target platform is higher than android 24:
  /// a [appId] is not required
  /// (the caller's applicationId which is defined in build.gradle)
  static Future<dynamic> installApk(String filePath,
      {String appId = ''}) async {
    Map<String, String> params = {'filePath': filePath, 'appId': appId};
    final dynamic result = await _channel.invokeMethod('installApk', params);
    return result;
  }

  /// for iOS: go to app store by the url
  static Future<dynamic> gotoAppStore(String urlString) async {
    Map<String, String> params = {'urlString': urlString};
    final dynamic result = await _channel.invokeMethod('gotoAppStore', params);
    return result;
  }
}
