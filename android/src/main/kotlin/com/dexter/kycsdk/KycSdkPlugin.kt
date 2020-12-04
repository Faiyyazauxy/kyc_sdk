package com.dexter.kycsdk

import android.app.Activity
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import com.dexter.kycsdk.delegate.KycSdkDelegate
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/**
 * KycSdkPlugin
 */
class KycSdkPlugin : MethodCallHandler, FlutterPlugin, ActivityAware {
  companion object {
    private const val CHANNEL = "kyc_sdk"
    init {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
  }

  private var delegate: KycSdkDelegate? = null
  private var activityPluginBinding: ActivityPluginBinding? = null

  private fun setupEngine(messenger: BinaryMessenger) {
    val channel = MethodChannel(messenger, CHANNEL)
    channel.setMethodCallHandler(this)
  }

  private fun setupActivity(activity: Activity?): KycSdkDelegate? {
    delegate = KycSdkDelegate(activity!!)
    return delegate
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "kycVerification") {
      delegate!!.getStatus(call, result)
    }
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
    setupEngine(flutterPluginBinding.binaryMessenger)
  }

  override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
    setupActivity(activityPluginBinding.activity)
    this.activityPluginBinding = activityPluginBinding
    activityPluginBinding.addActivityResultListener(delegate!!)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPluginBinding) {
    // no need to clear channel
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(activityPluginBinding!!)
  }

  override fun onDetachedFromActivity() {
    activityPluginBinding!!.removeActivityResultListener(delegate!!)
    activityPluginBinding = null
    delegate = null
  }
}