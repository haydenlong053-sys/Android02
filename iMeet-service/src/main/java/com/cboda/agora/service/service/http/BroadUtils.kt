package com.cboda.agora.service.service.http

import com.cboda.agora.service.service.callback.AUIBroadException
import org.json.JSONObject
import retrofit2.Response

class BroadUtils {
    companion object {

        @JvmStatic
        fun <T>errorFromResponse(response: Response<T>): AUIBroadException {
            val errorMsg = response.errorBody()?.string()
            var code = -1
            var msg = "error"
            if (errorMsg != null) {
                val obj = JSONObject(errorMsg)
                if (obj.has("code")){
                    code = obj.getInt("code")
                }
                if (obj.has("message")){
                    msg = obj.getString("message")
                }
            }
            return AUIBroadException(code, msg)
        }

    }
}