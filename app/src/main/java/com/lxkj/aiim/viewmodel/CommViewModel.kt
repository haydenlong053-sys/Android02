package com.lxkj.aiim.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import cn.wildfirechat.message.HongBaoMessageContent
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.bean.BaseResponse
import com.cboda.baselibrary.bean.CommonDataListBean
import com.cboda.baselibrary.bean.CommonResultBean
import com.cboda.baselibrary.utils.MMKVUtils
import com.cboda.baselibrary.utils.MMKVUtils.Companion.saveUserData
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog
import com.lxkj.aiim.bean.*
import com.lxkj.aiim.holder.SendCustomMsg
import com.lxkj.aiim.lj.utils.showToastMain
import com.lxkj.aiim.utils.OKGoHelper
import org.json.JSONObject

class CommViewModel : FriendViewModel() {

    var roomCateLiveData = MutableLiveData<CommonDataListBean>()
    val refuseRedEnvelopesLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    var errorLiveData = MutableLiveData<ErrorBean>()
    val imUserLiveData = MutableLiveData<CommonResultBean>()
    var lingquListLiveData = MutableLiveData<CommonResultBean>()
    var hongbaoLiveData = MutableLiveData<CommonResultBean>()
    var zhuanzhangLiveData = MutableLiveData<CommonResultBean>()
    var yanzhengListLiveData = MutableLiveData<CommonDataListBean>()
    var qunUserListLiveData = MutableLiveData<CommonDataListBean>()
    var tousuleixingListLiveData = MutableLiveData<CommonDataListBean>()
    var chengyuanListLiveData = MutableLiveData<CommonDataListBean>()
    var guanliListLiveData = MutableLiveData<CommonDataListBean>()
    var jinyanListLiveData = MutableLiveData<CommonDataListBean>()
    var qunxinxiLiveData = MutableLiveData<CommonResultBean>()
    var gonggaoLiveData = MutableLiveData<CommonResultBean>()
    var xiaochengxuLiveData = MutableLiveData<CommonResultBean>()
    var haoyouListLiveData = MutableLiveData<CommonDataListBean>()
    var userLiveData = MutableLiveData<CommonResultBean>()
    var keFuData = MutableLiveData<CommonResultBean>()
    var pintaiLiveData = MutableLiveData<CommonResultBean>()
    var versionUpdateData = MutableLiveData<CommonResultBean>()
    var sousuoLiveData = MutableLiveData<CommonDataListBean>()
    var qunchengyuanListLiveData = MutableLiveData<CommonDataListBean>()
    var xitongaongaoListLiveData = MutableLiveData<CommonDataListBean>()


