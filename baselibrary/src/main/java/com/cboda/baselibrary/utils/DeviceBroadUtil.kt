package com.cboda.baselibrary.utils

import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.blankj.utilcode.util.EncryptUtils.encryptMD5ToString
import com.cboda.baselibrary.BuildConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_AID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_BRAND
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_ID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_MODEL
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_WID
import java.util.UUID
import java.util.regex.Pattern
import kotlin.collections.joinToString

object DeviceBroadUtil {
    private var deviceId = ""
    private var deviceBrand = ""
    private var deviceModel = ""
    private var androidId = ""
    private var wId = ""

    fun getAndroidId(context: Context? = null): String {
        if (BuildConfig.IS_NEW_SIGNATURE) {
            return getDeviceId()
        } else {
            if (TextUtils.isEmpty(androidId)) {
                val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_AID)
                if (TextUtils.isEmpty(sqlData)) {
                    androidId = Settings.Secure.getString(getAppContext(context).contentResolver, Settings.Secure.ANDROID_ID)
                    MMKVBroadUtils.saveSqlStrData(DEVICE_AID, androidId)
                } else {
                    androidId = sqlData
                }
            }
            return androidId
        }
    }

    fun getDeviceBrand(): String {
        if (TextUtils.isEmpty(deviceBrand)) {
            val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_BRAND)
            if (TextUtils.isEmpty(sqlData)) {
                deviceBrand = Build.BRAND
                MMKVBroadUtils.saveSqlStrData(DEVICE_BRAND, deviceBrand)
            } else {
                deviceBrand = sqlData
            }
        }
        return deviceBrand
    }

    fun getDeviceModel(): String {
        if (TextUtils.isEmpty(deviceModel)) {
            val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_MODEL)
            if (TextUtils.isEmpty(sqlData)) {
                deviceModel = Build.MODEL
                MMKVBroadUtils.saveSqlStrData(DEVICE_MODEL, deviceModel)
            } else {
                deviceModel = sqlData
            }
        }
        return deviceModel
    }

    fun getDeviceId(): String {
        val mWId = getWidevineId()
        if (TextUtils.isEmpty(mWId)) {
            if (TextUtils.isEmpty(deviceId)) {
                try {
                    val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_ID)
                    if (TextUtils.isEmpty(sqlData)) {
                        val pattern = Pattern.compile("\\s*|\t|\r|\n")
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
                        val deviceIdBefore =
                            allDeviceId.toString().trim() + finGerPrint.trim()
                        val deviceIdBefore2 = deviceIdBefore.replace("release-keys", "").trim()
                        val matcher = pattern.matcher(deviceIdBefore2)
                        val deviceIdBefore4 = matcher.replaceAll("")
                            .replace("-", "").replace("/", "")
                            .replace(":", "").replace(".", "")
                            .replace("=", "").replace("\\", "")
                            .replace("+", "").replace("_", "").trim()
//                    deviceId = deviceIdBefore4
                        deviceId = encryptMD5ToString(deviceIdBefore4)
                        MMKVBroadUtils.saveSqlStrData(DEVICE_ID, deviceId)
                    } else {
                        deviceId = sqlData
                    }
                    return deviceId
                } catch (_: Exception) {
                    return getUuIdCreate()
                }
            } else {
                return deviceId
            }
        } else {
            return mWId
        }

    }

    private fun getWidevineId(): String {
        if (TextUtils.isEmpty(wId)) {
            val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_WID)
            if (TextUtils.isEmpty(sqlData)) {
                try {
                    var mediaDrm: MediaDrm? = null
                    try {
                        mediaDrm = MediaDrm(UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed"))
                        val deviceId = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                        wId = encryptMD5ToString(deviceId.joinToString("") { "%02x".format(it) })
                        MMKVBroadUtils.saveSqlStrData(DEVICE_WID, wId)
                    } catch (_: Exception) {
                    }
                    mediaDrm?.close()
                } catch (_: Exception) {
                }
            } else {
                wId = sqlData
            }
        }
        return wId
    }

}