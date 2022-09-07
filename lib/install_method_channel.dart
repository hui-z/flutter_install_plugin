import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'install_platform_interface.dart';

class MethodChannelInstall extends InstallPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('install_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String> installApk(String filePath, String appId) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// for iOS: go to app store by the url
  @override
  Future<String> gotoAppStore(String urlString) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }


}
