package com.base.commonlibrary.utils

import android.content.Context
import android.text.TextUtils
import com.base.commonlibrary.bean.CommonResultBean
import com.base.commonlibrary.bean.Constant
import com.base.commonlibrary.bean.GlobalFriendBean
import com.base.commonlibrary.bean.GlobalInfo
import com.base.commonlibrary.bean.GlobalSmallGroupBean
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

/**
 * 简述： mmkv本地存储
 */
class MMKVUtils {
    companion object {
        const val NET_TYPE = "appNetType"
        const val DEV_INPUT_BASE_URL = "devInputBaseUrl"
        const val SEARCH_KEY = "searchHistoryKey"
        const val USER_ID_KEY = "userIdKey"
        const val USER_NAME_KEY = "username"
        const val USER_PHONE_KEY = "userPhoneKey"
        const val USER_ONE_KEY_LOGIN_PHONE_KEY = "userOneKeyLoginPhoneKey"
        const val USER_ONE_KEY_LOGIN_TYPE_KEY = "userOneKeyLoginTypeKey"
        const val REAL_USER_ID_KEY = "realUserIdKey"
        const val TX_USER_SIGN = "txUserSign"
        const val APP_PRIVACYREADY = "app_privacyready"

        const val YE_HUO_USER_TOKEN = "yehuoUserToken"
        const val USER_YU_YAN_KEY = "yuyan"
        const val USER_ZHUTI_KEY = "zhuti"
        const val USER_FANYI_KEY = "fanyi"
        const val USER_ZIDONG_FANYI_KEY = "zidongfanyi"
        const val KEFU_ID_KEY = "kefuIdKey"
        const val KEFU_NAME_KEY = "kefuNameKey"
        const val KEY_FRIEND_LIST_DATA = "key_friend_list_data"

        const val KEY_GROUP_LIST_DATA = "key_group_list_data"

        fun saveSearchHistory(searchHistory: String?) {
            val kv = MMKV.defaultMMKV()
            kv.encode(SEARCH_KEY + Constant.getUid(), searchHistory)
        }

        fun getSearchHistory(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(SEARCH_KEY + Constant.getUid())
        }

        fun clearHistory() {
            val kv = MMKV.defaultMMKV()
            kv.remove(SEARCH_KEY + Constant.getUid())
        }

        fun saveUserId(userId: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_ID_KEY, userId)
        }

        fun saveUserName(userName: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(USER_NAME_KEY, userName)
        }

        fun getUserName(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(USER_NAME_KEY)
        }


        fun getUserId(): String? {
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

        fun saveKeFuName(name: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(KEFU_NAME_KEY, name)
        }

        fun getKeFuName(): String {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(KEFU_NAME_KEY) ?: "X-Bao客服"
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

        fun saveYeHuoToken(txUserSign: String) {
            val kv = MMKV.defaultMMKV()
            kv.encode(YE_HUO_USER_TOKEN, txUserSign)
        }

        fun getYeHuoToken(): String? {
            val kv = MMKV.defaultMMKV()
            return kv.decodeString(YE_HUO_USER_TOKEN)
        }

        fun clearYeHuoToken() {
            val kv = MMKV.defaultMMKV()
            kv.remove(YE_HUO_USER_TOKEN)
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
            return kv.decodeString(USER_YU_YAN_KEY)
        }

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

        fun saveUserData(data: GlobalFriendBean) {
            val list = arrayListOf<GlobalFriendBean>()
            list.add(data)
            saveUserListData(list)
        }

        fun saveUserData(data: CommonResultBean.CommonResult?) {
            data?.also {
                val bean = GlobalFriendBean()
                bean.userId = data.id
                bean.nickName = data.name
                bean.remarkName = data.remarkName
                data.registerDayBean?.registDay?.also { bean.registerDay = it }
                bean.grade = data.grade
                bean.icon = data.icon
                saveUserData(bean)
            }
        }

        fun saveUserListData(list: List<GlobalFriendBean>): HashMap<String, GlobalFriendBean> {
            val map = getFriendListData()
            list.forEach { item ->
                if (!TextUtils.isEmpty(item.userId)) {
                    val bean = GlobalFriendBean()
                    bean.registerDay = item.registerDay
                    bean.grade = item.grade
                    bean.icon = item.icon
                    bean.nickName = item.nickName
                    bean.remarkName = item.remarkName
                    map[item.userId ?: ""] = bean
                }
                MMKV.defaultMMKV().encode(KEY_FRIEND_LIST_DATA, toJson2(map))
            }
            return map
        }

        fun getFriendListData(): HashMap<String, GlobalFriendBean> {
            val decodeString = MMKV.defaultMMKV().decodeString(KEY_FRIEND_LIST_DATA)
            return if (decodeString?.isNotEmpty() == true) {
                val type = object : TypeToken<HashMap<String, GlobalFriendBean>>() {}
                return fromJsonTypeToken(decodeString, type)
            } else {
                HashMap()
            }
        }

        fun updateGroupNameAndRenameCount(id: String?, name: String?, renameCount: Int?) {
            if (!TextUtils.isEmpty(id) && (!TextUtils.isEmpty(name) || null != renameCount)) {
                val map = getGroupListData()
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
                    val globalSmallGroupBean = GlobalSmallGroupBean()
                    globalSmallGroupBean.id = id ?: ""
                    globalSmallGroupBean.name = name ?: ""
                    globalSmallGroupBean.renameCount = renameCount
                    map[id ?: ""] = globalSmallGroupBean
                }
                GlobalInfo.myGroupListData = map
                MMKV.defaultMMKV().encode(KEY_GROUP_LIST_DATA, toJson2(map))
            }
        }

        fun saveGroupList(list: List<GlobalSmallGroupBean>): HashMap<String, GlobalSmallGroupBean> {
            val map = getGroupListData()
            list.forEach { item ->
                if (!TextUtils.isEmpty(item.id)) {
                    map[item.id] = item
                }
                MMKV.defaultMMKV().encode(KEY_GROUP_LIST_DATA, toJson2(map))
            }
            return map
        }

        fun getGroupListData(): HashMap<String, GlobalSmallGroupBean> {
            val decodeString = MMKV.defaultMMKV().decodeString(KEY_GROUP_LIST_DATA)
            return if (decodeString?.isNotEmpty() == true) {
                val type = object : TypeToken<HashMap<String, GlobalSmallGroupBean>>() {}
                return fromJsonTypeToken(decodeString, type)
            } else {
                HashMap()
            }
        }

        fun clearFriendListData() {
            val kv = MMKV.defaultMMKV()
            kv.remove(KEY_FRIEND_LIST_DATA)
        }

    }
}