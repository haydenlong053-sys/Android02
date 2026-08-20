package com.cboda.intelligent.net.manager

import android.content.Context
import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.network.model.NetworkBroadResponse
import com.google.gson.reflect.TypeToken
import com.cboda.intelligent.bean.AddMeetingTimeBroadBean
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.intelligent.bean.KickListBroadBean
import com.cboda.intelligent.bean.MeetingData
import com.cboda.intelligent.bean.NoResultBroadBean
import com.cboda.intelligent.bean.UpdateRoomBroadBean
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.JsonBroadUtils
import com.cboda.intelligent.utils.OKGoBroadHelper
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import org.json.JSONObject

/**
 * 业务接口
 */
object ServerBroadManager {


    fun getUserInfo(context: Context?,userId:String="", completion: (CommonResultBroadBean?) -> Unit){
        val map = mapOf(
            "code" to userId,
        )
        OKGoBroadHelper().post(
            context,
            "/userInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            callBack = object : OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? CommonResultBroadBean)?.let {
                         completion.invoke(it)

                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {

                }

            }


        )
    }


    fun getMeetingRoomInfo(roomId:String,completion: (MeetingData?) -> Unit){
        OkGo.get<String>(UrlBroadBroadConstants.HOST + UrlBroadBroadConstants.GET_ROOM_INFO + roomId).execute(
            object : StringCallback() {
                override fun onSuccess(response: Response<String?>?) {
                    var body = response?.body()

                    val baseBean = JsonBroadUtils.getSuccessJsonBean(body)
                    if (baseBean?.code == "200") {
                        val type = object : TypeToken<NetworkBroadResponse<MeetingData>>() {}.type
                        val roomBean =
                            GsonUtils.getGson().fromJson<NetworkBroadResponse<MeetingData>>(body, type)
                        roomBean.data?.let {
                             completion.invoke(it)
                        }


                    }

                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    completion.invoke(null)

                }

            }
        )

    }


    fun updateMeetingRoomInfo(context: Context?,roomId:String, allMic: Boolean=false, allMute: Boolean=false, completion: (UpdateRoomBroadBean?) -> Unit){
        val map = mapOf(
            "roomId" to roomId,
            "allMic" to allMic,
            "allMute" to allMute
        )
        OKGoBroadHelper().postNoAes(context, UrlBroadBroadConstants.HOST + UrlBroadBroadConstants.UPDATE_MEETING_INFO, GsonUtils.getGson().toJson(map),
            UpdateRoomBroadBean(), callBack = object :
                OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? UpdateRoomBroadBean)?.let {
                         completion.invoke(it)


                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {


                }

            })

    }

    fun updateMeetingRoomState(context: Context?,roomId:String, state:String="active", completion: (UpdateRoomBroadBean?) -> Unit){
        val map = mapOf(
            "roomId" to roomId,
            "status" to state,
        )
        OKGoBroadHelper().postNoAes(context, UrlBroadBroadConstants.HOST + UrlBroadBroadConstants.UPDATE_MEETING_INFO, GsonUtils.getGson().toJson(map),
            UpdateRoomBroadBean(), callBack = object :
                OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? UpdateRoomBroadBean)?.let {
                        completion.invoke(it)


                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                    completion.invoke(null)


                }

            })

    }

    fun kickUser(context: Context?,roomId:String, userId:String, completion: (NoResultBroadBean?) -> Unit){
        val url = "${UrlBroadBroadConstants.HOST}/v2/users/channels/$roomId/kicked/$userId"
        OKGoBroadHelper().postNoAes(context, url, "",
            NoResultBroadBean(), callBack = object :
                OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? NoResultBroadBean)?.let {
                        completion.invoke(it)
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                        completion.invoke(null)
                }
            })


    }



    fun getKickUserList(context: Context?,roomId: String, completion: (KickListBroadBean?) -> Unit){
         val url  = "${UrlBroadBroadConstants.HOST}/v2/users/channels/$roomId/kicked"

        OKGoBroadHelper().getNoAes(context, url, "",
            KickListBroadBean(), callBack = object :
                OKGoBroadHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? KickListBroadBean)?.let {
                        completion.invoke(it)
                    }
                }
                override fun onFail(bean: BaseBroadResponse?) {
                    completion.invoke(null)
                }
            }
        )


    }


    /**
     * 添加会议时长/续费
     */
    fun  addMeetingTime(context: Context?,groupId:String,ownerId:String,meetingTime:String,roomId: String,
                        completion: (AddMeetingTimeBroadBean?) -> Unit){
        val map = mapOf(
            "roomId" to roomId,
            "ownerId" to ownerId,
            "meetingTime" to meetingTime,
            "groupId" to groupId,
        )
          OKGoBroadHelper().postNoAes(context,UrlBroadBroadConstants.HOST+"/"+UrlBroadBroadConstants.ADD_MEETING_TIME,GsonUtils.getGson().toJson(map),
              AddMeetingTimeBroadBean(),
              callBack = object :OKGoBroadHelper.RequestCallBack{
                  override fun onSuccess(bean: BaseBroadResponse?) {
                      (bean as? AddMeetingTimeBroadBean)?.let {
                          completion.invoke(it)
                      }

                  }

                  override fun onFail(bean: BaseBroadResponse?) {
                      completion.invoke(null)
                  }

              }
              )
    }





}