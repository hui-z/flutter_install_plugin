import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:install_plugin/install_plugin.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await InstallPlugin.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
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
            FlatButton(
                onPressed: () {
                  onClickInstallApk();
                },
                child: Text('install')),
            FlatButton(onPressed: (){
              onClickGotoAppStore();
            }, child: Text('gotoAppStore'))
          ],
        ),
      ),
    );
  }

  void onClickInstallApk() {
    requestPermission(PermissionGroup.storage).then((result) {
      print('requestPermission result: $result');
      if (result[PermissionGroup.storage] == PermissionStatus.granted) {
        InstallPlugin.installApk('/storage/emulated/0/zpartner/update.apk',
                'com.zaihui.installpluginexample')
            .then((result) {
          print('install apk $result');
        }).catchError((error) {
          print('install apk error: $error');
        });
      } else {}
    }).catchError((error) {
      print('requestPermission error: $error');
    });
  }

  void onClickGotoAppStore() {
    InstallPlugin.gotoAppStore('https://itunes.apple.com/cn/app/%E5%86%8D%E6%83%A0%E5%90%88%E4%BC%99%E4%BA%BA/id1375433239?l=zh&ls=1&mt=8');
  }
  
  Future<Map<PermissionGroup, PermissionStatus>> requestPermission(
      PermissionGroup permission) async {
    final List<PermissionGroup> permissions = <PermissionGroup>[permission];
    return await PermissionHandler().requestPermissions(permissions);
  }

}
