package com.cboda.baselibrary.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.blankj.utilcode.util.EncryptUtils.encryptMD5ToString
import com.cboda.baselibrary.BuildConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_AID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_ALL_ID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_BRAND
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_ID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_MODEL
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_SYS_AID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_UID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_WID
import com.cboda.baselibrary.utils.TimeBroadBroadUtils.getDeviceOpenTime
import java.util.UUID
import java.util.regex.Pattern
import kotlin.collections.joinToString

object DeviceBroadUtil {
    private const val TAG = "DeviceBroadUtilClass"
    private var deviceId = ""
    private var deviceBrand = ""
    private var deviceModel = ""
    private var androidId = ""
    private var wId = ""
    private var deviceUId = ""
    private var allDeviceId = ""
    private var sysAndroidId = ""
    private val newDeviceId = true

    fun getAndroidId(context: Context? = null): String {
        val getAndroidId = when (BuildConfig.DEVICE_ID_TYPE) {
            1 -> getSysAndroidId(context)
            2 -> getAllDeviceId(context)
            else -> getDeviceUUId()
        }
        return getAndroidId
    }

    @SuppressLint("HardwareIds")
    private fun getSysAndroidId(context: Context? = null): String {
        if (TextUtils.isEmpty(sysAndroidId)) {
            val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_SYS_AID)
            if (TextUtils.isEmpty(sqlData)) {
                if (TextUtils.isEmpty(androidId)) {
                    try {
                        val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_AID)
                        if (TextUtils.isEmpty(sqlData)) {
                            androidId = Settings.Secure.getString(getAppContext(context).contentResolver, Settings.Secure.ANDROID_ID)
                            if (!TextUtils.isEmpty(androidId)) {
                                MMKVBroadUtils.saveSqlStrData(DEVICE_AID, androidId)
                            }
                        } else {
                            androidId = sqlData
                        }
                    } catch (e: Exception) {
                        e.toLog(TAG)
                    }
                }
                this.sysAndroidId = if (TextUtils.isEmpty(androidId)) getDeviceUUId() else androidId
                MMKVBroadUtils.saveSqlStrData(DEVICE_SYS_AID, sysAndroidId)
            } else {
                sysAndroidId = sqlData
            }
        }
        return sysAndroidId
    }

    fun getDeviceBrand(): String {
        try {
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
        } catch (e: Exception) {
            e.toLog(TAG)
            return getDeviceUUId()
        }
    }

    fun getDeviceModel(): String {
        try {
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
        } catch (e: Exception) {
            e.toLog(TAG)
            return getDeviceUUId()
        }
    }

    private fun getAllDeviceId(context: Context? = null): String {
        if (TextUtils.isEmpty(allDeviceId)) {
            val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_ALL_ID)
            if (TextUtils.isEmpty(sqlData)) {
                if (newDeviceId) {
                    val dId = getDeviceId(context)
                    allDeviceId = if (TextUtils.isEmpty(dId)) {
                        val mWId = getWidevineId()
                        if (TextUtils.isEmpty(mWId)) {
                            getDeviceUUId()
                        } else {
                            mWId
                        }
                    } else {
                        dId
                    }
                } else {
                    val mWId = getWidevineId()
                    allDeviceId = if (TextUtils.isEmpty(mWId)) {
                        val dId = getDeviceId(context)
                        if (TextUtils.isEmpty(dId)) {
                            getDeviceUUId()
                        } else {
                            dId
                        }
                    } else {
                        mWId
                    }
                }
                MMKVBroadUtils.saveSqlStrData(DEVICE_ALL_ID, allDeviceId)
            } else {
                allDeviceId = sqlData
            }
        }
        return allDeviceId
    }


    private fun getDeviceUUId(): String {
        if (TextUtils.isEmpty(deviceUId)) {
            val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_UID)
            if (TextUtils.isEmpty(sqlData)) {
                deviceUId = getUuIdMd5()
                MMKVBroadUtils.saveSqlStrData(DEVICE_UID, deviceUId)
            } else {
                deviceUId = sqlData
            }
        }
        return deviceUId
    }

    fun getDeviceId(context: Context? = null, canEncrypt: Boolean = true): String {
        if (TextUtils.isEmpty(deviceId) || !canEncrypt) {
            try {
                val sqlData = MMKVBroadUtils.getSqlStrData(DEVICE_ID)
                if (TextUtils.isEmpty(sqlData) || !canEncrypt) {
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
                    val deviceIdBefore2 = if (newDeviceId) deviceIdBefore + getSysAndroidId(context) else deviceIdBefore
                    val deviceIdBefore3 = deviceIdBefore2.replace("release-keys", "").trim()
                    val matcher = pattern.matcher(deviceIdBefore3)
                    val deviceIdBefore4 = matcher.replaceAll("")
                        .replace("-", "").replace("/", "")
                        .replace(":", "").replace(".", "")
                        .replace("=", "").replace("\\", "")
                        .replace("+", "").replace("_", "").trim()
                    if (canEncrypt) {
                        deviceId = encryptMD5ToString(deviceIdBefore4)
                        MMKVBroadUtils.saveSqlStrData(DEVICE_ID, deviceId)
                    } else {
                        return deviceIdBefore4
                    }
                } else {
                    deviceId = sqlData
                }
            } catch (e: Exception) {
                e.toLog(TAG)
            }
        }
        return deviceId
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
                        val wId2 = deviceId.joinToString("") { "%02x".format(it) }
                        wId = if (newDeviceId) encryptMD5ToString(wId2 + getDeviceOpenTime()) else encryptMD5ToString(wId2)
                        MMKVBroadUtils.saveSqlStrData(DEVICE_WID, wId)
                    } catch (e: Exception) {
                        e.toLog(TAG)
                    }
                    mediaDrm?.close()
                } catch (e: Exception) {
                    e.toLog(TAG)
                }
            } else {
                wId = sqlData
            }
        }
        return wId
    }

}