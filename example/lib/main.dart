import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:install_plugin/install_plugin.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _appUrl = '';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await InstallPlugin.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new Column(
          children: <Widget>[
            Text(_platformVersion),
            FlatButton(
                onPressed: () {
                  onClickInstallApk();
                },
                child: Text('install')),
            TextField(
              decoration:
                  InputDecoration(hintText: 'URL for app store to launch'),
              onChanged: (url) => _appUrl = url,
            ),
            FlatButton(
                onPressed: () => onClickGotoAppStore(_appUrl),
                child: Text('gotoAppStore'))
          ],
        ),
      ),
    );
  }

  void onClickInstallApk() {
    InstallPlugin.installApk('/storage/emulated/0/zpartner/update.apk',
            'com.zaihui.installpluginexample')
        .then((result) {
      print('install apk $result');
    }).catchError((error) {
      print('install apk error: $error');
    });
  }

  void onClickGotoAppStore(String url) {
    url = url.isEmpty
        ? 'https://itunes.apple.com/cn/app/%E5%86%8D%E6%83%A0%E5%90%88%E4%BC%99%E4%BA%BA/id1375433239?l=zh&ls=1&mt=8'
        : url;
    InstallPlugin.gotoAppStore(url);
  }
}
