import 'install_platform_interface.dart';

class Untitled {
  Future<String?> getPlatformVersion() {
    return InstallPlatform.instance.getPlatformVersion();
  }
}
