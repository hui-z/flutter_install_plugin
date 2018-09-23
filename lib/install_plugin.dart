import 'dart:async';

import 'package:flutter/services.dart';

class InstallPlugin {
  static const MethodChannel _channel = const MethodChannel('install_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// for Android : install apk by its file absolute path;
  /// if the target platform is higher than android 24:
  /// 1. a [appId] is required
  /// (the caller's applicationId which is defined in build.gradle)
  /// 2. add provider tag to caller's AndroidManifest.xml
  ///   <application ...>
  ///     <provider
  ///     android:name="android.support.v4.content.FileProvider"
  ///     android:authorities="${applicationId}.fileProvider"
  ///     android:exported="false"
  ///     android:grantUriPermissions="true">
  ///     <meta-data
  ///     android:name="android.support.FILE_PROVIDER_PATHS"
  ///     android:resource="@xml/provider_paths"/>
  ///     </provider>
  ///   </application>
  /// 3. add provider_paths.xml to caller's res/xml
  ///   <?xml version="1.0" encoding="utf-8"?>
  ///   <paths>
  ///     <external-path
  ///     name="external_storage_root"
  ///     path="."/>
  ///   </paths>
  static Future<String> installApk(String filePath, String appId) async {
    Map<String, String> params = {'filePath': filePath, 'appId': appId};
    return await _channel.invokeMethod('installApk', params);
  }

  /// for iOS: go to app store by the url
  static Future<String> gotoAppStore(String urlString) async {
    Map<String, String> params = {'urlString': urlString};
    return await _channel.invokeMethod('gotoAppStore', params);
  }
}
