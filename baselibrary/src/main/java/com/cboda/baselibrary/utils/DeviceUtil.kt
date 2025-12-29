package com.cboda.baselibrary.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import java.util.regex.Pattern

object DeviceUtil {
    private var deviceId = ""
    fun getDeviceBrand(): String {
        return Build.BRAND
    }

    fun getDeviceModel(): String {
        return Build.MODEL
    }

    fun getAndroidId(context: Context? = null): String {
        return Settings.Secure.getString(
            getAppContext(context).contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun getDeviceId(context: Context? = null): String {
        if (TextUtils.isEmpty(deviceId)) {
            val pattern = Pattern.compile("\\s*|\t|\r|\n")
            try {
                val board = Build.BOARD.length % 10
                val brand = getDeviceBrand().length % 10
                val device = Build.DEVICE.length % 10
                val display = Build.DISPLAY.length % 10
                val host = Build.HOST.length % 10
                val id = Build.ID.length % 10
                val manufacturer = Build.MANUFACTURER.length % 10
                val model = getDeviceModel().length % 10
                val product = Build.PRODUCT.length % 10
                val tags = Build.TAGS.length % 10
                val type = Build.TYPE.length % 10
                val user = Build.USER.length % 10
                val finGerPrint = Build.FINGERPRINT
                val finGerPrintLength = finGerPrint.length % 10
                val allDeviceId =
                    board + brand + device + display + host + id + manufacturer + model + product + tags + type + user + finGerPrintLength
                val androidId = getAndroidId(context)
                val deviceIdBefore =
                    allDeviceId.toString().trim() + finGerPrint.trim() + androidId.trim()
                val deviceIdBefore2 = deviceIdBefore.replace("release-keys", "").trim()
                val matcher = pattern.matcher(deviceIdBefore2)
                val deviceIdBefore4 = matcher.replaceAll("")
                    .replace("-", "").replace("/", "")
                    .replace(":", "").replace(".", "")
                    .replace("=", "").replace("\\", "")
                    .replace("+", "").replace("_", "").trim()
                deviceId = deviceIdBefore4
                return deviceId
            } catch (_: Exception) {
                return getUuIdCreate()
            }
        } else {
            return deviceId
        }
    }

}