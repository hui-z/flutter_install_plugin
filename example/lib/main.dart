import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:install_plugin/install_plugin.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _appUrl = '';

  @override
  void initState() {
    super.initState();
    InstallPlugin.instance.setListener((code) {
      print("状态码:$code");
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
            ElevatedButton(
                onPressed: () {
                  onClickInstallApk();
                },
                child: Text('install')),
            TextField(
              decoration: InputDecoration(hintText: 'URL for app store to launch'),
              onChanged: (url) => _appUrl = url,
            ),
            ElevatedButton(onPressed: () => onClickGotoAppStore(_appUrl), child: Text('gotoAppStore'))
          ],
        ),
      ),
    );
  }

  void onClickInstallApk() async {
    FilePickerResult result = await FilePicker.platform.pickFiles();
    if (result != null) {
      Map<PermissionGroup, PermissionStatus> permissions = await PermissionHandler().requestPermissions([PermissionGroup.storage]);
      if (permissions[PermissionGroup.storage] == PermissionStatus.granted) {
        InstallPlugin.installApk(result.files.single.path, 'com.zaihui.installpluginexample').then((result) {
          print('install apk $result');
        }).catchError((error) {
          print('install apk error: $error');
        });
      } else {
        print('Permission request fail!');
      }
    } else {
      // User canceled the picker
    }
  }

  void onClickGotoAppStore(String url) {
    url = url.isEmpty ? 'https://itunes.apple.com/cn/app/%E5%86%8D%E6%83%A0%E5%90%88%E4%BC%99%E4%BA%BA/id1375433239?l=zh&ls=1&mt=8' : url;
    InstallPlugin.gotoAppStore(url);
  }
}
