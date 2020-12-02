package com.dexter.kycsdk.network

class KYCResponse {
    /**
     * response_status : {"status":"Success","code":200,"message":"Generated successfully"}
     * response_data : {"encrypted":"<YES></YES>|NO>","kyc_info":"base64 encoded KYC info","hash":"abc"}
     */
    var response_status: ResponseStatusBean? = null
    var response_data: ResponseDataBean? = null

    class ResponseStatusBean {
        /**
         * status : Success
         * code : 200
         * message : Generated successfully
         */
        var status: String? = null
        var code = 0
        var message: String? = null
    }

    class ResponseDataBean {
        /**
         * encrypted : <YES></YES>|NO>
         * kyc_info : base64 encoded KYC info
         * hash : abc
         */
        var encrypted: String? = null
        var kyc_info: String? = null
        var hash: String? = null
    }
}