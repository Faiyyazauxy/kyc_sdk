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
    private var requestId: String = ""
    private var salt: String = ""
    private var runMode: String = ""
    private var sdkVersion: String = ""
    private var functionCode: String = ""

    private fun startKYC(call: MethodCall, result: MethodChannel.Result?) {
        val hash: String? = generateInitialiseHash(requestId)

        val request = VideoIdKycInitRequest.Builder(clientCode, apiKey, purpose, requestId, hash!!)
                .moduleFactory(FaceSdkModuleFactory.newInstance())
                .moduleFactory(OcrSdkModuleFactory.newInstance())
                .plmaRequired("NO")
                .screenTitle(appName)
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
                    if (!id.equals("", ignoreCase = true)) {
                        callKycAPI(id);
                    } else {
                        finishWithError("No Data Found")
                    }
                }
            } else {
                if (data != null) {
                    val error = data.getStringExtra("error_message")
                    finishWithError(error!!)
                }
            }
        }
        return false
    }

    private fun callKycAPI(id: String?) {
        val hash: String = generateRequestHash(id!!)!!
        val requestID = UUID.randomUUID().toString()
        val apiInterface: APIInterface = APIClient.getClient(activity, url)!!.create(APIInterface::class.java)
        val headersBean: KYCRequest.HeadersBean = KYCRequest.HeadersBean(clientCode, clientCode, "CUSTOMER", "ANDROID_SDK", requestID, "DEFAULT", "DEFAULT", "", System.currentTimeMillis().toString(), runMode, "", "SELF", sdkVersion, functionCode, functionCode)
        apiInterface.postKyc(KYCRequest(headersBean, KYCRequest.RequestBean(apiKey, requestID, id, hash)))!!.enqueue(object : Callback<KYCResponse?> {
            override fun onResponse(call: Call<KYCResponse?>?, response: Response<KYCResponse?>) {
                if (response.isSuccessful) {
                    val kycResponse: KYCResponse? = response.body()
                    if (kycResponse != null) {
                        val data: ByteArray = Base64.decode(kycResponse.response_data!!.kyc_info, Base64.DEFAULT)
                        val text = String(data, StandardCharsets.UTF_8)
                        finishWithSuccess(text)
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
                    else -> {
                        finishWithError("You are not connected to the Internet")
                    }
                }
            }
        })
    }

    private fun generateInitialiseHash(requestId: String): String? {
        //<client_code>|<request_id>|<api_key>|<salt>
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            val text = "$clientCode|$requestId|$apiKey|$salt"
            // Change this to UTF-16 if needed
            md.update(text.toByteArray(StandardCharsets.UTF_8))
            val digest = md.digest()
            return String.format("%064x", BigInteger(1, digest))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun generateRequestHash(userId: String): String? {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            val text = "$clientCode|$userId|$apiKey|$salt"
            // Change this to UTF-16 if needed
            md.update(text.toByteArray(StandardCharsets.UTF_8))
            val digest = md.digest()
            return String.format("%064x", BigInteger(1, digest))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getStatus(methodCall: MethodCall, result: MethodChannel.Result?) {

        pendingResult = result

        appName = methodCall.argument("app_name")!!
        url = methodCall.argument("url")!!
        clientCode = methodCall.argument("client_code")!!
        apiKey = methodCall.argument("api_key")!!
        purpose = methodCall.argument("purpose")!!
        requestId = methodCall.argument("request_id")!!
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
                            startKYC(methodCall, result)
                        } else {
                            finishWithError("Currently Aadhaar service is down")
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

    companion object {
        private const val INIT_REQUEST_CODE = 1001
    }
}