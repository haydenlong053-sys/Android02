package com.lxkj.aiim.utils

import android.content.Context
import android.content.Intent
import cn.wildfire.chat.kit.search.SearchPortalActivity
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.lxkj.aiim.MainActivity
import com.lxkj.aiim.activity.*
import com.lxkj.aiim.dialog.pop.SaveGroupQrCodePop
import com.lxkj.aiim.lj.*
import com.lxkj.aiim.lj.bean.AiCheckResultBean
import com.lxkj.aiim.service.MeetingFloatService
import io.agora.auikit.ui.basic.AUIAlertBroadDialog
import org.greenrobot.eventbus.EventBus


class JumpUtils {
    companion object {

        fun startMainActivity(context: Context?, type: String = "") {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("type", type)
            context?.startActivity(intent)

        }

        fun startLoginActivity(context: Context?) {
            val intent = Intent(context, NewEquipmentActivity::class.java)
            context?.startActivity(intent)
        }

        fun startMyCollectActivity(context: Context?, uid: String, qunid: String) {
            val intent = Intent(context, MyCollectActivity::class.java)
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
            val intent = Intent(context, ZoomPicActivity::class.java)
            intent.putStringArrayListExtra("imgUrl", imgUrl)
            intent.putExtra("isLoca", isLoca)
            intent.putExtra("position", position)
            context?.startActivity(intent)
        }

        fun startH5Activity(context: Context?, url: String = "", fromPage: String = "") {
            eLogD("startH5Activity", "url:$url")
            val intent = Intent(context, H5Activity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("fromPage", fromPage)
            context?.startActivity(intent)
        }

//        fun startHaoYouPageActivity(context: Context?, uid: String) {
//            val intent = Intent(context, HaoYouPageActivity::class.java)
//            intent.putExtra("uid", uid)
//            context?.startActivity(intent)
//        }

        fun startHaoYouAddActivity(context: Context?, uid: String, source: String) {
//            val intent = Intent(context, HaoYouAddActivity::class.java)
            val intent = Intent(context, HaoYouPageActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("source", source)
            context?.startActivity(intent)
        }

        fun startHaoYouBeizhuActivity(context: Context?, uid: String, beizhu: String) {
            val intent = Intent(context, HaoYouBeizhuActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("beizhu", beizhu)
            context?.startActivity(intent)
        }

        fun startHaoYouSetActivity(context: Context?, uid: String, uname: String) {
            val intent = Intent(context, HaoYouSetActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("uname", uname)
            context?.startActivity(intent)
        }


        fun startFaHongBaoActivity(context: Context?, uid: String) {
            val intent = Intent(context, FaHongBaoActivity::class.java)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        fun startZhuanzhangActivity(context: Context?, uid: String) {
            val intent = Intent(context, ZhuanzhangActivity::class.java)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        fun startHongBaoPageActivity(context: Context?, hongbaoId: String, isgroup: String) {
            val intent = Intent(context, HongBaoPageActivity::class.java)
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
            val intent = Intent(context, GetWebxRedEnvelopesActivity::class.java)
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
            val intent = Intent(context, ZhuanZhangPageActivity::class.java)
            intent.putExtra("hongbaoId", hongbaoId)
            intent.putExtra("messageUid", messageUid)
            intent.putExtra("payload", payload)
            context?.startActivity(intent)
        }

        fun startYanZhengActivity(context: Context?, fromPage: String) {
            val intent = Intent(context, YanZhengActivity::class.java)
            intent.putExtra("fromPage", fromPage)
            context?.startActivity(intent)
        }

        fun startQunInfoActivity(context: Context?, qunId: String) {
            val intent = Intent(context, QunInfoActivity::class.java)
            intent.putExtra("qunid", qunId)
            context?.startActivity(intent)
        }

        fun startQunShengJiActivity(context: Context?, qunId: String) {
            val intent = Intent(context, SjqlActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startHyyActivity(context: Context?, qunId: String) {
            val intent = Intent(context, HyyActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startHyyAddActivity(context: Context?, qunId: String) {
            val intent = Intent(context, HyyAddActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startJiQiRenActivity(context: Context?, qunId: String) {
            val intent = Intent(context, DsxxActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startJiQiRenAddActivity(context: Context?, qunId: String) {
            val intent = Intent(context, DsxxAddActivity::class.java)
            intent.putExtra("id", qunId)
            context?.startActivity(intent)
        }

        fun startQunUserListActivity(context: Context?, qunId: String) {
            val intent = Intent(context, QunUserListActivity::class.java)
            intent.putExtra("qunid", qunId)
            context?.startActivity(intent)
        }

        fun startQunTouSuActivity(context: Context?, qunId: String, qunName: String) {
            val intent = Intent(context, QunTouSuActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("qunName", qunName)
            context?.startActivity(intent)
        }

        fun startTouSuLeiXingActivity(context: Context?) {
            val intent = Intent(context, TouSuLeiXingActivity::class.java)
            context?.startActivity(intent)
        }


        fun startQunMemberListActivity(
            context: Context?,
            qunId: String,
            fromPage: String,
            userType: String = ""
        ) {
            val intent = Intent(context, QunMemberListActivity::class.java)
            intent.putExtra("qunid", qunId)
            intent.putExtra("fromPage", fromPage)
            intent.putExtra("userType", userType)
            context?.startActivity(intent)
        }


        fun startQunNameActivity(context: Context?, qunid: String, qunName: String) {
            val intent = Intent(context, QunNameActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("qunName", qunName)
            context?.startActivity(intent)
        }

        fun startQunGongGaoActivity(context: Context?, qunid: String, qunName: String) {
            val intent = Intent(context, QunGongGaoActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("qunName", qunName)
            context?.startActivity(intent)
        }

        fun startQunNichengActivity(context: Context?, qunid: String, beizhu: String) {
            val intent = Intent(context, QunNichengActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("beizhu", beizhu)
            context?.startActivity(intent)
        }

        fun startQunManagerActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunManagerActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startQunJinYanActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunJinYanActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startXuanQunJinYanActivity(context: Context?, qunid: String) {
            val intent = Intent(context, XuanQunJinYanActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startQunJinYanMemberActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunJinYanMemberActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startMinGanCiActivity(context: Context?, qunid: String, mgc: String) {
            val intent = Intent(context, MinGanCiActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("mgc", mgc)
            context?.startActivity(intent)
        }

        fun startQunCodeActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunCodeActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
//            context?.let {
//                SaveGroupQrCodePop(it,qunid).showPopupWindow()
//            }
        }

        fun startZhuanRangQunActivity(context: Context?, qunid: String) {
            val intent = Intent(context, ZhuanRangQunActivity::class.java)
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
            val intent = Intent(context, QunGerenPageActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("uid", uid)
            intent.putExtra("source", source)
            intent.putExtra("type", type)
            context?.startActivity(intent)
        }

        fun startQunGerenJInyanTimeActivity(context: Context?, qunid: String, uid: String) {
            val intent = Intent(context, QunGerenJInyanTimeActivity::class.java)
            intent.putExtra("qunid", qunid)
            intent.putExtra("uid", uid)
            context?.startActivity(intent)
        }

        fun startQunGonggaoActivity(context: Context?, qunid: String) {
            val intent = Intent(context, QunGonggaoInfoActivity::class.java)
            intent.putExtra("qunid", qunid)
            context?.startActivity(intent)
        }

        fun startFaQunHongBaoActivity(context: Context?, qunid: String) {
            val intent = Intent(context, FaQunHongBaoActivity::class.java)
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
                if (MeetingFloatService.isServiceRunning && MeetingFloatService.isFloatShow) {
                    AUIAlertBroadDialog(ctx).apply {
                        setTitle("已加入其他会议室")
                        setMessage("您当前正在其他会议室，是否退出当前所在会议室？")
                        setPositiveButton("确认退出") {
                            dismiss()
                            EventBus.getDefault().post(
                                MeetingFloatService.MeetingEvent(
                                    MeetingFloatService.EVENT_BACKGROUND_QUITE,
                                    ownerId
                                )
                            )
//                            MeetingFloatService.stop(ctx)
                        }
                        setNegativeButton("取消") {
                            dismiss()
                        }
                        show()

                    }


                } else {
                    eLogD("zhanglong","roomId==$roomId---isCreate==$isCreate---MeetingFloatService==${MeetingFloatService.isFloatShow}")
                    if (MeetingFloatService.isServiceRunning){
                        EventBus.getDefault().post(
                            MeetingFloatService.MeetingEvent(
                                MeetingFloatService.EVENT_BACKGROUND_QUITE,
                                ownerId
                            )
                        )
                    }
                    MeetingRoomActivity.launch(ctx, roomId, ownerId, title, isCreate, isMute, isMic)
                }

            }
        }

        fun startHaoyouListActivity(context: Context?, uid: String, qunid: String) {
            val intent = Intent(context, HaoyouListActivity::class.java)
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
            val intent = Intent(context, HaoyouApplyActivity::class.java)
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
            val intent = Intent(context, CreateQunActivity::class.java)
            context?.startActivity(intent)
        }

        fun startSearchUserActivity(context: Context?) {
            val intent = Intent(context, SearchUserActivity::class.java)
            context?.startActivity(intent)
        }

        fun startSearchActivity(context: Context?) {
//            val intent = Intent(context, LixinSearchActivity::class.java)
//            context?.startActivity(intent)
            val intent = Intent(
                context,
                SearchPortalActivity::class.java
            )
            context?.startActivity(intent)
        }

        fun startQunListActivity(context: Context?) {
            val intent = Intent(context, QunListActivity::class.java)
            context?.startActivity(intent)
        }

        fun jumpAiCheckResultActivity(context: Context = getAppContext(), id: Int) {
            val intent = Intent(context, AiCheckResultActivity::class.java)
            intent.putExtra("id", id)
            context.startActivity(intent)
        }

        fun jumpAiCheckResultActivity(
            context: Context = getAppContext(),
            bean: AiCheckResultBean?
        ) {
            val intent = Intent(context, AiCheckResultActivity::class.java)
            intent.putExtra("ai_data", bean)
            context.startActivity(intent)
        }

    }
}