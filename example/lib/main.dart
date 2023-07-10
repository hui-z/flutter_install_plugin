import 'dart:io';

import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:install_plugin/install_plugin.dart';
import 'package:path_provider/path_provider.dart';

import 'utils.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  /// 默认使用了再惠合伙人的下载地址
  static const _defaultUrl =
      'https://itunes.apple.com/cn/app/%E5%86%8D%E6%83%A0%E5%90%88%E4%BC%99%E4%BA%BA/id1375433239?l=zh&ls=1&mt=8';
  TextEditingController _textEditingController = TextEditingController();
  String _appUrl = '';
  double _progressValue = 0.0;

  @override
  void initState() {
    super.initState();
    _textEditingController.text = _defaultUrl;
    PermissionUtil.requestAll();
  }

  @override
  void dispose() {
    _textEditingController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          alignment: Alignment.center,
          child: Platform.isAndroid
              ? Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Padding(
                      padding:
                          const EdgeInsets.only(top: 30, left: 16.0, right: 16),
                      child: LinearProgressIndicator(
                        value: _progressValue,
                        backgroundColor: Colors.grey,
                        valueColor: AlwaysStoppedAnimation<Color>(Colors.blue),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.only(
                          top: 16, left: 16.0, right: 16, bottom: 16),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          Text(
                              'downloading ${(_progressValue * 100).toStringAsFixed(0)} %')
                        ],
                      ),
                    ),
                    ElevatedButton(
                        onPressed: () => _networkInstallApk(),
                        child: Text('network install apk')),
                    SizedBox(height: 10),
                    ElevatedButton(
                        onPressed: () => _localInstallApk(),
                        child: Text('local install apk')),
                  ],
                )
              : Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: <Widget>[
                    Platform.isIOS
                        ? TextField(
                            controller: _textEditingController,
                            decoration: InputDecoration(
                                hintText: 'URL for app store to launch'),
                            onChanged: (url) => _appUrl = url,
                          )
                        : SizedBox(),
                    Platform.isIOS
                        ? ElevatedButton(
                            onPressed: () => _gotoAppStore(_appUrl),
                            child: Text('gotoAppStore'))
                        : SizedBox()
                  ],
                ),
        ),
      ),
    );
  }

  _networkInstallApk() async {
    if (_progressValue != 0 && _progressValue < 1) {
      _showResMsg("Wait a moment, downloading");
      return;
    }

    _progressValue = 0.0;
    var appDocDir = await getTemporaryDirectory();
    String savePath = appDocDir.path + "/zaihui_kylinim_42.apk";
    String fileUrl =
        "https://s3.cn-north-1.amazonaws.com.cn/mtab.kezaihui.com/apk/kylinim/zaihui_kylinim_42.apk";
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

  _showResMsg(String msg) {
    print(msg);
    Utils.toast(msg);
  }
}
