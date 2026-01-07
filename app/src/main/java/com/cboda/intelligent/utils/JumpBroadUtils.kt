package com.cboda.intelligent.utils

import android.content.Context
import android.content.Intent
import com.cboda.chatui.search.SearchPortalBroadActivity
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.intelligent.MainBroadActivity
import com.cboda.intelligent.activity.*
import com.cboda.intelligent.bean.AiCheckResultBroadBean
import com.cboda.intelligent.service.MeetingFloatBroadService
import com.cboda.agora.ui.ui.basic.AUIAlertBroadDialog
import org.greenrobot.eventbus.EventBus


class JumpBroadUtils {
    companion object {

        fun startMainActivity(context: Context?, type: String = "") {
            val intent = Intent(context, MainBroadActivity::class.java)
            intent.putExtra("type", type)
            context?.startActivity(intent)

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

        fun startH5Activity(context: Context?, url: String = "", fromPage: String = "") {
            eLogD("startH5Activity", "url:$url")
            val intent = Intent(context, H5BroadActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("fromPage", fromPage)
            context?.startActivity(intent)
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
            val intent = Intent(context, YanZhengBroadActivity::class.java)
            intent.putExtra("fromPage", fromPage)
            context?.startActivity(intent)
        }

        fun startQunInfoActivity(context: Context?, qunId: String) {
            val intent = Intent(context, QunInfoBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
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

        fun startQunUserListActivity(context: Context?, qunId: String) {
            val intent = Intent(context, QunUserListBroadActivity::class.java)
            intent.putExtra("qunid", qunId)
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

        fun startQunCodeActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunCodeBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
//            context?.let {
//                SaveGroupQrCodeBroadPop(it,qunid).showPopupWindow()
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

        fun startQunMeetingActivity(
            context: Context?,
            roomId: String,
            ownerId: String,
            title: String,
            isCreate: Boolean = false,
            isMute: Boolean? = false,
            isMic: Boolean? = true
        ) {
            context?.let { ctx ->
                if (MeetingFloatBroadService.isServiceRunning && MeetingFloatBroadService.isFloatShow) {
                    AUIAlertBroadDialog(ctx).apply {
                        setTitle("已加入其他会议室")
                        setMessage("您当前正在其他会议室，是否退出当前所在会议室？")
                        setPositiveButton("确认退出") {
                            dismiss()
                            EventBus.getDefault().post(
                                MeetingFloatBroadService.MeetingEvent(
                                    MeetingFloatBroadService.EVENT_BACKGROUND_QUITE,
                                    ownerId
                                )
                            )
//                            MeetingFloatBroadService.stop(ctx)
                        }
                        setNegativeButton("取消") {
                            dismiss()
                        }
                        show()

                    }


                } else {
                    eLogD("zhanglong","roomId==$roomId---isCreate==$isCreate---MeetingFloatBroadService==${MeetingFloatBroadService.isFloatShow}")
                    if (MeetingFloatBroadService.isServiceRunning){
                        EventBus.getDefault().post(
                            MeetingFloatBroadService.MeetingEvent(
                                MeetingFloatBroadService.EVENT_BACKGROUND_QUITE,
                                ownerId
                            )
                        )
                    }
                    MeetingRoomBroadActivity.launch(ctx, roomId, ownerId, title, isCreate, isMute, isMic)
                }

            }
        }

        fun startHaoyouListActivity(context: Context?, uid: String, qunid: String) {
            val intent = Intent(context, HaoyouListBroadActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        fun startHaoyouApplyActivity(
            context: Context?,
            uid: String,
            userName: String,
            source: String,
            qunid: String
        ) {
            val intent = Intent(context, HaoyouApplyBroadActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("uid", uid)
            intent.putExtra("source", source)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
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
//            val intent = Intent(context, LixinSearchBroadActivity::class.java)
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

    }
}