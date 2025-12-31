package cn.wildfire.chat.kit.utils

import com.cboda.baselibrary.bean.CommonDataListBean
import com.cboda.baselibrary.common.UrlConstants
import com.cboda.baselibrary.network.NetworkUtils
import com.cboda.baselibrary.network.NetworkUtils.postEncryptedList
import com.cboda.baselibrary.utils.MMKVUtils.Companion.getRealUserId
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object E {
    fun getDetailUserData(
        groupId: String?,
        callBack: (bean: CommonDataListBean.CommonBean?) -> Unit
    ) {
        val map = mapOf("id" to groupId)
        postEncryptedList(
            UrlConstants.GROUP_USER_LIST,
            map,
            object : NetworkUtils.ListCallback<CommonDataListBean.CommonBean> {
                override fun onError(throwable: Throwable) {
                    callBack.invoke(null)
                }

                override fun onFailure(code: Int, message: String) {
                    callBack.invoke(null)
                }

                override fun onSuccess(
                    list: List<CommonDataListBean.CommonBean>?,
                    totalCount: Int,
                    totalPage: Int
                ) {
                    list?.also {
                        val gson = GsonUtils.getGson()
                        val jsonStr = gson.toJson(list)
                        val userList = gson.fromJson<MutableList<CommonDataListBean.CommonBean>?>(
                            jsonStr,
                            object :
                                TypeToken<MutableList<CommonDataListBean.CommonBean>?>() {}.type
                        )
                        userList?.also {
                            var commonBean: CommonDataListBean.CommonBean? = null
                            for (bean in userList) {
                                if (bean.userId == getRealUserId()) {
                                    commonBean = bean
                                    break
                                }
                            }
                            callBack.invoke(commonBean)
                        } ?: also {
                            callBack.invoke(null)
                        }
                    } ?: also {
                        callBack.invoke(null)
                    }
                }
            })
    }
}