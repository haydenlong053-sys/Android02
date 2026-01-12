package com.cboda.intelligent.holder

import android.content.Context
import com.cboda.chatui.widget.popup.OpenMeetingBroadDialog
import com.cboda.intelligent.activity.MeetingCreateBroadActivity
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.intelligent.utils.OKGoBroadHelper
import org.json.JSONObject

object MeetingBroadHolder {

    fun openMeetingDialog(
        context: Context?,
        idNo: String,
        groupId: String,
        okGoHelper: OKGoBroadHelper,
        okContext: Context?
    ){
        okContext?.let {
            val map = mapOf(
                "id" to (groupId ?: ""),
            )
            okGoHelper.post(it,"/groupInfo",JSONObject(map).toString(),
                CommonResultBroadBean(), callBack = object : OKGoBroadHelper.RequestCallBack {
                    override fun onSuccess(bean: BaseBroadResponse?) {
                        (bean as? CommonResultBroadBean)?.let {
                            if (it.code == "200") {
                                context?.let {uiContext->
                                    MeetingCreateBroadActivity.launch(uiContext,groupId,idNo,it.data?.name?:"")
//                                    createMeetingDialog(uiContext,"${it.data?.name?:""} 会议室",{
//                                            openMic, openMute,meetingTitle ->
//                                        val params = HashMap<String,Any>()
//                                        params["groupId"] = groupId
//                                        params["ownerId"]=idNo
//                                        params["type"]=0
//                                        params["roomName"]=meetingTitle
//                                        params["allMute"]=openMute
//                                        params["allMic"]=openMic
//                                        okGoHelper.postNoAes(
//                                            okContext,
//                                            UrlBroadBroadConstants.HOST+UrlBroadBroadConstants.GET_UNIQUE_ROOM_ID,
//                                            Gson().toJson(params),
//                                            UniqueRoomBroadBean(),
//                                            hasBaseUrl = true,
//                                            callBack = object : OKGoBroadHelper.RequestCallBack{
//                                                override fun onSuccess(bean: BaseBroadResponse?) {
//                                                    (bean as? UniqueRoomBroadBean)?.let {
//                                                        val meetingMessage=
//                                                            MeetingInviteMessageContent()
//                                                                .apply {
//                                                                    meetingId=it?.data?.roomId?:""
//                                                                    idno=idNo
//                                                                    title=meetingTitle
//                                                                    desc="我在会议室等你，快进来吧"
//                                                                    isAllMic = openMic
//                                                                    isAllMute=openMute
//
//                                                                }
//                                                        EventBus.getDefault().post(
//                                                            MyBroadEvent(
//                                                                "huiyishi",
//                                                                Gson().toJson(meetingMessage),
//                                                                ""
//                                                            )
//                                                        )
//                                                        JumpBroadUtils.startQunMeetingActivity(context,it.data?.roomId?:"",idNo,meetingTitle,true,
//                                                            openMute,openMic)
//
//                                                    }
//                                                    (bean as? CreateRoomBroadBean)?.let { t->
//                                                        val meetingMessage=
//                                                            MeetingInviteMessageContent()
//                                                                .apply {
//                                                                    meetingId=t?.data?.roomId?:""
//                                                                    idno=idNo
//                                                                    title="邀请你加入会议室"
//                                                                    desc="我在会议室等你，快进来吧"
//                                                                    isAllMic = t?.data?.payload?.allMic?:true
//                                                                    isAllMute=t?.data?.payload?.allMute?:false
//
//                                                                }
//                                                        EventBus.getDefault().post(
//                                                            MyBroadEvent(
//                                                                "huiyishi",
//                                                                Gson().toJson(meetingMessage),
//                                                                ""
//                                                            )
//                                                        )
//                                                    }
//                                                }
//
//                                                override fun onFail(bean: BaseBroadResponse?) {
//                                                    ToastBroadUtils.show(bean?.msg?:"创建会议失败")
//
//                                                }
//
//
//                                            }
//
//                                        )
//                                        //请求创建会议的接口
////                                        OkHttpBroadHelper.getInstance().post_json(uiContext,
////                                            UrlBroadBroadConstants.HOST+ UrlBroadBroadConstants.CREATE_ROOM,
////                                            params,
////                                            object : BaseBroadCallback<ResultBroadBean>() {
////                                                override fun onBeforeRequest(request: Request?) {
////                                                }
////
////                                                override fun onFailure(
////                                                    request: Request?,
////                                                    e: Exception?
////                                                ) {
////                                                }
////
////                                                override fun onResponse(response: Response?) {
////
////                                                }
////
////                                                override fun onSuccess(
////                                                    response: Response?,
////                                                    t: ResultBroadBean?
////                                                ) {
////
////
////                                                }
////
////                                                override fun onError(
////                                                    response: Response?,
////                                                    code: Int,
////                                                    e: Exception?
////                                                ) {
////                                                }
////
////                                            }
////                                            )
//
//
//
//
//
//                                    }).showPopupWindow()
                                }
                            }
                        }
                    }

                    override fun onFail(bean: BaseBroadResponse?) {
                    }

                })
        }



    }

    private  fun  createMeetingDialog(context: Context,meetingTitle:String,openClick: (openMic: Boolean, openMute: Boolean,meetingTitle: String) -> Unit?): OpenMeetingBroadDialog{
        return OpenMeetingBroadDialog(context,meetingTitle,true,false,openClick)
    }
}