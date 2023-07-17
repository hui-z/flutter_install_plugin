# install_plugin
-->Amazing Project
[![Build Status](https://travis-ci.org/hui-z/flutter_install_plugin.svg?branch=master)](https://travis-ci.org/hui-z/flutter_install_plugin#)
[![pub package](https://img.shields.io/pub/v/install_plugin.svg)](https://pub.dartlang.org/packages/install_plugin)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/hui-z/flutter_install_plugin/blob/master/LICENSE)

We use the `install_plugin` plugin to install apk for android; and using url to go to app store for iOS.

## Usage

To use this plugin, add `install_plugin` as a dependency in your pubspec.yaml file. For example:
```yaml
dependencies:
  install_plugin: '^2.1.0'
```

## iOS
Your project need create with swift.

##  Android
You need to request permission for READ_EXTERNAL_STORAGE to read the apk file. You can handle the storage permission using [flutter_permission_handler](https://github.com/BaseflowIT/flutter-permission-handler).
```
 <!-- read permissions for external storage -->
 <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
In Android version >= 8.0 , You need to request permission for REQUEST_INSTALL_PACKAGES to install the apk file
 ```
 <!-- installation package permissions -->
 <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
 ```
In Android version <= 6.0 , You need to request permission for WRITE_EXTERNAL_STORAGE to copy the apk from the app private location to the download directory
 ```
 <!-- write permissions for external storage -->
 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 ```

## Example
install apk from the internet 
``` dart
  _networkInstallApk() async {
    if (_progressValue != 0 && _progressValue < 1) {
      _showResMsg("Wait a moment, downloading");
      return;
    }

    _progressValue = 0.0;
    var appDocDir = await getTemporaryDirectory();
    String savePath = appDocDir.path + "/takeaway_phone_release_1.apk";
    String fileUrl =
        "https://s3.cn-north-1.amazonaws.com.cn/mtab.kezaihui.com/apk/takeaway_phone_release_1.apk";
    await Dio().download(fileUrl, savePath, onReceiveProgress: (count, total) {
      final value = count / total;
      if (_progressValue != value) {
        setState(() {
          if (_progressValue < 1.0) {
            _progressValue = count / total;
          } else {
            _progressValue = 0.0;
          }
        });
        print((_progressValue * 100).toStringAsFixed(0) + "%");
      }
    });

    final res = await InstallPlugin.install(savePath);
    _showResMsg(
        "install apk ${res['isSuccess'] == true ? 'success' : 'fail:${res['errorMessage'] ?? ''}'}");
  }
```

install apk from the local storage
``` dart
  _localInstallApk() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      final res = await InstallPlugin.install(result.files.single.path ?? '');
      _showResMsg(
          "install apk ${res['isSuccess'] == true ? 'success' : 'fail:${res['errorMessage'] ?? ''}'}");
    } else {
      // User canceled the picker
      _showResMsg("User canceled the picker apk");
    }
  }
```

Go to AppStore , example appStore url : https://itunes.apple.com/cn/app/%E5%86%8D%E6%83%A0%E5%90%88%E4%BC%99%E4%BA%BA/id1375433239?l=zh&ls=1&mt=8
``` dart
  _gotoAppStore(String url) async {
    url = url.isEmpty ? _defaultUrl : url;
    final res = await InstallPlugin.install(url);
    _showResMsg(
        "go to appstroe ${res['isSuccess'] == true ? 'success' : 'fail:${res['errorMessage'] ?? ''}'}");
  }
```
