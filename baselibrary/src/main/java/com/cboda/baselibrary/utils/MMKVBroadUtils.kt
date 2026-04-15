package com.cboda.baselibrary.utils

import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.util.FileUtils
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.bean.GlobalBroadInfo
import com.cboda.baselibrary.bean.GlobalBroadInfo.getFriendData
import com.cboda.baselibrary.bean.GlobalBroadInfo.getGroupData
import com.cboda.baselibrary.bean.GlobalBroadInfo.myGroupListData
import com.cboda.baselibrary.bean.GlobalSmallGroupBroadBean
import com.cboda.select.picture.utils.LanguageUtil
import com.cboda.select.picture.utils.LanguageUtil.ENGLISH
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.get

/**
 * 简述： mmkv本地存储
 */
class MMKVBroadUtils {
    companion object {
        const val OLD_QR_CODE = "oldQrCode"
        const val DEVICE_BRAND = "deviceBrand"
        const val DEVICE_MODEL = "deviceModel"
        const val DEVICE_ID = "deviceIdFive"
        const val DEVICE_AID = "deviceAndroidId"
        const val DEVICE_WID = "deviceWId"
        const val DEVICE_ALL_ID = "deviceAllId"
        const val DEVICE_SYS_AID = "deviceSysAndroidId"
        const val DEVICE_UID = "deviceUId"
        const val NET_TYPE = "appNetType"
        const val DEV_INPUT_BASE_URL = "devInputBaseUrl"
        const val SEARCH_KEY = "searchHistoryKey"
        const val USER_ID_KEY = "userIdKey"

        const val USER_SEND_LINK = "userSendLink"

        const val USER_CREDIT_SCORE_NEW_USER_DAYS = "userCreditScoreNewUserDays"
        const val USER_NAME_KEY = "username"

        const val USER_VIP_KEY = "userVip"

        const val USER_NICE_NUMBER_KEY = "userNiceNumberKey"

        const val USER_IMG_KEY = "userAvatar"
        const val USER_PHONE_KEY = "userPhoneKey"
        const val USER_ONE_KEY_LOGIN_PHONE_KEY = "userOneKeyLoginPhoneKey"
        const val USER_ONE_KEY_LOGIN_TYPE_KEY = "userOneKeyLoginTypeKey"
        const val REAL_USER_ID_KEY = "realUserIdKey"
        const val TX_USER_SIGN = "txUserSign"
        const val APP_PRIVACYREADY = "app_privacyready"

        const val MY_IM_USER_TOKEN = "myImUserToken"
        const val USER_YU_YAN_KEY = "yuyan"

        const val USER_LANGUAGE_KEY = "LanguageKey"
        const val USER_ZHUTI_KEY = "zhuti"
        const val USER_FANYI_KEY = "fanyi"
        const val USER_ZIDONG_FANYI_KEY = "zidongfanyi"
        const val KEFU_ID_KEY = "kefuIdKey"
        const val KEFU_NAME_KEY = "kefuNameKey"
        const val KEY_FRIEND_LIST_DATA = "key_friend_list_data"

        const val KEY_GROUP_LIST_DATA = "key_group_list_data"
        const val KEY_GROUP_NOTICE = "keyGroupNotice_"
        const val KEY_SAVE_COMPRESS_VIDEO = "keySaveCompressVideo"

        const val REAL_USER_M_ID_KEY = "mId"
        fun saveGroupNotice(groupId: String?, desNotice: String?) {
            val kv = MMKV.defaultMMKV()
            kv.encode(KEY_GROUP_NOTICE + groupId, desNotice)
        }

        fun getGroupNotice(groupId: String?): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(KEY_GROUP_NOTICE + groupId)
        }

        fun saveSearchHistory(searchHistory: String?) {
            val kv = MMKV.defaultMMKV()
            kv.encode(SEARCH_KEY + BroadConstant.getAppToken(), searchHistory)
        }

        fun getSearchHistory(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(SEARCH_KEY + BroadConstant.getAppToken())
        }

        fun clearHistory() {
            val kv = MMKV.defaultMMKV()
            kv.remove(SEARCH_KEY + BroadConstant.getAppToken())
        }

