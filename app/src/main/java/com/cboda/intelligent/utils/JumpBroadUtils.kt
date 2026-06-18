package com.cboda.intelligent.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.blankj.utilcode.util.ActivityUtils
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.chatui.search.SearchPortalBroadActivity
import com.cboda.intelligent.MainBroadActivity
import com.cboda.intelligent.activity.AiActivity
import com.cboda.intelligent.activity.AiAntiFraudBroadActivity
import com.cboda.intelligent.activity.AiCheckRecordBroadActivity
import com.cboda.intelligent.activity.AiCheckResultBroadActivity
import com.cboda.intelligent.activity.CreateQunBroadActivity
import com.cboda.intelligent.activity.CreditScoreActivity
import com.cboda.intelligent.activity.DsxxAddBroadActivity
import com.cboda.intelligent.activity.DsxxBroadActivity
import com.cboda.intelligent.activity.FaHongBaoBroadActivity
import com.cboda.intelligent.activity.FaQunHongBaoBroadActivity
import com.cboda.intelligent.activity.GetWebxRedEnvelopesBroadActivity
import com.cboda.intelligent.activity.H5BroadActivity
import com.cboda.intelligent.activity.HaoYouBeizhuBroadActivity
import com.cboda.intelligent.activity.HaoYouPageBroadActivity
import com.cboda.intelligent.activity.HaoYouSetBroadActivity
import com.cboda.intelligent.activity.HaoyouApplyBroadActivity
import com.cboda.intelligent.activity.HaoyouListBroadActivity
import com.cboda.intelligent.activity.HongBaoPageBroadActivity
import com.cboda.intelligent.activity.HyyAddBroadActivity
import com.cboda.intelligent.activity.HyyBroadActivity
import com.cboda.intelligent.activity.LoginQrCodeActivity
import com.cboda.intelligent.activity.MinGanCiBroadActivity
import com.cboda.intelligent.activity.MyCollectBroadActivity
import com.cboda.intelligent.activity.NewEquipmentBroadActivity
import com.cboda.intelligent.activity.QunGerenJInyanTimeBroadActivity
import com.cboda.intelligent.activity.QunGerenPageBroadActivity
import com.cboda.intelligent.activity.QunGongGaoBroadActivity
import com.cboda.intelligent.activity.QunGonggaoInfoBroadActivity
import com.cboda.intelligent.activity.QunInfo2BroadActivity
import com.cboda.intelligent.activity.QunJinYanBroadActivity
import com.cboda.intelligent.activity.QunJinYanMemberBroadActivity
import com.cboda.intelligent.activity.QunListBroadActivity
import com.cboda.intelligent.activity.QunManagerBroadActivity
import com.cboda.intelligent.activity.QunMemberListBroadActivity
import com.cboda.intelligent.activity.QunNameBroadActivity
import com.cboda.intelligent.activity.QunNichengBroadActivity
import com.cboda.intelligent.activity.QunTouSuBroadActivity
import com.cboda.intelligent.activity.QunUserList2BroadActivity
import com.cboda.intelligent.activity.im.ImGroupIntent
import com.cboda.intelligent.activity.im.ImQunCodeBroadActivity
import com.cboda.intelligent.activity.im.ImQunGerenPageBroadActivity
import com.cboda.intelligent.activity.im.ImQunGongGaoBroadActivity
import com.cboda.intelligent.activity.im.ImQunInfo2BroadActivity
import com.cboda.intelligent.activity.im.ImQunMemberListBroadActivity
import com.cboda.intelligent.activity.im.ImQunNameBroadActivity
import com.cboda.intelligent.activity.im.ImQunUserList2BroadActivity
import com.cboda.intelligent.activity.ScanBroadActivity
import com.cboda.intelligent.activity.SearchUserBroadActivity
import com.cboda.intelligent.activity.SjqlBroadActivity
import com.cboda.intelligent.activity.TestActivity
import com.cboda.intelligent.activity.TouSuLeiXingBroadActivity
import com.cboda.intelligent.activity.VerifyBroadActivity
import com.cboda.intelligent.activity.XuanQunJinYanBroadActivity
import com.cboda.intelligent.activity.ZhuanRangQunBroadActivity
import com.cboda.intelligent.activity.ZhuanZhangPageBroadActivity
import com.cboda.intelligent.activity.ZhuanzhangBroadActivity
import com.cboda.intelligent.activity.ZoomPicBroadActivity
import com.cboda.intelligent.bean.AiCheckResultBroadBean
import com.cboda.intelligent.dialog.GroupQrCodeDialog
import com.freddy.chat.MessageListActivity
import com.freddy.im.http.model.im.GroupVo
import com.freddy.chat.SessionItemIntent
import com.freddy.chat.collect.CollectIntent
import com.freddy.im.http.model.im.SessionItem


