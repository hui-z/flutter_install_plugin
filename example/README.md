example/lib/main.dart

```dart
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

_gotoAppStore(String url) async {
  url = url.isEmpty ? _defaultUrl : url;
  final res = await InstallPlugin.install(url);
  _showResMsg(
      "go to appstroe ${res['isSuccess'] == true ? 'success' : 'fail:${res['errorMessage'] ?? ''}'}");
}
```
