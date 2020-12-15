// To parse this JSON data, do
//
//     final kycsdkConfig = kycsdkConfigFromJson(jsonString);

import 'dart:convert';

KycsdkConfig kycsdkConfigFromJson(String str) =>
    KycsdkConfig.fromJson(json.decode(str));

String kycsdkConfigToJson(KycsdkConfig data) => json.encode(data.toJson());

class KycsdkConfig {
  KycsdkConfig({
    this.appName,
    this.url,
    this.clientCode,
    this.apiKey,
    this.purpose,
    this.salt,
    this.runMode,
    this.sdkVersion,
    this.functionCode,
  });

  String appName;
  String url;
  String clientCode;
  String apiKey;
  String purpose;
  String requestId;
  String salt;
  String runMode;
  String sdkVersion;
  String functionCode;

  factory KycsdkConfig.fromJson(Map<String, dynamic> json) => KycsdkConfig(
        appName: json["app_name"],
        url: json["url"],
        clientCode: json["client_code"],
        apiKey: json["api_key"],
        purpose: json["purpose"],
        salt: json["salt"],
        runMode: json["run_mode"],
        sdkVersion: json["sdk_version"],
        functionCode: json["function_code"],
      );

  Map<String, dynamic> toJson() => {
        "app_name": appName,
        "url": url,
        "client_code": clientCode,
        "api_key": apiKey,
        "purpose": purpose,
        "request_id": requestId,
        "salt": salt,
        "run_mode": runMode,
        "sdk_version": sdkVersion,
        "function_code": functionCode,
      };
}