    private val okGoHelper = OKGoHelper()
    fun imUser(context: Context?, uid: String) {
        val map = mapOf(
            "id" to uid,
        )
        okGoHelper.post(
            context,
            "/duiInfo",
            JSONObject(map).toString(),
            CommonResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        saveUserData(bean.data)
                        imUserLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "qingkongSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "tianjiahaoyousuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "yichubenqunsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "shezhijinyantimesuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "shezhibeizhuSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "xiugaiqunmingSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "shanchusuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "zhidingsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "zhidingsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "miandaraosuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "setFanyisuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "jiaruheimingdansuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value =
                            ErrorBean(type = "fahongbaosuc", type2 = bean.id ?: "")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value =
                            ErrorBean(type = "fahongbaosuc", type2 = bean.id ?: "")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value =
                            ErrorBean(type = "zhuanzhangsuc", type2 = bean.id ?: "")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        lingquListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        yanzhengListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        hongbaoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        hongbaoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        zhuanzhangLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "shoukuanSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "tongyiSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "tongyiSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
                }
            },

            )
    }


    fun getQunUser(context: Context?, id: String) {

        val map = mapOf(
            "id" to id,
        )

        okGoHelper.post(
            context,
            "/groupUserList",
            JSONObject(map).toString(),
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        qunUserListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
                }
            },
        )
    }

    fun getxitonggaonggao(context: Context?, id: String) {

        val map = mapOf(
            "id" to id,
        )

        okGoHelper.post(
            context,
            "/noticeMsgList",
            JSONObject(map).toString(),
            CommonDataListBean(),
            showToast = false,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        xitongaongaoListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        tousuleixingListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "tousuSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
        name: String
    ) {

        val map = mapOf(
            "pageNo" to pageNo,
            "pageSize" to "10",
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        chengyuanListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        haoyouListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        sousuoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        bean.dataList?.forEachIndexed { index, commonBean ->
                            val groupInfo =
                                ChatManager.Instance().getGroupInfo(commonBean.id, false)
                            commonBean.icon = groupInfo?.portrait ?: ""
                        }
                        sousuoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "xiugaiqunxinxisuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "xiugaigonggaoSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "mingancisetsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "qingkongSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "qunichenguSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "jiesanqunsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "jiesanqunsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        guanliListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        qunchengyuanListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "yichuguanliSuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "zhuanrangqunsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonDataListBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonDataListBean) {
                        jinyanListLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        qunxinxiLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
                }
            },
        )
    }

    fun gonggaoxiangqing(context: Context?, qunid: String) {

        val map = mapOf(
            "id" to qunid,
        )

        okGoHelper.post(
            context,
            "/groupInfo",
            JSONObject(map).toString(),
            CommonResultBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        gonggaoLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        xiaochengxuLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showToast = false,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        userLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showToast = false,
            showLoading = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        saveUserData(bean.data)
                        keFuData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "sehnqingsuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        if (isGroup && !TextUtils.isEmpty(msgTarget) && !TextUtils.isEmpty(
                                sendUserId
                            )
                        ) {
                            SendCustomMsg().sendYeHuoLingQu(
                                msgTarget, "",
                                sendUserId,
                                sendUserName,
                                MMKVUtils.getRealUserId()!!, MMKVUtils.getUserName()!!, msgSender
                            )
                        }
                        errorLiveData.value = ErrorBean(type = "linghongbaosuc", isGroup = isGroup)
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "linghongbaosuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
                }
            },

            )
    }


    fun refuseRedEnvelopes(context: Context?, id: String, isGroup: Boolean, messageId: Long) {
        okGoHelper.post(
            context,
            "/redPacket/refuse",
            JSONObject(mapOf("id" to id)).toString(),
            CommonResultBean(),
            showLoading = true,
            showToast = true,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
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

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value =
                            ErrorBean(type = "linghongbaosuc", type2 = bean.isfin ?: "")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            CommonResultBean(),
            showLoading = false,
            showToast = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        pintaiLiveData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
                }
            },
        )
    }

    fun versionUpdate(context: Context?) {

        val map = mapOf(
            "id" to "",
        )

        okGoHelper.post(
            context,
            "/versionUpdate",
            JSONObject(map).toString(),
            CommonResultBean(),
            showLoading = false,
            showToast = false,
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is CommonResultBean) {
                        versionUpdateData.value = bean
                    }
                }

                override fun onFail(bean: BaseResponse?) {
                }
            },
        )
    }

    fun chuangjainqun(context: Context?, uid: String) {
        val map = mapOf(
            "uids" to uid,
        )
        okGoHelper.post(
            context,
            "/addGroup",
            JSONObject(map).toString(),
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value =
                            ErrorBean(type = "createGroupsuc", type2 = bean.groupId ?: "")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "xiugaiqunxinxisuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "zidongfanyisuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
            callBack = object : OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseResponse?) {
                    if (bean is NoResultBean) {
                        errorLiveData.value = ErrorBean(type = "caozuomembersuc")
                    }
                }

                override fun onFail(bean: BaseResponse?) {
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
            NoResultBean(),
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
            NoResultBean(),
            showLoading = false,
            showToast = false

        )
    }

}