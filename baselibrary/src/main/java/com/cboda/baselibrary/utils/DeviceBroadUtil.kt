package com.cboda.baselibrary.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import com.blankj.utilcode.util.EncryptUtils.encryptMD5ToString
import com.cboda.baselibrary.BuildConfig
import com.cboda.baselibrary.utils.EncryptUtil.aesEncrypt
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_AID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_ALL_ID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_ALL_NOT_ENCRYPT_ID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_BRAND
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_ID
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_MODEL
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEVICE_START_TIME
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
    private var notEncryptId = ""
    private var deviceIdType = 0
//    private var deviceStartTime = 0L

    fun getAndroidId(context: Context? = null): String {
        deviceIdType = BuildConfig.DEVICE_ID_TYPE
        val getAndroidId = getAllDeviceId(context, true)
        return getAndroidId
    }

    fun grtReleaseId(context: Context? = null): String {
        deviceIdType = BuildConfig.DEVICE_ID_TYPE
        val getAndroidId = getAllDeviceId(context, false)
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

    private fun getAllDeviceId(context: Context? = null, canEncrypt: Boolean = true): String {
        var deviceId1 = ""
        if (deviceIdType == 0) {
            deviceId1 = getDeviceUUId()
        } else {
            deviceId1 = if (canEncrypt) allDeviceId else notEncryptId
            val sqlKey = if (canEncrypt) DEVICE_ALL_ID else DEVICE_ALL_NOT_ENCRYPT_ID
            if (TextUtils.isEmpty(deviceId1)) {
                val sqlData = MMKVBroadUtils.getSqlStrData(sqlKey)
                if (TextUtils.isEmpty(sqlData)) {
                    deviceId1 = getAllDeviceId2(context, canEncrypt)
                    MMKVBroadUtils.saveSqlStrData(sqlKey, deviceId1)
                } else {
                    deviceId1 = sqlData
                }
                if (canEncrypt) {
                    allDeviceId = deviceId1
                } else {
                    notEncryptId = deviceId1
                }
            }
        }
        return deviceId1
    }


    private fun getAllDeviceId2(context: Context? = null, canEncrypt: Boolean = true): String {
        var getAllDeviceId1 = ""
        var getAllDeviceId2 = ""
        when (deviceIdType) {
            2 -> {
                val dId = getDeviceId(context)
                if (TextUtils.isEmpty(dId)) {
                    val mWId = getWidevineId()
                    if (TextUtils.isEmpty(mWId)) {
                        getAllDeviceId1 = getDeviceUUId()
                        getAllDeviceId2 = getAllDeviceId1
                    } else {
                        getAllDeviceId2 = mWId
                        getAllDeviceId1 = encryptMD5ToString(getAllDeviceId2)
                    }
                } else {
                    getAllDeviceId2 = dId
                    getAllDeviceId1 = encryptMD5ToString(getAllDeviceId2)
                }
            }

            3 -> {
                val dId = getDeviceId(context)
                val aId = getSysAndroidId(context)
                val mWId = getWidevineId()
                val openTime = getDeviceOpenTime()
                val openTimeStr = openTime.toString()
                val aesKey = if (!TextUtils.isEmpty(aId) && aId.length == 16) {
                    aId
                } else if (openTimeStr.length > 8) {
                    (openTimeStr + openTimeStr).substring(0, 16)
                } else {
                    getDeviceUUId().substring(0, 16)
                }
                val jmId = if (!TextUtils.isEmpty(aId)) {
                    aId
                } else {
                    if (openTime > 0L && openTimeStr.length > 15) {
                        openTimeStr
                    } else {
                        getDeviceUUId()
                    }
                }
                val jmId2 = if (TextUtils.isEmpty(mWId)) "$jmId#$dId" else "$jmId#$mWId#$dId"
                getAllDeviceId2 = "$jmId2#$aesKey"
                val jmId3 = aesEncrypt(jmId2, aesKey = aesKey, flags = Base64.NO_WRAP).replace("/", "").replace("=", "").replace("+", "")
                val jmId5 = aesEncrypt(jmId, aesKey = aesKey, flags = Base64.NO_WRAP).replace("/", "").replace("=", "").replace("+", "")
                val jmId6 = encryptMD5ToString(jmId3 + jmId5)
                getAllDeviceId1 = (jmId3.substring(0, 7) + jmId6 + jmId + jmId5.takeLast(11)).uppercase()
            }

            5 -> {
                val dId = getDeviceId(context)
                val mWId = getWidevineId()
                val openTime = getDeviceOpenTime()
                getAllDeviceId2 = dId + mWId + openTime
                getAllDeviceId1 = encryptMD5ToString(dId) + encryptMD5ToString(mWId) + encryptMD5ToString("$openTime")
            }

            else -> {
                val mWId = getWidevineId()
                if (TextUtils.isEmpty(mWId)) {
                    val dId = getDeviceId(context)
                    if (TextUtils.isEmpty(dId)) {
                        getAllDeviceId1 = getDeviceUUId()
                        getAllDeviceId2 = getAllDeviceId1
                    } else {
                        getAllDeviceId2 = dId
                        getAllDeviceId1 = encryptMD5ToString(getAllDeviceId2)
                    }
                } else {
                    getAllDeviceId2 = mWId
                    getAllDeviceId1 = encryptMD5ToString(getAllDeviceId2)
                }
            }
        }
        var deviceId1 = if (canEncrypt) notEncryptId else allDeviceId
        val sqlKey = if (canEncrypt) DEVICE_ALL_NOT_ENCRYPT_ID else DEVICE_ALL_ID
        val byData = if (canEncrypt) getAllDeviceId2 else getAllDeviceId1
        if (TextUtils.isEmpty(deviceId1)) {
            val sqlData = MMKVBroadUtils.getSqlStrData(sqlKey)
            if (TextUtils.isEmpty(sqlData)) {
                deviceId1 = byData
                MMKVBroadUtils.saveSqlStrData(sqlKey, byData)
            } else {
                deviceId1 = sqlData
            }
            if (canEncrypt) {
                notEncryptId = deviceId1
            } else {
                allDeviceId = deviceId1
            }
        }
        return if (canEncrypt) getAllDeviceId1 else getAllDeviceId2
    }

//    private fun getDeviceStartTime(): Long {
//        if (deviceStartTime == 0L) {
//            val sqlData = MMKVBroadUtils.getSqlLongData(DEVICE_START_TIME)
//            if (sqlData == 0L) {
//                deviceStartTime = getDeviceOpenTime()
//                MMKVBroadUtils.saveSqlLongData(DEVICE_UID, deviceStartTime)
//            } else {
//                deviceStartTime = sqlData
//            }
//        }
//        return deviceStartTime
//    }

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

    private fun getDeviceId(context: Context? = null): String {
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
                    val deviceIdBefore2 = if (deviceIdType == 1 || deviceIdType == 3) deviceIdBefore else deviceIdBefore + getSysAndroidId(context)
                    val deviceIdBefore3 = deviceIdBefore2.replace("release-keys", "").trim()
                    val matcher = pattern.matcher(deviceIdBefore3)
                    val deviceIdBefore4 = matcher.replaceAll("")
                        .replace("-", "").replace("/", "")
                        .replace(":", "").replace(".", "")
                        .replace("=", "").replace("\\", "")
                        .replace("+", "").replace("_", "").trim()
//                    deviceId = encryptMD5ToString(deviceIdBefore4)
                    deviceId = deviceIdBefore4
                    MMKVBroadUtils.saveSqlStrData(DEVICE_ID, deviceId)
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
                        wId = if (deviceIdType == 2) wId2 + getDeviceOpenTime() else wId2
                        MMKVBroadUtils.saveSqlStrData(DEVICE_WID, wId)
                    } catch (e: Exception) {
                        e.toLog(TAG)
                        if (deviceIdType != 1 && deviceIdType != 3) {
                            wId = "${getDeviceOpenTime()}"
                        }
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