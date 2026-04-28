package com.cboda.intelligent.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import cn.wildfirechat.message.HongBaoMessageContent
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import com.cboda.intelligent.bean.*
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.holder.SendCustomBroadMsg
import com.cboda.intelligent.utils.OKGoBroadHelper
import org.json.JSONObject

class CommViewBroadModel : FriendViewBroadModel() {

    var roomCateLiveData = MutableLiveData<CommonDataListBroadBean>()
    val refuseRedEnvelopesLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    var errorLiveData = MutableLiveData<ErrorBroadBean>()
    val imUserLiveData = MutableLiveData<CommonResultBroadBean>()
    var lingquListLiveData = MutableLiveData<CommonResultBroadBean>()
    var hongbaoLiveData = MutableLiveData<CommonResultBroadBean>()
    var zhuanzhangLiveData = MutableLiveData<CommonResultBroadBean>()
    var yanzhengListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var qunUserListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var tousuleixingListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var chengyuanListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var guanliListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var jinyanListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var qunxinxiLiveData = MutableLiveData<CommonResultBroadBean>()
    var gonggaoLiveData = MutableLiveData<CommonResultBroadBean>()
    var xiaochengxuLiveData = MutableLiveData<CommonResultBroadBean>()
    var haoyouListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var userLiveData = MutableLiveData<CommonResultBroadBean>()
    var keFuData = MutableLiveData<CommonResultBroadBean>()
    var pintaiLiveData = MutableLiveData<CommonResultBroadBean>()
    var versionUpdateData = MutableLiveData<CommonResultBroadBean>()
    var sousuoLiveData = MutableLiveData<CommonDataListBroadBean>()
    var qunchengyuanListLiveData = MutableLiveData<CommonDataListBroadBean>()
    var xitongaongaoListLiveData = MutableLiveData<CommonDataListBroadBean>()


