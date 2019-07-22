#import "InstallPlugin.h"
#import "SwiftInstallPlugin.swift"
//#import <install_plugin/install_plugin-Swift.h>

@implementation InstallPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftInstallPlugin registerWithRegistrar:registrar];
}
@end
