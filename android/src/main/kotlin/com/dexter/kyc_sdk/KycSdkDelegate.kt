package com.dexter.kyc_sdk

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.khoslalabs.base.ViKycResults
import com.khoslalabs.facesdk.FaceSdkModuleFactory
import com.khoslalabs.ocrsdk.OcrSdkModuleFactory
import com.khoslalabs.videoidkyc.ui.init.VideoIdKycInitActivity
import com.khoslalabs.videoidkyc.ui.init.VideoIdKycInitRequest
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class KycSdkDelegate(private val activity: Activity) : PluginRegistry.ActivityResultListener {
    private var pendingResult: MethodChannel.Result? = null
    fun startKYC(call: MethodCall, result: MethodChannel.Result?) {
        pendingResult = result;
        val requestId = UUID.randomUUID().toString()
        val hash: String? = generateInitialiseHash(requestId)

        val request = VideoIdKycInitRequest.Builder(Constants.CLIENT_CODE, Constants.API_KEY, "KYC", requestId, hash!!)
                .moduleFactory(FaceSdkModuleFactory.newInstance())
                .moduleFactory(OcrSdkModuleFactory.newInstance())
                .screenTitle("App Name")
                .build()
        val myIntent = Intent(activity, VideoIdKycInitActivity::class.java)
        myIntent.putExtra("init_request", request)
        activity.startActivityForResult(myIntent, INIT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == INIT_REQUEST_CODE) {
            if (resultCode == ViKycResults.RESULT_OK || resultCode == ViKycResults.RESULT_DOC_COMPLETE) {
                if (data != null) {
                    val id = data.getStringExtra("user_id")
                    return if (!id.equals("", ignoreCase = true)) {
                        finishWithSuccess(id!!)
                        true
                    } else {
                        false
                    }
                }
            } else {
                if (data != null) {
                    val error = data.getStringExtra("error_message")
                    finishWithError(error!!)
                    return false
                }
            }
        }
        return false
    }

    private fun finishWithSuccess(data: String) {
        if (pendingResult != null) {
            pendingResult!!.success(data)
            clearMethodCallAndResult()
        }
    }

    private fun finishWithError(errorMessage: String) {
        if (pendingResult != null) {
            pendingResult!!.error("error_message", errorMessage, null)
            clearMethodCallAndResult()
        }
    }

    private fun clearMethodCallAndResult() {
        pendingResult = null
    }

    private fun generateInitialiseHash(requestId: String): String? {
        //<client_code>|<request_id>|<api_key>|<salt>
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            val text: String = Constants.CLIENT_CODE + "|" + requestId + "|" + Constants.API_KEY + "|" + Constants.SALT
            // Change this to UTF-16 if needed
            md.update(text.toByteArray(StandardCharsets.UTF_8))
            val digest = md.digest()
            return String.format("%064x", BigInteger(1, digest))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    companion object {
        private const val INIT_REQUEST_CODE  = 1001
    }
}