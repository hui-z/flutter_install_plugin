
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'install_method_channel.dart';

abstract class InstallPlatform extends PlatformInterface {
  InstallPlatform() : super(token: _token);

  static final Object _token = Object();

  static InstallPlatform _instance = MethodChannelInstall();

  static InstallPlatform get instance => _instance;

  static set instance(InstallPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

   Future<String> installApk(String filePath, String appId) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// for iOS: go to app store by the url
   Future<String> gotoAppStore(String urlString) async {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

}
