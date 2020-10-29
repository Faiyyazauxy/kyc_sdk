#import "KycSdkPlugin.h"
#if __has_include(<kyc_sdk/kyc_sdk-Swift.h>)
#import <kyc_sdk/kyc_sdk-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "kyc_sdk-Swift.h"
#endif

@implementation KycSdkPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftKycSdkPlugin registerWithRegistrar:registrar];
}
@end