        fun saveUserAppToken(appToken: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_ID_KEY, appToken)
        }


        fun saveUserImg(userImg: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_IMG_KEY, userImg)
        }

        fun getUserImg(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_IMG_KEY)
        }


        fun saveUserVip(userVip: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_VIP_KEY, userVip == "1")
        }

        fun getUserVip(): Boolean {
            val kv = MMKV.defaultMMKV()
            return kv.decodeBool(USER_VIP_KEY, false)
        }


        fun saveUserNiceNum(isLiang: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_NICE_NUMBER_KEY, isLiang == "1")
        }

        fun getUserIsNiceNum(): Boolean {
            val kv = MMKV.defaultMMKV()
            return kv.decodeBool(USER_NICE_NUMBER_KEY, false)
        }

        fun saveUserName(userName: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_NAME_KEY, userName)
        }

        fun getUserName(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_NAME_KEY)
        }


        fun getUserAppToken(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_ID_KEY)
        }

        fun saveKeFuId(userId: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(KEFU_ID_KEY, userId)
        }

        //增加默认兜底robot001
        fun getKeFuId(): String {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(KEFU_ID_KEY) ?: "robot001"
        }

        fun saveUserMId(mId: String?) {
            val kv = MMKV.defaultMMKV()
            kv.encode(REAL_USER_M_ID_KEY, mId)
        }

        //用户的mId，用于声网
        fun getUserMId(): String {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(REAL_USER_M_ID_KEY) ?: "0"
        }

        fun saveKeFuName(name: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(KEFU_NAME_KEY, name)
        }

        fun getKeFuName(): String {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(KEFU_NAME_KEY) ?: getAppContext(null).getString(R.string.base_default_kefu_name)
        }

        fun saveRealUserId(realUserId: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(REAL_USER_ID_KEY, realUserId)
        }

        fun getRealUserId(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(REAL_USER_ID_KEY)
        }

        fun saveTxUserSign(txUserSign: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(TX_USER_SIGN, txUserSign)
        }

        fun getTxUserSign(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(TX_USER_SIGN)
        }

        fun saveMyImToken(txUserSign: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(MY_IM_USER_TOKEN, txUserSign)
        }

        fun getMyImToken(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(MY_IM_USER_TOKEN)
        }

        fun clearMyImToken() {
            val kv = MMKV.defaultMMKV()
            kv.remove(MY_IM_USER_TOKEN)
        }


        fun saveOneKeyPhone(phone: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_ONE_KEY_LOGIN_PHONE_KEY, phone)
        }

        fun getOneKeyPhone(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_ONE_KEY_LOGIN_PHONE_KEY)
        }

        fun saveOneKeyType(type: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_ONE_KEY_LOGIN_TYPE_KEY, type)
        }

        fun getOneKeyType(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_ONE_KEY_LOGIN_TYPE_KEY)
        }


        fun clearUserId() {
            val kv = MMKV.defaultMMKV()
            kv.remove(USER_ID_KEY)
        }

        fun clearRealUserId() {
            val kv = MMKV.defaultMMKV()
            kv.remove(REAL_USER_ID_KEY)
        }

        fun saveUserPhone(phone: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_PHONE_KEY, phone)
        }

        fun getUserPhone(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_PHONE_KEY)
        }

        fun clearUserPhone() {
            val kv = MMKV.defaultMMKV()
            kv.remove(USER_PHONE_KEY)
        }

        fun isPrivacyready(): Boolean {
            val kv = MMKV.defaultMMKV()
            return kv.decodeBool(APP_PRIVACYREADY) ?: false
        }

        fun setPrivacyready(b: Boolean) {
            val kv = MMKV.defaultMMKV()
            kv.encode(APP_PRIVACYREADY, b)
        }

        fun saveYuYan(yuyan: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_YU_YAN_KEY, yuyan)
        }

        fun getYuYan(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_YU_YAN_KEY) ?: ENGLISH
        }


        fun getAppLanguageKey(context: Context? = null): String {
//            val kv = MMKV.defaultMMKV()
//            return kv.decodeString(USER_LANGUAGE_KEY) ?: ENGLISH
//            return getSharedPreferences(context).getString(USER_LANGUAGE_KEY, ENGLISH) ?: ENGLISH
            return LanguageUtil.getAppLanguageKey(getAppContext(context), false)
        }


        fun saveAppLanguageKey(languageKey: String, context: Context? = null) {
//            val kv = MMKV.defaultMMKV()
//            kv.encode(USER_LANGUAGE_KEY, languageKey)
//            getSharedPreferences(context).edit()
//                .putString(USER_LANGUAGE_KEY, languageKey)
//                .apply()
            LanguageUtil.saveAppLanguageKey(languageKey, getAppContext(context))
        }


