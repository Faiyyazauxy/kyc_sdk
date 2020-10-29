import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:kyc_sdk/kyc_sdk.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _text = 'Verify';

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: GestureDetector(
            onTap: () async {
              try {
                String data = await KycSdk.getAadhaarData();
                _text = data;
                setState(() {});
              } on PlatformException catch (e) {
                _text = e.message;
                setState(() {});
              }
            },
            child: Text(_text),
          ),
        ),
      ),
    );
  }
}
