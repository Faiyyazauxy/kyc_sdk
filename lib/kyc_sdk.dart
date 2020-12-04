import 'dart:async';

import 'package:flutter/services.dart';
import 'package:kyc_sdk/kycsdk_config.dart';

class KycSdk {
  static const MethodChannel _channel = const MethodChannel('kyc_sdk');

  static Future<String> getAadhaarData({
    KycsdkConfig kycsdkConfig,
  }) async {
    final String data = await _channel.invokeMethod(
      'kycVerification',
      kycsdkConfig.toJson(),
    );
    return data;
  }
}