//        private fun getSharedPreferences(context: Context? = null): SharedPreferences {
//            return getAppContext(context).getSharedPreferences(getAppPackName(), Context.MODE_PRIVATE)
//        }

        fun saveZt(yuyan: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_ZHUTI_KEY, yuyan)
        }

        fun getZt(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_ZHUTI_KEY)
        }

        fun saveFanYi(yuyan: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_FANYI_KEY, yuyan)
        }

        fun getFanYi(): String? {
            val kv = MMKV.defaultMMKV()
            var yuyan = kv.decodeString(USER_FANYI_KEY)
            if (yuyan == null) {
                yuyan = "English"
            }
            return yuyan
        }

        fun saveZiDongFanYi(yuyan: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_ZIDONG_FANYI_KEY, yuyan)
        }

        fun getZiDongFanYi(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_ZIDONG_FANYI_KEY)
        }

        fun init(context: Context) {
            MMKV.initialize(context)
        }

        fun saveUserData(data: GlobalFriendBroadBean) {
            val list = arrayListOf<GlobalFriendBroadBean>()
            list.add(data)
            saveUserListData(list)
        }

        fun saveUserData(data: CommonResultBroadBean.CommonResult?) {
            data?.also {
                val bean = GlobalFriendBroadBean()
                bean.userId = data.id
                bean.nickName = data.name
                bean.remarkName = data.remarkName
                data.registerDayBean?.registDay?.also { bean.registerDay = it }
                bean.grade = data.grade
                bean.icon = data.icon
                saveUserData(bean)
            }
        }

        fun saveUserListData(list: List<GlobalFriendBroadBean>, viewModelScope: CoroutineScope? = null) {
            getFriendData(viewModelScope) { map ->
                viewModelScope?.launch {
                    withContext(Dispatchers.IO) {
                        saveUserData(list, map)
                    }
                } ?: let {
                    GlobalScope.launch(Dispatchers.IO) {
                        saveUserData(list, map)
                    }
                }
            }
        }


        fun saveUserData(list: List<GlobalFriendBroadBean>, map: HashMap<String, GlobalFriendBroadBean>) {
            list.forEach { item ->
                if (!TextUtils.isEmpty(item.userId)) {
                    val bean = GlobalFriendBroadBean()
                    bean.registerDay = item.registerDay
                    bean.grade = item.grade
                    bean.icon = item.icon
                    bean.nickName = item.nickName
                    bean.remarkName = item.remarkName
                    map[item.userId ?: ""] = bean
                }
            }
            MMKV.defaultMMKV().encode(KEY_FRIEND_LIST_DATA, toJson2(map))
            GlobalBroadInfo.myFriendListBean = map
        }

        fun getFriendListData(): HashMap<String, GlobalFriendBroadBean> {

            val decodeString = MMKV.defaultMMKV().decodeString(KEY_FRIEND_LIST_DATA)
            return if (decodeString?.isNotEmpty() == true) {
                val type = object : TypeToken<HashMap<String, GlobalFriendBroadBean>>() {}
                fromJsonTypeToken(decodeString, type)
            } else {
                HashMap()
            }
        }

        fun updateGroupNameAndRenameCount(id: String?, name: String?, renameCount: Int?, viewModelScope: CoroutineScope? = null) {
            if (!TextUtils.isEmpty(id) && (!TextUtils.isEmpty(name) || null != renameCount)) {
                getGroupData(viewModelScope) { map ->
                    viewModelScope?.launch {
                        withContext(Dispatchers.IO) {
                            saveGroupData(map, id, name, renameCount)
                        }
                    } ?: let {
                        GlobalScope.launch(Dispatchers.IO) {
                            saveGroupData(map, id, name, renameCount)
                        }
                    }
                }
            }
        }


        private fun saveGroupData(map: HashMap<String, GlobalSmallGroupBroadBean>, id: String?, name: String?, renameCount: Int?) {
            val bean = map[id]
            if (bean != null) {
                if (!TextUtils.isEmpty(name)) {
                    bean.name = name ?: ""
                }
                if (null != renameCount) {
                    bean.renameCount = renameCount
                }
                map[id ?: ""] = bean
            } else {
                val globalSmallGroupBean = GlobalSmallGroupBroadBean()
                globalSmallGroupBean.id = id ?: ""
                globalSmallGroupBean.name = name ?: ""
                globalSmallGroupBean.renameCount = renameCount
                map[id ?: ""] = globalSmallGroupBean
            }
            myGroupListData = map
            MMKV.defaultMMKV().encode(KEY_GROUP_LIST_DATA, toJson2(map))
        }

        fun saveGroupList(list: List<GlobalSmallGroupBroadBean>, viewModelScope: CoroutineScope? = null) {
            getGroupData(viewModelScope) { map ->
                viewModelScope?.launch {
                    withContext(Dispatchers.IO) {
                        list.forEach { item ->
                            if (!TextUtils.isEmpty(item.id)) {
                                map[item.id] = item
                            }
                        }
                        MMKV.defaultMMKV().encode(KEY_GROUP_LIST_DATA, toJson2(map))
                        GlobalBroadInfo.myGroupListData = map
                    }
                } ?: let {
                    GlobalScope.launch(Dispatchers.IO) {
                        list.forEach { item ->
                            if (!TextUtils.isEmpty(item.id)) {
                                map[item.id] = item
                            }
                        }
                        MMKV.defaultMMKV().encode(KEY_GROUP_LIST_DATA, toJson2(map))
                        GlobalBroadInfo.myGroupListData = map
                    }
                }
            }
        }

        fun getGroupListData(): HashMap<String, GlobalSmallGroupBroadBean> {
            val decodeString = MMKV.defaultMMKV().decodeString(KEY_GROUP_LIST_DATA)
            return if (decodeString?.isNotEmpty() == true) {
                val type = object : TypeToken<HashMap<String, GlobalSmallGroupBroadBean>>() {}
                return fromJsonTypeToken(decodeString, type)
            } else {
                HashMap()
            }
        }


        //保存压缩视频
        fun saveCompressionVideo(originalPath: String, compressionPath: String): HashMap<String, String> {
            val map = getSaveCompressionVideoData()
            map[originalPath] = compressionPath
            MMKV.defaultMMKV().encode(KEY_SAVE_COMPRESS_VIDEO, toJson2(map))
            return map
        }

        //获取压缩视频
        fun getSaveCompressionVideo(originalPath: String): String? {
            val map = getSaveCompressionVideoData()
            val comVideoPath = map[originalPath]
            return if (null != comVideoPath && FileUtils.isFileExists(comVideoPath)) {
                comVideoPath
            } else {
                null
            }
        }

        fun getSaveCompressionVideoData(): HashMap<String, String> {
            val decodeString = MMKV.defaultMMKV().decodeString(KEY_SAVE_COMPRESS_VIDEO)
            return if (decodeString?.isNotEmpty() == true) {
                val type = object : TypeToken<HashMap<String, String>>() {}
                return fromJsonTypeToken(decodeString, type)
            } else {
                HashMap()
            }
        }


        fun clearFriendListData() {
            val kv = MMKV.defaultMMKV()
            kv.remove(KEY_FRIEND_LIST_DATA)
        }


        fun getSqlStrData(key: String, defaultValue: String = ""): String {
            return try {
                getMmKv()?.decodeString(key) ?: defaultValue
            } catch (e: Exception) {
                e.toLog("MMKVBroadUtilsClass")
                ""
            }
        }

        fun saveSqlStrData(key: String, value: String? = null) {
            try {
                getMmKv()?.encode(key, value ?: "")
            } catch (e: Exception) {
                e.toLog("MMKVBroadUtilsClass")
            }

        }


        private var mkv: MMKV? = null
        private fun getMmKv(): MMKV? {
            return try {
                if (mkv != null) {
                    mkv
                } else {
                    mkv = MMKV.defaultMMKV()
                    mkv
                }
            } catch (e: Exception) {
                e.toLog("MMKVBroadUtilsClass")
                null
            }
        }
    }
}