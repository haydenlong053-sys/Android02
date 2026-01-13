package com.cboda.intelligent.holder

import android.content.Context
import android.content.Intent
import com.cboda.chatui.bridge.MyCommonBroadBridge
import com.cboda.chatui.bridge.MyBroadEvent
import com.cboda.chatui.conversation.message.model.UiBroadMessage
import cn.wildfirechat.message.HongBaoMessageContent
import cn.wildfirechat.message.Message
import cn.wildfirechat.message.TextMessageContent
import cn.wildfirechat.message.VideoMessageContent
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.event.FanYiBroadEvent
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.getFanYi
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.saveUserData
import com.cboda.baselibrary.utils.eLogD
import com.google.gson.Gson
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.chatemoji.EmotionBroadEvent
import com.cboda.intelligent.MainBroadActivity
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.MeetingDetailBroadBean
import com.cboda.intelligent.bean.NoResultBroadBean
import com.cboda.intelligent.activity.VideoPlayOnlyBroadActivity
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.utils.StringBroadUtil
import com.cboda.intelligent.utils.*
import com.cboda.intelligent.utils.AppBroadUtils.Companion.exit
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class MyImManagerBroadHolder(private val context: Context?) {
    init {
        setBridge()
    }

    private var okGoHelper = OKGoBroadHelper()

    private fun setBridge() {
        MyCommonBroadBridge.getInstance()
            ?.setMyImCommonListener(object : MyCommonBroadBridge.MyImCommonListener {
                override fun goBridgeMethod(
                    mContext: Context?,
                    type: String?,
                    params: String?,
                    params2: String?,
                    msg: UiBroadMessage?
                ) {
                    eLogD("野火桥接", "---> $type  $params  $params2")
                    when (type) {
                        "cjxlt" -> {
                            JumpBroadUtils.startCreateQunActivity(mContext)
                        }

                        "jinyaninfo" -> {
                            if (params2 == "0") {//单聊
                                duiInfo(params ?: "")
                            } else {//群聊
                                groupInfo(params ?: "")
                            }
                        }

                        "danliaogengduo" -> {
                            JumpBroadUtils.startHaoYouAddActivity(mContext, params ?: "", "2")
                        }

                        "qunliaozhuye" -> {
                            JumpBroadUtils.startQunInfoActivity(mContext, params ?: "")
                        }


                        "getUserData" -> {
                            val map = mapOf(
                                "id" to params,
                            )
                            okGoHelper.post(
                                context,
                                "/duiInfo",
                                JSONObject(map).toString(),
                                CommonResultBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                        if (bean is CommonResultBroadBean) {
                                            saveUserData(bean.data)
                                        }
                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                    }
                                },
                            )
                        }

                        "tianjiahaoyou" -> {
                            val map = mapOf(
                                "id" to params,
                            )
                            okGoHelper.post(
                                context,
                                "/duiInfo",
                                JSONObject(map).toString(),
                                CommonResultBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                        if (bean is CommonResultBroadBean) {
                                            saveUserData(bean.data)
                                            if (bean.data?.isFri == "1") {
                                                JumpBroadUtils.startHaoYouAddActivity(
                                                    mContext,
                                                    params ?: "", "2"
                                                )
                                            } else {
                                                JumpBroadUtils.startHaoYouAddActivity(
                                                    mContext,
                                                    params ?: "",
                                                    "2"
                                                )
                                            }
                                        }
                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                    }
                                },
                            )
                        }

                        "weiduxiaoxitianjiahaoyou" -> {
                            val map = mapOf(
                                "id" to params,
                            )
                            okGoHelper.post(
                                context,
                                "/duiInfo",
                                JSONObject(map).toString(),
                                CommonResultBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                        if (bean is CommonResultBroadBean) {
                                            saveUserData(bean.data)
                                            if (bean.data?.isFri == "1") {
                                                JumpBroadUtils.startHaoYouAddActivity(
                                                    mContext,
                                                    params ?: "","1"
                                                )
                                            } else {
                                                JumpBroadUtils.startHaoYouAddActivity(
                                                    mContext,
                                                    params ?: "",
                                                    "1"
                                                )
                                            }
                                        }
                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                    }
                                },
                            )
                        }

                        "mingpian" -> {
                            if (params2?.isNotEmpty() == true) {
                                val map = mapOf(
                                    "id" to (params2),
                                )
                                okGoHelper.post(
                                    context,
                                    "/groupInfo",
                                    JSONObject(map).toString(),
                                    CommonResultBroadBean(),
                                    callBack = object : OKGoBroadHelper.RequestCallBack {
                                        override fun onSuccess(bean: BaseBroadResponse?) {
                                            if (bean is CommonResultBroadBean) {

                                                if (bean.data?.userType == "3" && bean.data?.openQzts == "1") {
                                                    ToastBroadUtils.show(mContext?.getString(R.string.qunzhutuisongmingp))
                                                } else {
                                                    JumpBroadUtils.startHaoyouListActivity(
                                                        mContext,
                                                        params ?: "",
                                                        params2 ?: ""
                                                    )
                                                }
                                            }
                                        }

                                        override fun onFail(bean: BaseBroadResponse?) {
                                        }
                                    },
                                )
                            } else {
                                JumpBroadUtils.startHaoyouListActivity(
                                    mContext,
                                    params ?: "",
                                    params2 ?: ""
                                )

                            }
                        }

                        "zhuanzhang" -> {
                            JumpBroadUtils.startZhuanzhangActivity(mContext, params ?: "")
                        }

                        "hongbao" -> {
                            JumpBroadUtils.startFaHongBaoActivity(mContext, params ?: "")
                        }

                        "qunhongbao" -> {
                            JumpBroadUtils.startFaQunHongBaoActivity(mContext, params ?: "")
                        }

                        "bianji" -> {
                            val holder = BianJiTextBroadHolder(
                                mContext,
                                (msg?.message?.content as TextMessageContent).content
                            )
                            holder.callBack = object : BianJiTextBroadHolder.OnConfimCallBack {
                                override fun onClickConfim(txt: String) {
                                    val msgContent = (msg.message.content as TextMessageContent)
                                    msgContent.content = txt
                                    ChatManager.Instance()
                                        .updateRemoteMessageContent(
                                            msg.message.messageUid,
                                            msgContent,
                                            true,
                                            true,
                                            object : GeneralCallback {
                                                override fun onSuccess() {
                                                    AppBroadUtils.debug(
                                                        "野火",
                                                        "更新文本消息-->onSuccess"
                                                    )
                                                    EventBus.getDefault()
                                                        .post(MyBroadEvent("reloadMsg", "", ""))
                                                }

                                                override fun onFail(errorCode: Int) {
                                                    AppBroadUtils.debug(
                                                        "野火",
                                                        "更新文本消息-->onFail:: $errorCode"
                                                    )
                                                }
                                            })

                                }
                            }
                            holder.show()
                        }

                        "tianjiadonghuabiaoqing" -> {
                            addEmoji(mContext, params ?: "")
                        }

                        "shoucang" -> {
                            val paraList = params2?.split("|")
                            if (paraList?.size == 3) {
                                shoucang(
                                    mContext,
                                    params ?: "",
                                    paraList!![0],
                                    paraList[1],
                                    paraList[2]
                                )
                            } else {
                                shoucang(mContext, params ?: "", paraList!![0], paraList[1])
                            }
                        }

                        "fanyi" -> {
                            if (SharePrefBroadBroadUtil.getString(mContext, "isvip", "0").equals("1")) {
                                fanyi(
                                    mContext,
                                    getFanYi().toString(),
                                    params!!,
                                    msg!!.message
                                )
                            }
                        }

                        "lingzhuanzhang" -> {
                            val paraList = params?.split("|")
                            paraList?.apply {
                                JumpBroadUtils.startZhuanZhangPageActivity(
                                    mContext,
                                    this[0],
                                    this[1],
                                    params2 ?: ""
                                )
                            }
                        }

                        "jieshoulingzhuanzhang" -> {
                            val paraList = params?.split("|")
                            paraList?.apply {
                                JumpBroadUtils.startZhuanZhangPageActivity(
                                    mContext,
                                    this[0],
                                    this[1],
                                    params2 ?: ""
                                )
                            }
                        }

                        "linghongbao" -> {
                            val paraList = params?.split("|")
                            paraList?.apply {
                                if (this.size > 3) {
                                    if (this[1] != "3") {
                                        hbInfo(
                                            mContext,
                                            this[0],
                                            this[2],
                                            this[3],
                                            msg!!,
                                            this[1],
                                            false
                                        )
                                    }
                                }
                            }

                        }

                        "lingqunhongbao" -> {
                            val paraList = params?.split("|")
                            paraList?.apply {
                                if (this.size > 3) {
                                    if (this[1] != "3") {
                                        hbInfo(
                                            mContext,
                                            this[0],
                                            this[2],
                                            this[3],
                                            msg!!,
                                            this[1],
                                            true
                                        )
                                    }
                                }
                            }
                        }

                        "xindepengyou" -> {
                            JumpBroadUtils.startYanZhengActivity(mContext, "xindepengyou")
                        }

                        "ruqunyaoqing" -> {
                            JumpBroadUtils.startYanZhengActivity(mContext, "ruqunyaoqing")
                        }

                        "qunliaoliebiao" -> {
                            JumpBroadUtils.startQunListActivity(mContext)
                        }

                        "beifeng" -> {
                            ToastBroadUtils.show("您已被封禁，请重新登录")
                            exit(mContext)
                        }

                        "huoququnMeeting" -> {
                            val map = mapOf(
                                "groupId" to (params ?: ""),
                            )
//                            okGoHelper.postNoAes(context, UrlBroadBroadConstants.HOST+"/"+ UrlBroadBroadConstants.GET_GROUP_MEETING,
//                                JSONObject(map).toString(),
//                                GroupMeetingBroadBean(),
//                                callBack = object : OKGoBroadHelper.RequestCallBack{
//                                    override fun onSuccess(bean: BaseBroadResponse?) {
//                                         ( bean as? GroupMeetingBroadBean)?.let {
//                                             it.data?.activeMeetings?.let {
//                                                 if (it.isNotEmpty()){
//                                                     it.findLast {
//                                                         it?.status=="active"
//                                                     }?.let {
//                                                         EventBus.getDefault().post(
//                                                             MyBroadEvent(
//                                                                 "qunMeeting",
//                                                                 it.roomId,
//                                                                 QuickJsonBroadUtils.build(
//                                                                     "title" to (it.payload?.roomName?:"会议室"),
//                                                                     "ownerId" to it.ownerId
//                                                                 ).toString()
//                                                             )
//                                                         )
//                                                     }
//
//                                                 }else{
//                                                     EventBus.getDefault().post(
//                                                         MyBroadEvent(
//                                                             "qunMeeting",
//                                                             "",
//                                                             ""
//                                                         )
//                                                     )
//
//                                                 }
//                                             }
//
////
//
//                                        }
//
//                                    }
//
//                                    override fun onFail(bean: BaseBroadResponse?) {
//                                    }
//
//                                })
                            okGoHelper.postNoAes(
                                context, UrlBroadBroadConstants.HOST + "/" + UrlBroadBroadConstants.GET_GROUP_MEETING,
                                JSONObject(map).toString(),
                                MeetingDetailBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                        (bean as? MeetingDetailBroadBean)?.let {
                                            it.data?.let { data ->
                                                data.id?.let {
                                                    EventBus.getDefault().post(
                                                        MyBroadEvent(
                                                            "qunMeeting",
                                                            data.roomId,
                                                            QuickJsonBroadUtils.build(
                                                                "title" to (data.roomName
                                                                    ?: "会议室"),
                                                                "ownerId" to data.ownerId
                                                            ).toString()
                                                        )
                                                    )
                                                }
                                                if (data.id.isNullOrEmpty()) {
                                                    EventBus.getDefault().post(
                                                        MyBroadEvent(
                                                            "qunMeeting",
                                                            "",
                                                            ""
                                                        )
                                                    )

                                                }
                                            }

                                        }

                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                    }

                                })
                        }

                        "enterMeeting" -> {
                            if (!params.isNullOrEmpty()) {
                                MyCommonBroadBridge.getInstance().setMyBridgeMethod(
                                    context,
                                    "meeting",
                                    params,
                                    params2,
                                    null
                                )
                            }
                        }

                        "huoququnxiangqing" -> {
                            val map = mapOf(
                                "id" to (params ?: ""),
                            )
                            okGoHelper.post(
                                context,
                                "/groupInfo",
                                JSONObject(map).toString(),
                                CommonResultBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                        if (bean is CommonResultBroadBean) {
                                            bean.data?.apply {
                                                EventBus.getDefault().post(
                                                    MyBroadEvent(
                                                        "qunxiangqing",
                                                        gson.toJson(this),
                                                        ""
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                        EventBus.getDefault()
                                            .post(MyBroadEvent("qunxiangqingshibai", "", ""))
                                    }
                                },
                            )
                        }

                        "chakanqungonggao" -> {
                            JumpBroadUtils.startQunGonggaoActivity(mContext, params ?: "")
                        }

                        "gengxingonggaole" -> {
                            val gongGaoHolder = GongGaoBroadHolder(mContext, params ?: "")
                            gongGaoHolder.show()
                        }

                        "huoquyonghuxinxi" -> {
                            MainBroadActivity.currtentLoginUser?.apply {
                                EventBus.getDefault()
                                    .post(MyBroadEvent("yonghuxinxi", gson.toJson(this), ""))
                            }
                        }

                        "weizhi" -> {
                            mContext?.apply {
                                XXPermissions.with(this)
                                    .permission(
                                        Permission.ACCESS_COARSE_LOCATION,
                                        Permission.ACCESS_FINE_LOCATION
                                    ).request { _, _ ->
                                        JumpBroadUtils.startInputTipsActivity3(
                                            mContext,
                                            params ?: "",
                                            params2 ?: ""
                                        )
                                    }
                            }
                        }

                        "daohang" -> {
                            val paraList = params2?.split("|")
                            paraList?.apply {
                                JumpBroadUtils.startMapsActivity(
                                    mContext,
                                    this[0],
                                    this[1],
                                    params ?: "",
                                    ""
                                )
                            }
                        }

                        "mingancichufajianyan" -> {
                            val map = mapOf(
                                "uids" to (params2 ?: ""),
                                "id" to (params ?: ""),
                                "type" to "4"
                            )
                            okGoHelper.post(
                                context,
                                "/jinyan",
                                JSONObject(map).toString(),
                                NoResultBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {

                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                    }
                                },
                            )
                        }

                        "bofangshipin" -> {
                            val intent = Intent(mContext, VideoPlayOnlyBroadActivity::class.java)
                            intent.putExtra("video", params ?: "")
                            intent.putExtra("videoImage", params ?: "")
                            msg?.let {
                                if (it.message.content is VideoMessageContent &&(it.message.content as VideoMessageContent).thumbnail !=null){
                                    intent.putExtra("thumbnail", (it.message.content as VideoMessageContent).thumbnail)
                                }
                            }

                            mContext?.startActivity(intent)
                        }

                        "AI" -> {
                            if (params == MMKVBroadUtils.getKeFuId()) {
                                val map = mapOf(
                                    "uid" to BroadConstant.getRealUid(),
                                    "info" to params2,
                                )
                                okGoHelper.post(
                                    context,
                                    "/aihuifu",
                                    JSONObject(map).toString(),
                                    CommonResultBroadBean(),
                                    callBack = object : OKGoBroadHelper.RequestCallBack {
                                        override fun onSuccess(bean: BaseBroadResponse?) {

                                        }

                                        override fun onFail(bean: BaseBroadResponse?) {
                                        }
                                    },
                                )
                            } else {
                                if (params2?.contains("@" + MMKVBroadUtils.getKeFuName()) == true) {
                                    var info = removeBeforeFirstSpace(params2 ?: "")
                                    val map = mapOf(
                                        "id" to params,
                                        "uid" to BroadConstant.getRealUid(),
                                        "info" to info,
                                    )
                                    okGoHelper.post(
                                        context,
                                        "/aihuifu",
                                        JSONObject(map).toString(),
                                        CommonResultBroadBean(),
                                        callBack = object : OKGoBroadHelper.RequestCallBack {
                                            override fun onSuccess(bean: BaseBroadResponse?) {

                                            }

                                            override fun onFail(bean: BaseBroadResponse?) {
                                            }
                                        },
                                    )
                                }
                            }
                        }

                        "shoucangim" -> {
                            JumpBroadUtils.startMyCollectActivity(mContext, params ?: "", params2 ?: "")
                        }

                        "huiyishi" -> {
                            val map = mapOf(
                                "code" to "",
                            )
                            okGoHelper.post(
                                context,
                                "/userInfo",
                                JSONObject(map).toString(),
                                CommonResultBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                        (bean as? CommonResultBroadBean)?.let {
                                            val userIdNo = it.data?.mId ?: ""
                                            val map = mapOf(
                                                "id" to params2,
                                            )

                                            okGoHelper.post(
                                                context,
                                                "/groupInfo",
                                                JSONObject(map).toString(),
                                                CommonResultBroadBean(),
                                                showToast = true,
                                                showLoading = false,
                                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                                        if (bean is CommonResultBroadBean) {
                                                            if (bean.data?.userType == "1" || bean.data?.userType == "2") {

                                                                MeetingBroadHolder.openMeetingDialog(
                                                                    mContext,
                                                                    it.data?.mId ?: "",
                                                                    params2 ?: "",
                                                                    okGoHelper,
                                                                    context
                                                                )
                                                            } else {
                                                                ToastBroadUtils.show("只有群主/管理员可以开启会议室")
                                                            }
                                                        }
                                                    }

                                                    override fun onFail(bean: BaseBroadResponse?) {
                                                    }
                                                },
                                            )


                                        }
                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                    }

                                }


                            )
                        }

                        "meeting" -> {
                            val map = mapOf(
                                "code" to "",
                            )
                            okGoHelper.post(
                                context,
                                "/userInfo",
                                JSONObject(map).toString(),
                                CommonResultBroadBean(),
                                callBack = object : OKGoBroadHelper.RequestCallBack {
                                    override fun onSuccess(bean: BaseBroadResponse?) {
                                        (bean as? CommonResultBroadBean)?.let { cm ->
                                            QuickJsonBroadUtils.parse(params2 ?: "")?.let {
                                                val meetTitle =
                                                    QuickJsonBroadUtils.getValue(params2 ?: "", "title")
                                                val ownerId = QuickJsonBroadUtils.getValue(
                                                    params2 ?: "",
                                                    "ownerId"
                                                )
//                                                ServerBroadManager.getKickUserList(context,params?:""){
//                                                    it?.data?.let {
//                                                        if (it.isNotEmpty()){
//                                                            val index = it.firstOrNull {
//                                                                it.userId == cm.data?.idno
//                                                            }
//                                                            index?.let {
//                                                                ToastBroadUtils.show("您已被房主/管理员踢出会议室！")
//                                                            }
//                                                            if (index==null){
//                                                                if (ownerId==cm.data?.idno){
//                                                                    JumpBroadUtils.startQunMeetingActivity(mContext,params?:"",ownerId?:"",meetTitle?:"",false)
//                                                                }else{
//                                                                    JumpBroadUtils.startQunMeetingActivity(mContext,params?:"",ownerId?:"",meetTitle?:"",false)
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                    if (it==null||it.data.isNullOrEmpty()){
//                                                        if (ownerId==cm.data?.idno){
//                                                            JumpBroadUtils.startQunMeetingActivity(mContext,params?:"",ownerId?:"",meetTitle?:"",false)
//                                                        }else{
//                                                            JumpBroadUtils.startQunMeetingActivity(mContext,params?:"",ownerId?:"",meetTitle?:"",false)
//                                                        }
//                                                    }
//
//
//                                                }
                                                if (ownerId == cm.data?.mId) {
                                                    JumpBroadUtils.startQunMeetingActivity(
                                                        mContext,
                                                        params ?: "",
                                                        ownerId ?: "",
                                                        meetTitle ?: "",
                                                        false
                                                    )
                                                } else {
                                                    JumpBroadUtils.startQunMeetingActivity(
                                                        mContext,
                                                        params ?: "",
                                                        ownerId ?: "",
                                                        meetTitle ?: "",
                                                        false
                                                    )
                                                }


                                            }


                                        }
                                    }

                                    override fun onFail(bean: BaseBroadResponse?) {
                                    }

                                }


                            )

                        }
                    }
                }

            })
    }

    fun removeBeforeFirstSpace(input: String): String? {
        val spaceIndex = input.indexOf(' ')
        return if (spaceIndex != -1) {
            input.substring(spaceIndex + 1)
        } else {
            // 如果没有空格，则返回原始字符串
            input
        }
    }

    private fun shoucang(
        context: Context?,
        uid: String,
        info: String,
        type: String,
        miao: String = ""
    ) {
        val map = mapOf(
            "uid" to uid,
            "info" to info,
            "type" to type,
            "miao" to miao
        )

        okGoHelper.post(
            context,
            "/logColl",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        ToastBroadUtils.show(context?.getString(R.string.shoucangchengg))
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }


    private fun fanyi(
        context: Context?,
        yuzhong: String,
        info: String,
        message: Message,
    ) {
        val map = mapOf(
            "yuzhong" to yuzhong,
            "info" to info,
        )
        okGoHelper.post(
            context,
            "/aifanyi",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            showToast = true,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        if (!StringBroadUtil.isEmpty(bean.answer)) {
//                            message.localExtra = bean.answer
                            SharePrefBroadBroadUtil.saveString(
                                context,
                                message.messageUid.toString(),
                                bean.answer
                            )
                            EventBus.getDefault().post(FanYiBroadEvent())
                        }
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }


    fun duiInfo(params: String) {
        val map = mapOf(
            "id" to params,
        )
        okGoHelper.post(
            context,
            "/duiInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        saveUserData(bean.data)
                        if (bean.data?.isfanyi == "1") {
                            SharePrefBroadBroadUtil.saveString(
                                context,
                                "autofanyi",
                                "1"
                            )
                            SharePrefBroadBroadUtil.saveLong(
                                context,
                                "autofanyitime",
                                bean.data?.fantime!!.toLong()
                            )
                        } else {
                            SharePrefBroadBroadUtil.saveString(
                                context,
                                "autofanyi",
                                "0"
                            )
                        }
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    fun groupInfo(params: String) {
        val map = mapOf(
            "id" to params,
        )
        okGoHelper.post(
            context,
            "/groupInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonResultBroadBean) {
                        if (bean.data?.isfanyi == "1") {
                            SharePrefBroadBroadUtil.saveString(
                                context,
                                "autofanyi",
                                "1"
                            )
                            SharePrefBroadBroadUtil.saveLong(
                                context,
                                "autofanyitime",
                                bean.data?.fantime!!.toLong()
                            )
                        } else {
                            SharePrefBroadBroadUtil.saveString(
                                context,
                                "autofanyi",
                                "0"
                            )
                        }
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    private fun hbInfo(
        context: Context?,
        id: String,
        messageUid: String,
        payload: String,
        msg: UiBroadMessage,
        lingStatus: String,
        isGroup: Boolean
    ) {
        eLogD("hbInfoClass", "lingStatus:$lingStatus")
        if (lingStatus == "2") {
            JumpBroadUtils.toGetWebxRedEnvelopesActivity(
                context,
                id,
                isGroup = isGroup,
                isReceived = true,
                payload,
                messageUid,
                msg.message.serverTime,
                msg.message.conversation.target,
                msg.message.sender, msg.message.messageId
            )
        } else {
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
                            if (bean.data!!.isLing == "1") {
                                //已领取
//                            JumpBroadUtils.startHongBaoPageActivity(context, id, "1")
                                val hongBaoMessageContent =
                                    msg.message.content as HongBaoMessageContent
                                hongBaoMessageContent.lingquStatus = "1"
                                ChatManager.Instance()
                                    .updateMessage(msg.message.messageId, hongBaoMessageContent)
                                JumpBroadUtils.toGetWebxRedEnvelopesActivity(
                                    context,
                                    id,
                                    isGroup = isGroup,
                                    isReceived = true,
                                    payload,
                                    messageUid,
                                    msg.message.serverTime,
                                    msg.message.conversation.target,
                                    msg.message.sender, msg.message.messageId
                                )
                            } else {
                                if (bean.data?.tuiStatus == 1) {
                                    //红包已过期
                                    val hongBaoMessageContent =
                                        msg.message.content as HongBaoMessageContent
                                    hongBaoMessageContent.lingquStatus = "3"
                                    ChatManager.Instance()
                                        .updateMessage(msg.message.messageId, hongBaoMessageContent)
                                    ToastBroadUtils.show(context?.getString(R.string.string_hongbao_expired))
                                } else if (bean.data?.tuiStatus == 2) {
                                    //红包已拒绝
                                    val hongBaoMessageContent =
                                        msg.message.content as HongBaoMessageContent
                                    hongBaoMessageContent.lingquStatus = "2"
                                    ChatManager.Instance()
                                        .updateMessage(msg.message.messageId, hongBaoMessageContent)
//                                    ToastBroadUtils.show("红包已拒绝")
                                } else {
                                    JumpBroadUtils.toGetWebxRedEnvelopesActivity(
                                        context,
                                        id,
                                        isGroup = isGroup,
                                        isReceived = !isGroup && msg.message.sender == MMKVBroadUtils.getRealUserId(),
                                        payload,
                                        messageUid,
                                        msg.message.serverTime,
                                        msg.message.conversation.target,
                                        msg.message.sender, msg.message.messageId
                                    )
//                                val cheDuiManager = LingQunHongBaoDialogBroadFragment()
//                                cheDuiManager.hongbaoId = id
//                                cheDuiManager.isBottomAni = false
//                                cheDuiManager.isWrap = false
//                                cheDuiManager.msgBean = msg
//                                cheDuiManager.isgroup = "1"
//                                cheDuiManager.messageUid = messageUid
//                                cheDuiManager.payload = payload
//                                cheDuiManager.show(
//                                    (context as AppCompatActivity).supportFragmentManager,
//                                    "LingQunHongBaoDialogBroadFragment"
//                                )
                                }

                            }
                        }
                    }

                    override fun onFail(bean: BaseBroadResponse?) {
                    }
                },
            )
        }

    }

    private fun addEmoji(context: Context?, img: String) {
        val map = mapOf(
            "img" to img,
        )
        okGoHelper.post(
            context,
            "/addEmoji",
            JSONObject(map).toString(),
            NoResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is NoResultBroadBean) {
                        ToastBroadUtils.show(context?.getString(R.string.tianjaichengg))
                        addCustomFace()
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )
    }

    private var gson = Gson()
    fun addCustomFace() {
        val map = mapOf(
            "pageNo" to "1",
            "pageSize" to "10",
        )
        if (okGoHelper == null) {
            okGoHelper = OKGoBroadHelper()
        }
        okGoHelper.post(
            context,
            "/emojiList",
            JSONObject(map).toString(),
            CommonDataListBroadBean(),
            showToast = false,
            showLoading = false,
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    if (bean is CommonDataListBroadBean) {
//                        bean.dataList?.apply {
//                            val dataList = arrayListOf<String>()
//                            this.forEachIndexed { index, commonBean ->
//                                dataList.add(commonBean.emoji?:"")
//                            }
//                        }
                        EventBus.getDefault()
                            .post(EmotionBroadEvent("biaoqiangneirong", gson.toJson(bean)))
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                }
            },
        )

    }
}