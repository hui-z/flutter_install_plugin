import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:install_plugin/install_plugin.dart';

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
              ? ElevatedButton(
                  onPressed: () {
                    onClickInstallApk();
                  },
                  child: Text('install'))
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
                            onPressed: () => onClickGotoAppStore(_appUrl),
                            child: Text('gotoAppStore'))
                        : SizedBox()
                  ],
                ),
        ),
      ),
    );
  }

  void onClickInstallApk() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    if (result != null) {
      InstallPlugin.installApk(
              result.files.single.path ?? '', 'com.zaihui.installpluginexample')
          .then((result) {
        print('install apk $result');
      }).catchError((error) {
        print('install apk error: $error');
      });
    } else {
      // User canceled the picker
    }
  }

  Future<void> onClickGotoAppStore(String url) async {
    url = url.isEmpty ? _defaultUrl : url;
    final Map<Object?, Object?> res = await InstallPlugin.gotoAppStore(url);
    print(
        "跳转appstroe ${res['isSuccess'] == true ? '成功' : '失败:'}${res['errorMessage'] ?? ''}");
    Utils.toast(
        "跳转appstroe ${res['isSuccess'] == true ? '成功' : '失败:'}${res['errorMessage'] ?? ''}");
  }
}