class JumpBroadUtils {

    companion object {
        private val tAG = "JumpBroadUtilsClass"
        fun toLoginQrCodeAct(context: Context? = null) {
            val mContext = getAppContext(context)
//            val haveLoginQrCodeAct = ActivityUtils.getActivityList().any { it is LoginQrCodeActivity }
//            if (haveLoginQrCodeAct) {
//                ActivityUtils.finishActivity(LoginQrCodeActivity::class.java)
//            }
            val intent = Intent(mContext, LoginQrCodeActivity::class.java)
            mContext.startActivity(intent)
//            ActivityUtils.getActivityList().forEach {
//                val actName = it.javaClass.simpleName
//                eLogD(tAG, "actName2:$actName")
//            }
        }

        fun startMainActivity(context: Context? = null, type: String = "") {
            val mContext = getAppContext(context)
            var checkActName = "MainBroadActivity"
            //82环境登录后不自动跳转到登录二维码页面
            val intent = if (HttpsBroadConfig.currentHostHttpUrlType < 1000) {
                Intent(mContext, MainBroadActivity::class.java)
            } else if (type == "SplashBroadActivity" || type == "LoginVerificationActivity" || type == "NewEquipmentBroadActivity" || type == "ScanBroadActivity") {
                checkActName = "LoginQrCodeActivity"
                Intent(mContext, LoginQrCodeActivity::class.java)
            } else {
                Intent(mContext, MainBroadActivity::class.java)
            }
            intent.putExtra("type", type)
            if (type == "FontSizeBroadActivity") {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            mContext.startActivity(intent)
            ActivityUtils.getActivityList().forEach {
                val actName = it.javaClass.simpleName
//                eLogD(tAG, "actName:$actName")
                if (actName != checkActName) {
                    it.finish()
                }
            }
        }

        fun toTestActivity(context: Context? = null) {
            val mContext = getAppContext(context)
//            val haveLoginQrCodeAct = ActivityUtils.getActivityList().any { it is LoginQrCodeActivity }
//            if (haveLoginQrCodeAct) {
//                ActivityUtils.finishActivity(LoginQrCodeActivity::class.java)
//            }
            val intent = Intent(mContext, TestActivity::class.java)
            mContext.startActivity(intent)
//            ActivityUtils.getActivityList().forEach {
//                val actName = it.javaClass.simpleName
//                eLogD(tAG, "actName2:$actName")
//            }
        }

        fun startLoginActivity(context: Context?) {
            val intent = Intent(context, NewEquipmentBroadActivity::class.java)
            context?.startActivity(intent)
        }

        fun startMyCollectActivity(context: Context?, uid: String, qunid: String) {
            val intent = Intent(context, MyCollectBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        /** 自研收藏夹（聊天内带 uid/qunid 可点选发送；「我的」页无会话参数仅浏览/转发/删除）。 */
        fun startImMyCollectActivity(context: Context?, uid: String = "", qunid: String = "", sessionId: String = "") {
            context?.startActivity(CollectIntent.build(context, uid, qunid, sessionId))
        }


        fun startZoomPicActivity(
            context: Context?,
            imgUrl: ArrayList<String>,
            isLoca: Boolean = false,
            position: Int
        ) {
            val intent = Intent(context, ZoomPicBroadActivity::class.java)
            intent.putStringArrayListExtra("imgUrl", imgUrl)
            intent.putExtra("isLoca", isLoca)
            intent.putExtra("position", position)
            context?.startActivity(intent)
        }

        fun startH5Activity(context: Context? = null, url: String = "", fromPage: String = "") {
            eLogD("startH5Activity", "url:$url")
            val mContext = getAppContext(context)
            val intent = Intent(mContext, H5BroadActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("fromPage", fromPage)
            mContext.startActivity(intent)
        }

        fun toAiCheckRecordAct(context: Context? = null) {
            val mContext = getAppContext(context)
            val intent = Intent(mContext, AiCheckRecordBroadActivity::class.java)
            mContext.startActivity(intent)
        }

        fun toAiActivity(context: Context? = null) {
            val mContext = getAppContext(context)
            val intent = Intent(mContext, AiActivity::class.java)
            mContext.startActivity(intent)
        }

        fun startAiAntiFraudActivity(context: Context? = null) {
            val mContext = getAppContext(context)
            val intent = Intent(mContext, AiAntiFraudBroadActivity::class.java)
            mContext.startActivity(intent)
        }

        fun startAiActivity(context: Context? = null) {
            val mContext = getAppContext(context)
            val intent = Intent(mContext, AiActivity::class.java)
            mContext.startActivity(intent)
        }

//        fun startHaoYouPageActivity(context: Context?, uid: String) {
//            val intent = Intent(context, HaoYouPageBroadActivity::class.java)
//            intent.putExtra("uid", uid)
//            context?.startActivity(intent)
//        }

        fun startHaoYouAddActivity(context: Context?, uid: String, source: String) {
//            val intent = Intent(context, HaoYouAddBroadActivity::class.java)
            val intent = Intent(context, HaoYouPageBroadActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("source", source)
            context?.startActivity(intent)
        }

        fun startHaoYouBeizhuActivity(context: Context?, uid: String, beizhu: String) {
            val intent = Intent(context, HaoYouBeizhuBroadActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("beizhu", beizhu)
            context?.startActivity(intent)
        }

        fun startHaoYouSetActivity(context: Context?, uid: String, uname: String) {
            val intent = Intent(context, HaoYouSetBroadActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("uname", uname)
            context?.startActivity(intent)
        }


        fun startFaHongBaoActivity(context: Context?, uid: String) {
            val intent = Intent(context, FaHongBaoBroadActivity::class.java)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        fun startZhuanzhangActivity(context: Context?, uid: String) {
            val intent = Intent(context, ZhuanzhangBroadActivity::class.java)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        fun startHongBaoPageActivity(context: Context?, hongbaoId: String, isgroup: String) {
            val intent = Intent(context, HongBaoPageBroadActivity::class.java)
            intent.putExtra("hongbaoId", hongbaoId)
            intent.putExtra("isgroup", isgroup)
            context?.startActivity(intent)
        }

        fun toGetWebxRedEnvelopesActivity(
            context: Context?,
            hongbaoId: String,
            isGroup: Boolean,
            isReceived: Boolean,
            payload: String?,
            messageUid: String?,
            messageTime: Long?,
            msgTarget: String?,
            sender: String?,
            messageId: Long?
        ) {
            val intent = Intent(context, GetWebxRedEnvelopesBroadActivity::class.java)
            intent.putExtra("redEnvelopesId", hongbaoId)
            intent.putExtra("isGroup", isGroup)
            intent.putExtra("isReceived", isReceived)
            intent.putExtra("payload", payload)
            intent.putExtra("messageUid", messageUid)
            intent.putExtra("messageTime", messageTime ?: 0L)
            intent.putExtra("msgTarget", msgTarget)
            intent.putExtra("sender", sender)
            intent.putExtra("messageId", messageId ?: 0L)
            context?.startActivity(intent)
        }


        fun startZhuanZhangPageActivity(
            context: Context?,
            hongbaoId: String,
            messageUid: String,
            payload: String
        ) {
            val intent = Intent(context, ZhuanZhangPageBroadActivity::class.java)
            intent.putExtra("hongbaoId", hongbaoId)
            intent.putExtra("messageUid", messageUid)
            intent.putExtra("payload", payload)
            context?.startActivity(intent)
        }

        fun startYanZhengActivity(context: Context?, fromPage: String) {
            val intent = Intent(context, VerifyBroadActivity::class.java)
            intent.putExtra("fromPage", fromPage)
            context?.startActivity(intent)
        }

        fun startQunInfoActivity(context: Context?, qunId: String) {
            val intent = Intent(context, QunInfo2BroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            context?.startActivity(intent)
        }
        /** 自研 IM 群详情入口。 */
        fun startImQunInfoActivity(context: Context?, qunId: String) {
            val intent = Intent(context, ImQunInfo2BroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            context?.startActivity(intent)
        }

        fun startImQunUserListActivity(context: Context?, qunId: String, myRole: Int) {
            val intent = Intent(context, ImQunUserList2BroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra(ImGroupIntent.EXTRA_MY_ROLE, myRole)
            context?.startActivity(intent)
        }

        fun startImQunMemberListActivity(
            context: Context?,
            qunId: String,
            fromPage: String,
            userType: String
        ) {
            val intent = Intent(context, ImQunMemberListBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("fromPage", fromPage)
            intent.putExtra("userType", userType)
            context?.startActivity(intent)
        }

        fun startImQunGongGaoActivity(context: Context?, qunId: String, notice: String) {
            val intent = Intent(context, ImQunGongGaoBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra(ImGroupIntent.EXTRA_NOTICE, notice)
            context?.startActivity(intent)
        }

        fun startImQunNameActivity(context: Context?, qunId: String, qunName: String) {
            val intent = Intent(context, ImQunNameBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("qunName", qunName)
            context?.startActivity(intent)
        }

        fun startImQunCodeActivity(context: Context?, qunId: String, groupVo: GroupVo?) {
            val intent = Intent(context, ImQunCodeBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            if (groupVo != null) {
                intent.putExtra(ImGroupIntent.EXTRA_GROUP_VO, groupVo)
            }
            context?.startActivity(intent)
        }

        fun startImQunGerenPageActivity(
            context: Context?,
            qunId: String,
            uid: String,
            type: String,
            myRole: Int
        ) {
            val intent = Intent(context, ImQunGerenPageBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("uid", uid)
            intent.putExtra("type", type)
            intent.putExtra(ImGroupIntent.EXTRA_MY_ROLE, myRole)
            context?.startActivity(intent)
        }


        fun startQunShengJiActivity(context: Context?, qunId: String) {
            val intent = Intent(context, SjqlBroadActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startHyyActivity(context: Context?, qunId: String) {
            val intent = Intent(context, HyyBroadActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startHyyAddActivity(context: Context?, qunId: String) {
            val intent = Intent(context, HyyAddBroadActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startJiQiRenActivity(context: Context?, qunId: String) {
            val intent = Intent(context, DsxxBroadActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startJiQiRenAddActivity(context: Context?, qunId: String) {
            val intent = Intent(context, DsxxAddBroadActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startQunUserListActivity(context: Context?, qunId: String, openQyKj: String?) {
            val intent = Intent(context, QunUserList2BroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("openQyKj", openQyKj ?: "")
            context?.startActivity(intent)
        }

        fun startQunTouSuActivity(context: Context?, qunId: String, qunName: String) {
            val intent = Intent(context, QunTouSuBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("qunName", qunName)
            context?.startActivity(intent)
        }

        fun startTouSuLeiXingActivity(context: Context?) {
            val intent = Intent(context, TouSuLeiXingBroadActivity::class.java)
            context?.startActivity(intent)
        }


        fun startQunMemberListActivity(
            context: Context?,
            qunId: String,
            fromPage: String,
            userType: String = ""
        ) {
            val intent = Intent(context, QunMemberListBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("fromPage", fromPage)
            intent.putExtra("userType", userType)
            context?.startActivity(intent)
        }


        fun startQunNameActivity(context: Context?, qunid: String, qunName: String) {
            val intent = Intent(context, QunNameBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("qunName", qunName)
            context?.startActivity(intent)
        }

        fun startQunGongGaoActivity(context: Context?, qunid: String, qunName: String) {
            val intent = Intent(context, QunGongGaoBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("qunName", qunName)
            context?.startActivity(intent)
        }

        fun startQunNichengActivity(context: Context?, qunid: String, beizhu: String) {
            val intent = Intent(context, QunNichengBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("beizhu", beizhu)
            context?.startActivity(intent)
        }

        fun startQunManagerActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunManagerBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startQunJinYanActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunJinYanBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startXuanQunJinYanActivity(context: Context?, qunid: String) {
            val intent = Intent(context, XuanQunJinYanBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startQunJinYanMemberActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunJinYanMemberBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startMinGanCiActivity(context: Context?, qunid: String, mgc: String) {
            val intent = Intent(context, MinGanCiBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("mgc", mgc)
            context?.startActivity(intent)
        }

        fun startQunCodeActivity(context: Context?, qunid: String, qunxiangqing: CommonResultBroadBean.CommonResult?) {
//            val intent = Intent(context, QunCodeBroadActivity::class.java)
//            intent.putExtra("qunid", qunid)
//            context?.startActivity(intent)
            val groupQrCodeDialog = GroupQrCodeDialog(getAppContext(context), qunid, qunxiangqing)
            groupQrCodeDialog.showPopupWindow()
//            context?.let {
//                SaveGroupQrCodeBroadPop(it, qunid).showPopupWindow()
//            }
        }

        fun startZhuanRangQunActivity(context: Context?, qunid: String) {
            val intent = Intent(context, ZhuanRangQunBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startQunGerenPageActivity(
            context: Context?,
            qunid: String,
            uid: String,
            source: String,
            type: String
        ) {
            val intent = Intent(context, QunGerenPageBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("uid", uid)
            intent.putExtra("source", source)
            intent.putExtra("type", type)
            context?.startActivity(intent)
        }

        fun startQunGerenJInyanTimeActivity(context: Context?, qunid: String, uid: String) {
            val intent = Intent(context, QunGerenJInyanTimeBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        fun startQunGonggaoActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunGonggaoInfoBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startFaQunHongBaoActivity(context: Context?, qunid: String) {
            val intent = Intent(context, FaQunHongBaoBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        //跳转到会议室（混淆屏蔽）
        fun startQunMeetingActivity(
            context: Context?,
            roomId: String,
            ownerId: String,
            title: String,
            isCreate: Boolean = false,
            isMute: Boolean? = false,
            isMic: Boolean? = true
        ) {
//            context?.let { ctx ->
//                if (MeetingFloatBroadService.isServiceRunning && MeetingFloatBroadService.isFloatShow) {
//                    AUIAlertBroadDialog(ctx).apply {
//                        setTitle("已加入其他会议室")
//                        setMessage("您当前正在其他会议室，是否退出当前所在会议室？")
//                        setPositiveButton("确认退出") {
//                            dismiss()
//                            EventBus.getDefault().post(
//                                MeetingFloatBroadService.MeetingEvent(
//                                    MeetingFloatBroadService.EVENT_BACKGROUND_QUITE,
//                                    ownerId
//                                )
//                            )
////                            MeetingFloatBroadService.stop(ctx)
//                        }
//                        setNegativeButton("取消") {
//                            dismiss()
//                        }
//                        show()
//
//                    }
//
//
//                } else {
//                    eLogD(APP_TAG,"roomId==$roomId---isCreate==$isCreate---MeetingFloatBroadService==${MeetingFloatBroadService.isFloatShow}")
//                    if (MeetingFloatBroadService.isServiceRunning){
//                        EventBus.getDefault().post(
//                            MeetingFloatBroadService.MeetingEvent(
//                                MeetingFloatBroadService.EVENT_BACKGROUND_QUITE,
//                                ownerId
//                            )
//                        )
//                    }
//                    MeetingRoomBroadActivity.launch(ctx, roomId, ownerId, title, isCreate, isMute, isMic)
//                }
//            }
        }

        fun startHaoyouListActivity(context: Context?, uid: String, qunid: String, sessionId: String? = "") {
            val intent = Intent(context, HaoyouListBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("uid", uid)
            intent.putExtra("sessionId", sessionId)
            context?.startActivity(intent)
        }

        fun startHaoyouApplyActivity(
            context: Context?,
            uid: String,
            userName: String,
            source: String,
            qunid: String,
            type: String,
            startForResult: ActivityResultLauncher<Intent>? = null
        ) {
            val intent = Intent(context, HaoyouApplyBroadActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("uid", uid)
            intent.putExtra("source", source)
            intent.putExtra("qunid", qunid)
            intent.putExtra("type", type)
            if (null != startForResult) {
                startForResult.launch(intent)
            } else {
                context?.startActivity(intent)
            }
        }


        fun toScanActivity(
            jumpSource: Int,
            context: Context? = null,
            startForResult: ActivityResultLauncher<Intent>? = null
        ) {
            val mContext = getAppContext(context)
            val intent = Intent(mContext, ScanBroadActivity::class.java)
            intent.putExtra("jumpSource", jumpSource)
            if (null != startForResult) {
                startForResult.launch(intent)
            } else {
                mContext.startActivity(intent)
            }
        }

        fun startInputTipsActivity3(context: Context?, uid: String, qunid: String) {
//            val intent = Intent(context, InputTipsActivity3::class.java)
//            intent.putExtra("qunid", qunid)
//            intent.putExtra("uid", uid)
//            context?.startActivity(intent)
        }

        fun startMapsActivity(
            context: Context?,
            lat: String,
            lon: String,
            addTitle: String,
            addDesc: String
        ) {
//            val intent = Intent(context, MapsActivity::class.java)
//            intent.putExtra("lat", lat)
//            intent.putExtra("lon", lon)
//            intent.putExtra("addTitle", addTitle)
//            intent.putExtra("addDesc", addDesc)
//            context?.startActivity(intent)
        }

        fun startCreateQunActivity(context: Context?) {
            val intent = Intent(context, CreateQunBroadActivity::class.java)
            context?.startActivity(intent)
        }

        fun startSearchUserActivity(context: Context?) {
            val intent = Intent(context, SearchUserBroadActivity::class.java)
            context?.startActivity(intent)
        }

        fun startSearchActivity(context: Context?) {
//            val intent = Intent(context, MySearchBroadActivity::class.java)
//            context?.startActivity(intent)
            val intent = Intent(
                context,
                SearchPortalBroadActivity::class.java
            )
            context?.startActivity(intent)
        }

        fun startQunListActivity(context: Context?) {
            val intent = Intent(context, QunListBroadActivity::class.java)
            context?.startActivity(intent)
        }

        fun jumpAiCheckResultActivity(context: Context = getAppContext(), id: Int) {
            val intent = Intent(context, AiCheckResultBroadActivity::class.java)
            intent.putExtra("id", id)
            context.startActivity(intent)
        }

        fun jumpAiCheckResultActivity(
            context: Context = getAppContext(),
            bean: AiCheckResultBroadBean?
        ) {
            val intent = Intent(context, AiCheckResultBroadActivity::class.java)
            intent.putExtra("ai_data", bean)
            context.startActivity(intent)
        }

        //跳转信用分页
        fun startCreditScoreActivity(context: Context?) {
            val intent = Intent(context, CreditScoreActivity::class.java)
            context?.startActivity(intent)
        }

        //自研--进聊天页（内部通过session对象自己区分了单聊还是群里）
        fun startChatMsgActivity(sessionItem: SessionItem?, context: Context?) {
            sessionItem?.let {
                val mContext = getAppContext(context)
                val intent = Intent(mContext, MessageListActivity::class.java)
                SessionItemIntent.putSessionItem(intent, sessionItem)
                mContext.startActivity(intent)
            }
        }

    }
}