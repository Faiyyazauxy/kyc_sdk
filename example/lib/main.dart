import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:kyc_sdk/kyc_sdk.dart';
import 'package:kyc_sdk/kycsdk_config.dart';
import 'package:uuid/uuid.dart';

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
                String data = await KycSdk.getAadhaarData(
                  kycsdkConfig: KycsdkConfig(
                    appName: 'Jewell',
                    url: 'https://sandbox.veri5digital.com/',
                    clientCode: 'OZEL6526',
                    apiKey: 'FM63634NF',
                    purpose: 'kyc',
                    salt: 'r84734475',
                    runMode: 'TRIAL',
                    sdkVersion: '4.2.0',
                    functionCode: 'REVISED',
                  ),
                );
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
