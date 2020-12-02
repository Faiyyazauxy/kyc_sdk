package com.dexter.kycsdk.network

class KYCRequest(
        /**
         * headers : {"client_code":"<your client_code>","sub_client_code":"<should be same as client_code>","actor_type":"NA","channel_code":"ANDROID_SDK","stan":"<System Trace Number>","user_handle_type":"<>","user_handle_value":"<>","location":"","transmission_datetime":"1533123525716","run_mode":"REAL","client_ip":"192.168.0.1","operation_mode":"SELF","channel_version":"0.0.1","function_code":"REVISED","function_sub_code":"DEFAULT"}
         * request : {"api_key":"<api-key>","request_id":"rid1234567890","user_id":"userid","hash":"###########"}
        </api-key></System></should></your> */
        var headers: HeadersBean, var request: RequestBean) {

    class HeadersBean(
            /**
             * client_code : <your client_code>
             * sub_client_code : <should be same as client_code>
             * actor_type : NA
             * channel_code : ANDROID_SDK
             * stan : <System Trace Number>
             * user_handle_type : <>
             * user_handle_value : <>
             * location :
             * transmission_datetime : 1533123525716
             * run_mode : REAL
             * client_ip : 192.168.0.1
             * operation_mode : SELF
             * channel_version : 0.0.1
             * function_code : REVISED
             * function_sub_code : DEFAULT
            </System></should></your> */
            var client_code: String, var sub_client_code: String, var actor_type: String, var channel_code: String, var stan: String, var user_handle_type: String, var user_handle_value: String, var location: String, var transmission_datetime: String, var run_mode: String, var client_ip: String, var operation_mode: String, var channel_version: String, var function_code: String, var function_sub_code: String)

    class RequestBean(
            /**
             * api_key : <api-key>
             * request_id : rid1234567890
             * user_id : userid
             * hash : ###########
            </api-key> */
            var api_key: String, var request_id: String, var user_id: String, var hash: String)
}