    private val okGoHelper = OKGoBroadHelper()
    fun imUser(context: Context?, uid: String) {
        val map = mapOf(
            "id" to uid,
        )
        okGoHelper.post(
            context,
            "/duiInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
//                        saveUserData(bean.data)
                        RealmUtil.addUserData(bean.data)
                        imUserLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun qingkong(context: Context?, uid: String) {
        val map = mapOf(
            "uid" to uid,
        )
        okGoHelper.post(
            context,
            "/qingkong",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "qingkongSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun tianjiahaoyou(context: Context?, uid: String) {
        val map = mapOf(
            "uid" to uid,
        )
        okGoHelper.post(
            context,
            "/qingkong",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "tianjiahaoyousuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun yichubenqun(context: Context?, uid: String, qunid: String) {
        val map = mapOf(
            "uids" to uid,
            "id" to qunid,
        )
        okGoHelper.post(
            context,
            "/remove",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "yichubenqunsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun jinyanshichang(context: Context?, uid: String, qunid: String, type: String) {
        val map = mapOf(
            "uids" to uid,
            "id" to qunid,
            "type" to type
        )
        okGoHelper.post(
            context,
            "/jinyan",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "shezhijinyantimesuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun shezhibeizhu(context: Context?, uid: String, beizhu: String, characterTagsId: Int? = -1) {
        val map = if (characterTagsId == -1) {
            mapOf(
                "id" to uid,
                "bei" to beizhu,
            )
        } else {
            mapOf(
                "id" to uid,
                "bei" to beizhu,
                "characterTagsId" to characterTagsId
            )
        }
        okGoHelper.post(
            context,
            "/setBei",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "shezhibeizhuSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun xiugaiqunmingc(context: Context?, uid: String, beizhu: String) {
        val map = mapOf(
            "id" to uid,
            "name" to beizhu,
        )
        okGoHelper.post(
            context,
            "/editGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "xiugaiqunmingSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun shanchu(context: Context?, uid: String) {
        val map = mapOf(
            "uid" to uid,
        )
        okGoHelper.post(
            context,
            "/delFriend",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "shanchusuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun zhiding(context: Context?, uid: String, state: String) {
        val map = mapOf(
            "id" to uid,
            "type" to "1",
            "state" to state
        )
        okGoHelper.post(
            context,
            "/setZd",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "zhidingsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun zhidingqun(context: Context?, uid: String, state: String) {
        val map = mapOf(
            "id" to uid,
            "type" to state,
        )
        okGoHelper.post(
            context,
            "/setGTop",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "zhidingsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun miandarao(context: Context?, uid: String, state: String) {
        val map = mapOf(
            "type" to "2",
            "state" to state,
            "id" to uid,
        )
        okGoHelper.post(
            context,
            "/setZd",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "miandaraosuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun setFanyi(context: Context?, uid: String, state: String) {
        val map = mapOf(
            "state" to state,
            "id" to uid,
        )
        okGoHelper.post(
            context,
            "/setFanyi",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "setFanyisuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun jiaruheimingdan(context: Context?, uid: String, type: String) {
        val map = mapOf(
            "uid" to uid,
            "type" to type
        )
        okGoHelper.post(
            context,
            "/black",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "jiaruheimingdansuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun fahongbao(context: Context?, uid: String, price: String, con: String, payPwd: String) {
        val map = mapOf(
            "uid" to uid,
            "money" to price,
            "info" to con,
            "payPwd" to payPwd,
        )
        okGoHelper.post(
            context,
            "/sendHongbao",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value =
                            ErrorBroadBean(type = "fahongbaosuc", type2 = bean.id ?: "")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun faqunhongbao(
        context: Context?,
        id: String,
        type: String,
        money: String,
        count: String,
        uid: String,
        info: String,
        payPwd: String
    ) {
        val map = mapOf(
            "id" to id,
            "type" to type,
            "money" to money,
            "count" to count,
            "uid" to uid,
            "info" to info,
            "payPwd" to payPwd
        )
        okGoHelper.post(
            context,
            "/sendGroHb",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value =
                            ErrorBroadBean(type = "fahongbaosuc", type2 = bean.id ?: "")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun zhuanzhang(context: Context?, uid: String, price: String, con: String) {
        val map = mapOf(
            "uid" to uid,
            "money" to price,
            "payPwd" to con,
        )
        okGoHelper.post(
            context,
            "/zhuangZhang",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value =
                            ErrorBroadBean(type = "zhuanzhangsuc", type2 = bean.id ?: "")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun getLingquList(context: Context?, id: String, isGroup: Boolean) {

        val map = mapOf(
            "id" to id,
        )

        okGoHelper.post(
            context,
            if (isGroup) "/hbInfo" else "/dhbInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        lingquListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun getYanZhengList(context: Context?, pageNo: String, type: String, fromPage: String) {

        val map = mapOf(
            "pageNo" to pageNo,
            "pageSize" to "10",
            "key" to type,
        )

        okGoHelper.post(
            context,
            if (fromPage == "xindepengyou") "/newFriList" else "/newGroupList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        yanzhengListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun hongbaoInfo(context: Context?, hongbaoid: String) {

        val map = mapOf(
            "id" to hongbaoid,
        )

        okGoHelper.post(
            context,
            "/dhbInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        hongbaoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun qunhongbaoInfo(context: Context?, hongbaoid: String) {

        val map = mapOf(
            "id" to hongbaoid,
        )

        okGoHelper.post(
            context,
            "/hbInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        hongbaoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun zhuanzhangInfo(context: Context?, hongbaoid: String) {

        val map = mapOf(
            "id" to hongbaoid,
        )

        okGoHelper.post(
            context,
            "/zzInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        zhuanzhangLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun shoukuan(context: Context?, json: String) {
//        val map = mapOf(
//            "id" to uid,
//            "messageUid" to messageUid,
//            "payload" to payload,
//        )
        okGoHelper.post(
            context,
            "/getZhuanzhang",
            json,
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "shoukuanSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun tongyi(
        context: Context?,
        uid: String,
        fromPage: String,
        remarkName: String = "",
        tagsId: Int = -1
    ) {
        val map = if (tagsId == -1) {
            mapOf(
                "id" to uid,
                "state" to "2",
                "remarkName" to remarkName,
            )
        } else {
            mapOf(
                "id" to uid,
                "state" to "2",
                "remarkName" to remarkName,
                "characterTagsId" to tagsId,
            )
        }
        okGoHelper.post(
            context,
            if (fromPage == "xindepengyou") "/friExamine" else "/groupExamine",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "tongyiSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun jujue(context: Context?, uid: String, fromPage: String) {
        val map = mapOf(
            "id" to uid,
            "state" to "3",
        )
        okGoHelper.post(
            context,
            if (fromPage == "xindepengyou") "/friExamine" else "/groupExamine",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "tongyiSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    var qunUserListPageLiveData = MutableLiveData<CommonDataListBroadBean>()
    var qunUserListPageLiveData2 = MutableLiveData<CommonDataListBroadBean>()

    fun getQunUserPage(context: Context?, id: String, showLoading: Boolean = false) {

        val map = mapOf(
            "id" to id,
            "pageNo" to "1",
            "pageSize" to "500"
        )

        okGoHelper.post(
            context,
            "/groupUserListPage",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = showLoading,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        qunUserListPageLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun getQunUserPage2(context: Context?, id: String, pageNo: Int, pageSize: Int) {

        val map = mapOf(
            "id" to id,
            "pageNo" to pageNo,
            "pageSize" to pageSize
        )

        okGoHelper.post(
            context,
            "/groupUserListPage",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = (pageNo == 1),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        qunUserListPageLiveData2.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

//    fun getQunUser(context: Context?, id: String, showLoading: Boolean = false) {
//
//        val map = mapOf(
//            "id" to id,
//        )
//
//        okGoHelper.post(
//            context,
//            "/groupUserList",
//            JSONObject(map).toString(),
//            CommonDataListBroadBean(),
//            showToast = true,
//            showLoading = showLoading,
//            callBack = object : OKGoBroadHelper.RequestCallBack {
//                override fun onSuccess(bean: BaseBroadResponse?) {
//                    if (bean is CommonDataListBroadBean) {
//                        qunUserListLiveData.value = bean
//                    }
//                }
//
//                override fun onFail(bean: BaseBroadResponse?) {
//                }
//            },
//        )
//    }

    fun getxitonggaonggao(context: Context?, id: String) {

        val map = mapOf(
            "id" to id,
        )

        okGoHelper.post(
            context,
            "/noticeMsgList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = false,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        xitongaongaoListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun tousuleixing(context: Context?, pageNo: String, type: String) {

        val map = mapOf(
            "pageNo" to pageNo,
            "pageSize" to "10",
            "ge_gh" to "2",
            "type" to type,
        )

        okGoHelper.post(
            context,
            "/tousuReason",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        tousuleixingListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun tousu(
        context: Context?,
        qunid: String,
        leixing: String,
        zhengju: String,
        tupian: String,
        wenzi: String
    ) {
        val map = mapOf(
            "id" to qunid,
            "type" to leixing,
            "log" to zhengju,
            "imgs" to tupian,
            "info" to wenzi,

            )
        okGoHelper.post(
            context,
            "/tousuGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "tousuSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun getQunMemberList(
        context: Context?,
        pageNo: String,
        url: String,
        id: String,
        type: String,
        name: String,
        pageSize: Int = CommonBroadConfig.pageSize_100
    ) {

        val map = mapOf(
            "pageNo" to pageNo,
            "pageSize" to pageSize,
            "id" to id,
            "type" to type,
            "groupId" to id,
            "name" to name,
            "key" to name,
        )

        okGoHelper.post(
            context,
            url,
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        chengyuanListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }


    fun gethaoyoulist(context: Context?, key: String) {

        val map = mapOf(
            "pageNo" to "1",
            "pageSize" to "100",
            "key" to key,
        )

        okGoHelper.post(
            context,
            "/friendList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        haoyouListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun sousuoUser(context: Context?, key: String) {

        val map = mapOf(
            "pageNo" to "1",
            "pageSize" to "100",
            "key" to key,
        )

        okGoHelper.post(
            context,
            "/serchList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        sousuoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun qunList(context: Context?, key: String) {

        val map = mapOf(
            "pageNo" to "1",
            "pageSize" to "100",
            "key" to key,
        )

        okGoHelper.post(
            context,
            "/groupList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        bean.dataList?.forEachIndexed { index, commonBean ->
                            val groupInfo =
                                ChatManager.Instance().getGroupInfo(commonBean.id, false)
                            commonBean.icon = groupInfo?.portrait ?: ""
                        }
                        sousuoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun caozuochengyuan(context: Context?, qunid: String) {
        val map = mapOf(
            "qunid" to qunid,
        )
        okGoHelper.post(
            context,
            "/qingkong",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun shangchuanquntouxiang(context: Context?, qunid: String, url: String) {
        val map = mapOf(
            "id" to qunid,
            "icon" to url,
        )
        okGoHelper.post(
            context,
            "/editGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "xiugaiqunxinxisuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun xiugaigonggao(context: Context?, uid: String, content: String) {
        val map = mapOf(
            "id" to uid,
            "gonggao" to content,
        )
        okGoHelper.post(
            context,
            "/editGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "xiugaigonggaoSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun mingancishezhi(context: Context?, id: String, content: String) {
        val map = mapOf(
            "id" to id,
            "mgc" to content,
        )
        okGoHelper.post(
            context,
            "/editGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "mingancisetsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun qingkongQun(context: Context?, uid: String) {
        val map = mapOf(
            "uid" to uid,
        )
        okGoHelper.post(
            context,
            "/qingkong",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "qingkongSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun shezhiqunnicheng(context: Context?, uid: String, beizhu: String) {
        val map = mapOf(
            "id" to uid,
            "name" to beizhu,
        )
        okGoHelper.post(
            context,
            "/editGnickname",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "qunichenguSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun destoryGroup(context: Context?, uid: String) {
        val map = mapOf(
            "id" to uid,
        )
        okGoHelper.post(
            context,
            "/destoryGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        // 本地移除该群会话，触发 OnRemoveConversationListener，会话列表会刷新
                        val conversation = Conversation().apply {
                            type = Conversation.ConversationType.Group
                            line = 0
                            target = uid
                        }
                        ChatManager.Instance().removeConversation(conversation, true)
                        errorLiveData.value = ErrorBroadBean(type = "jiesanqunsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun outGroup(context: Context?, uid: String) {
        val map = mapOf(
            "id" to uid,
        )
        okGoHelper.post(
            context,
            "/outGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "jiesanqunsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun qunGuanliList(context: Context?, qunid: String) {

        val map = mapOf(
            "id" to qunid,
            "type" to "2",
        )

        okGoHelper.post(
            context,
            "/groupUserList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        guanliListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun qunGuanMemberliList(context: Context?, qunid: String) {

        val map = mapOf(
            "id" to qunid,
            "type" to "3",
        )

        okGoHelper.post(
            context,
            "/groupUserList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        qunchengyuanListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun yichuguanli(context: Context?, uid: String, qunid: String, type: String) {
        val map = mapOf(
            "uids" to uid,
            "id" to qunid,
            "type" to type
        )
        okGoHelper.post(
            context,
            "/addManag",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "yichuguanliSuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun zhuanrang(context: Context?, uid: String, qunid: String) {
        val map = mapOf(
            "uid" to uid,
            "id" to qunid,
        )
        okGoHelper.post(
            context,
            "/turnQz",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "zhuanrangqunsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }


    fun jinyanList(context: Context?, qunid: String, name: String) {

        val map = mapOf(
            "id" to qunid,
            "name" to name,
        )

        okGoHelper.post(
            context,
            "/jinyanUserList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
                        jinyanListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun qunxinxi(context: Context?, hongbaoid: String) {

        val map = mapOf(
            "type" to hongbaoid,
        )

        okGoHelper.post(
            context,
            "/hongbaoInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        qunxinxiLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun gonggaoxiangqing(context: Context?, qunid: String, showLoading: Boolean = false) {
        val map = mapOf(
            "id" to qunid,
        )
        okGoHelper.post(
            context,
            "/groupInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = true,
            showLoading = showLoading,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        gonggaoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun getxiaochengxu(context: Context?, code: String) {

        val map = mapOf(
            "code" to code,
        )

        okGoHelper.post(
            context,
            "/changeXcx",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        xiaochengxuLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun getUserInfo(context: Context?, code: String) {

        val map = mapOf(
            "code" to code,
        )

        okGoHelper.post(
            context,
            "/userInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = false,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        userLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }


    fun getKfInfo(context: Context?, id: String) {
        val map = mapOf(
            "id" to id,
        )
        okGoHelper.post(
            context,
            "/duiInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showToast = false,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
//                        saveUserData(bean.data)
                        RealmUtil.addUserData(bean.data)
                        keFuData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun fasonghaoyoushenqing(
        context: Context?,
        uid: String,
        name: String,
        beizhu: String,
        tagId: String,
        source: String,
        qunid: String
    ) {
        val map = mapOf(
            "id" to uid,
            "sinfo" to name,
            "bei" to beizhu,
            "info" to "",
            "characterTagsId" to tagId,
            "source" to source,
            "groupId" to qunid,
        )
        okGoHelper.post(
            context,
            "/addFriend",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "sehnqingsuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun linghongbao2(
        context: Context?,
        json: String,
        isGroup: Boolean,
        msgTarget: String,
        sendUserId: String,
        sendUserName: String,
        messageId: Long,
        msgSender: String
    ) {
        okGoHelper.post(
            context,
            if (isGroup) "/getGroHb" else "/getHongbao",
            json,
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        if (isGroup && !TextUtils.isEmpty(msgTarget) && !TextUtils.isEmpty(
                                sendUserId
                            )
                        ) {
                            SendCustomBroadMsg().sendMyImLingQu(
                                msgTarget, "",
                                sendUserId,
                                sendUserName,
                                MMKVBroadUtils.getRealUserId()!!, MMKVBroadUtils.getUserName()!!, msgSender
                            )
                        }
                        errorLiveData.value = ErrorBroadBean(type = "linghongbaosuc", isGroup = isGroup)
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                    if (bean?.code == "200") {
                        try {
                            val msg = ChatManager.Instance().getMessage(messageId)
                            val hongBaoMessageContent = msg.content as HongBaoMessageContent
                            hongBaoMessageContent.lingquStatus = "3"
                            ChatManager.Instance().updateMessage(messageId, hongBaoMessageContent)
                            eLogD("refuseRedEnvelopesClass", "updateMessage")
                        } catch (e: Exception) {
                            e.toLog("refuseRedEnvelopesClass")
                        }
                    }
                }
            },

            )
    }

    fun linghongbao(context: Context?, json: String) {
//        val map = mapOf(
//            "id" to uid,
//        )
        okGoHelper.post(
            context,
            "/getHongbao",
            json,
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "linghongbaosuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun refuseRedEnvelopes(context: Context?, id: String, isGroup: Boolean, messageId: Long) {
        okGoHelper.post(
            context,
            "/redPacket/refuse",
            JSONObject(mapOf("id" to id)).toString(),
            CommonResultBroadBean(),
            showLoading = true,
            showToast = true,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
//
                    try {
                        val msg = ChatManager.Instance().getMessage(messageId)
                        val hongBaoMessageContent = msg.content as HongBaoMessageContent
                        hongBaoMessageContent.lingquStatus = "2"
//                        eLogD("refuseRedEnvelopesClass","messageId:$messageId")
//                        eLogD("refuseRedEnvelopesClass","msgContent:${toJson2(msg.content)}")
//                        eLogD("refuseRedEnvelopesClass","hongBaoMessageContent:${toJson2(hongBaoMessageContent)}")
                        ChatManager.Instance().updateMessage(messageId, hongBaoMessageContent)
                    } catch (e: Exception) {
                        e.toLog("refuseRedEnvelopesClass")
                    }

                    refuseRedEnvelopesLiveData.value = Pair(isGroup, true)
                }

                override fun onFail(bean: BaseBroadResponse?) {
                    eLogD("refuseRedEnvelopesClass", "code:${bean?.code}")
                    if (bean?.code == "500") {
                        try {
                            val msg = ChatManager.Instance().getMessage(messageId)
                            val hongBaoMessageContent = msg.content as HongBaoMessageContent
                            hongBaoMessageContent.lingquStatus = "3"
                            ChatManager.Instance().updateMessage(messageId, hongBaoMessageContent)
                            eLogD("refuseRedEnvelopesClass", "updateMessage")
                        } catch (e: Exception) {
                            e.toLog("refuseRedEnvelopesClass")
                        }
                    }

                }
            },
        )
    }

    fun lingqunhongbao(context: Context?, json: String) {
//        val map = mapOf(
//            "id" to uid,
//        )
        okGoHelper.post(
            context,
            "/getGroHb",
            json,
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value =
                            ErrorBroadBean(type = "linghongbaosuc", type2 = bean.isfin ?: "")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun getconfig(context: Context?) {

        val map = mapOf(
            "id" to "",
        )

        okGoHelper.post(
            context,
            "/ptConfig",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            showLoading = false,
            showToast = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        pintaiLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

//    fun versionUpdate(context: Context?) {
//        val map = mapOf(
//            "id" to "",
//        )
//        okGoHelper.post(
//            context,
//            "/versionUpdate",
//            JSONObject(map).toString(),
//            CommonResultBroadBean(),
//            showLoading = false,
//            showToast = false,
//            callBack = object : OKGoBroadHelper.RequestCallBack {
//                override fun onSuccess(bean: BaseBroadResponse?) {
//                    if (bean is CommonResultBroadBean) {
//                        versionUpdateData.value = bean
//                    }
//                }
//
//                override fun onFail(bean: BaseBroadResponse?) {
//                }
//            },
//        )
//    }

    fun chuangjainqun(context: Context?, uid: String) {
        val map = mapOf(
            "uids" to uid,
        )
        okGoHelper.post(
            context,
            "/addGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value =
                            ErrorBroadBean(type = "createGroupsuc", type2 = bean.groupId ?: "")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun larenjinqun(
        context: Context?,
        qunid: String,
        uids: String,
    ) {
        val map = mapOf(
            "id" to qunid,
            "uids" to uids,

            )
        okGoHelper.post(
            context,
            "/addGroupUser",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun rechuqun(
        context: Context?,
        qunid: String,
        uids: String,
    ) {
        val map = mapOf(
            "id" to qunid,
            "uids" to uids,

            )
        okGoHelper.post(
            context,
            "/remove",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun xiugaiqunxinxi(context: Context?, uid: String, key: String, va: String) {
        val map = mapOf(
            "id" to uid,
            key to va
        )
        okGoHelper.post(
            context,
            "/editGroup",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "xiugaiqunxinxisuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun setGroupFanyi(context: Context?, qid: String, state: String) {
        val map = mapOf(
            "id" to qid,
            "state" to state
        )
        okGoHelper.post(
            context,
            "/setGroupFanyi",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "zidongfanyisuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }

    fun jeichujinyan(context: Context?, id: String, uid: String) {
        val map = mapOf(
            "id" to id,
            "uid" to uid
        )
        okGoHelper.post(
            context,
            "/delJinyan",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        errorLiveData.value = ErrorBroadBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },

            )
    }


    fun yidugonggao(context: Context?, id: String) {
        val map = mapOf(
            "id" to id,
        )
        okGoHelper.post(
            context,
            "/msgYidu",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            showLoading = false,
            showToast = false

        )
    }

    fun shanchugonggao(context: Context?, id: String) {
        val map = mapOf(
            "id" to id,
        )
        okGoHelper.post(
            context,
            "/msgDel",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            showLoading = false,
            showToast = false

        )
    }

}