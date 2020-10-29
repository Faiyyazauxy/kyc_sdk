import 'dart:async';

import 'package:flutter/services.dart';

class KycSdk {
  static const MethodChannel _channel = const MethodChannel('kyc_sdk');

  static Future<String> getAadhaarData() async {
    final String data = await _channel.invokeMethod('kycVerification');
    return data;
  }
}
