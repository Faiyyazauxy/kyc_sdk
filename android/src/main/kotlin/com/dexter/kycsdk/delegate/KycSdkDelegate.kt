package com.dexter.kycsdk.delegate

import android.app.Activity
import android.content.Intent
import android.util.Base64
import com.dexter.kycsdk.network.*
import com.khoslalabs.base.ViKycResults
import com.khoslalabs.facesdk.FaceSdkModuleFactory
import com.khoslalabs.ocrsdk.OcrSdkModuleFactory
import com.khoslalabs.videoidkyc.ui.init.VideoIdKycInitActivity
import com.khoslalabs.videoidkyc.ui.init.VideoIdKycInitRequest
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

class KycSdkDelegate(private val activity: Activity) : PluginRegistry.ActivityResultListener {
    private var pendingResult: MethodChannel.Result? = null
    private var appName: String = ""
    private var clientCode: String = ""
    private var url: String = ""
    private var apiKey: String = ""
    private var purpose: String = ""
    private var requestID: String = ""
    private var salt: String = ""
    private var runMode: String = ""
    private var sdkVersion: String = ""
    private var functionCode: String = ""

    private fun startKYC() {
        requestID = UUID.randomUUID().toString()
        val hash: String = generateInitialiseHash(requestID)
        val videoIdKycInitRequest = VideoIdKycInitRequest.Builder(
                clientCode,
                apiKey,
                purpose,
                requestID,
                hash
        )
                .plmaRequired("NO")
                .moduleFactory(OcrSdkModuleFactory.newInstance())
                .moduleFactory(FaceSdkModuleFactory.newInstance())
                .build()

        val myIntent = Intent(activity, VideoIdKycInitActivity::class.java)
        myIntent.putExtra("init_request", videoIdKycInitRequest)
        activity.startActivityForResult(myIntent, INIT_REQUEST_CODE)
    }

    private fun callKycAPI(id: String) {
        val hash: String = generateRequestHash(id)
        val apiInterface: APIInterface = APIClient.getClient(activity, url)!!.create(APIInterface::class.java)
        val headersBean = Headers(
                clientCode,
                clientCode,
                "CUSTOMER",
                "ANDROID_SDK",
                requestID,
                "mail",
                "a@b.c",
                "New Delhi",
                System.currentTimeMillis().toString(),
                runMode,
                "192.0.2.0",
                "SELF",
                sdkVersion,
                functionCode,
                functionCode
        )

        val request = Request(apiKey, id, hash);

        apiInterface.postKyc(KYCRequest(headersBean, request))!!.enqueue(object : Callback<KYCResponse?> {
            override fun onResponse(call: Call<KYCResponse?>?, response: Response<KYCResponse?>) {
                if (response.isSuccessful) {
                    val kycResponse: KYCResponse? = response.body()
                    if (kycResponse != null) {
                        if (kycResponse.responseStatus.status == "SUCCESS") {
                            val data: ByteArray = Base64.decode(kycResponse.responseData.kycInfo, Base64.DEFAULT)
                            val text = String(data, StandardCharsets.UTF_8)
                            finishWithSuccess(text)
                        } else {
                            finishWithError( kycResponse.responseStatus.message)
                        }
                    } else {
                        finishWithError("Some error occurred")
                    }
                }
            }

            override fun onFailure(call: Call<KYCResponse?>?, t: Throwable) {
                return when (t) {
                    is SSLHandshakeException -> {
                        finishWithError("Your wifi firewall may be blocking your access to our service. Please switch your internet connection")
                    }
                    is TimeoutException -> {
                        finishWithError("There seems to be an error with your connection")
                    }
                    is java.net.SocketTimeoutException -> {
                        finishWithError("There seems to be an error with your connection")
                    }
                    else -> {
                        finishWithError("You are not connected to the Internet")
                    }
                }
            }
        })
    }

    fun getStatus(methodCall: MethodCall, result: MethodChannel.Result?) {

        pendingResult = result

        appName = methodCall.argument("app_name")!!
        url = methodCall.argument("url")!!
        clientCode = methodCall.argument("client_code")!!
        apiKey = methodCall.argument("api_key")!!
        purpose = methodCall.argument("purpose")!!
        salt = methodCall.argument("salt")!!
        runMode = methodCall.argument("run_mode")!!
        sdkVersion = methodCall.argument("sdk_version")!!
        functionCode = methodCall.argument("function_code")!!

        val apiInterface: APIInterface = APIClient.getClient(activity, url)!!.create(APIInterface::class.java)
        apiInterface.getStatus()!!.enqueue(object : Callback<UIDAIResponse?> {
            override fun onResponse(call: Call<UIDAIResponse?>?, response: Response<UIDAIResponse?>) {
                if (response.isSuccessful) {
                    val statusResponse: UIDAIResponse? = response.body()
                    if (statusResponse != null) {
                        if (statusResponse.status == "SUCCESS") {
                            startKYC()
                        } else {
                            finishWithError(statusResponse.message)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UIDAIResponse?>?, t: Throwable) {
                return when (t) {
                    is SSLHandshakeException -> {
                        finishWithError("Your wifi firewall may be blocking your access to our service. Please switch your internet connection")
                    }
                    is TimeoutException -> {
                        finishWithError("There seems to be an error with your connection")
                    }
                    is java.net.SocketTimeoutException -> {
                        finishWithError("There seems to be an error with your connection")
                    }
                    else -> {
                        finishWithError("You are not connected to the Internet")
                    }
                }
            }
        })
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

    private fun generateRequestHash(userId: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            val text =
                    "$clientCode|$userId|$apiKey|$salt"
            // Change this to UTF-16 if needed
            md.update(text.toByteArray(StandardCharsets.UTF_8))
            val digest: ByteArray = md.digest()
            return java.lang.String.format("%064x", BigInteger(1, digest))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }


    private fun generateInitialiseHash(requestId: String): String {
        //<client_code>|<request_id>|<api_key>|<salt>
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            val text =
                    "$clientCode|$requestId|$apiKey|$salt"
            // Change this to UTF-16 if needed
            md.update(text.toByteArray(StandardCharsets.UTF_8))
            val digest: ByteArray = md.digest()
            return java.lang.String.format("%064x", BigInteger(1, digest))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == INIT_REQUEST_CODE) {
            if (resultCode == ViKycResults.RESULT_OK || resultCode == ViKycResults.RESULT_DOC_COMPLETE) {
                if (data != null) {
                    val userId = data.getStringExtra("user_id")
                    return if (userId != null) {
                        callKycAPI(userId)
                        true
                    } else {
                        finishWithError("Null user ID")
                        false
                    }
                }
            } else {
                if (data != null) {
                    val error = data.getStringExtra("error_message")
                    return if (error != null) {
                        finishWithError(error)
                        false
                    } else {
                        finishWithError("Some error occurred")
                        false
                    }
                }
            }
        }
        return false
    }

    companion object {
        private const val INIT_REQUEST_CODE = 1001
    }
}