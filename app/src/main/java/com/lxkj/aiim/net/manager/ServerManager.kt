package com.lxkj.aiim.net.manager

import android.content.Context
import com.cboda.baselibrary.network.model.NetworkBroadResponse
import com.cboda.baselibrary.network.result.NetworkBroadResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lxkj.aiim.bean.AddMeetingTimeBean
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.lxkj.aiim.bean.CreateRoomBean
import com.lxkj.aiim.bean.KickListBean
import com.lxkj.aiim.bean.MeetingData
import com.lxkj.aiim.bean.NoResultBean
import com.lxkj.aiim.bean.RoomData
import com.lxkj.aiim.bean.UpdateRoomBean
import com.lxkj.aiim.lj.http.UrlBroadConstants
import com.lxkj.aiim.utils.JsonUtils
import com.lxkj.aiim.utils.OKGoHelper
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import org.json.JSONObject

/**
 * 业务接口
 */
object ServerManager {


    fun getUserInfo(context: Context?,userId:String="", completion: (CommonResultBroadBean?) -> Unit){
        val map = mapOf(
            "code" to userId,
        )
        OKGoHelper().post(
            context,
            "/userInfo",
            JSONObject(map).toString(),
            CommonResultBroadBean(),
            callBack = object : OKGoHelper.RequestCallBack {
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
        OkGo.get<String>(UrlBroadConstants.HOST + UrlBroadConstants.GET_ROOM_INFO + roomId).execute(
            object : StringCallback() {
                override fun onSuccess(response: Response<String?>?) {
                    var body = response?.body()

                    val baseBean = JsonUtils.getSuccessJsonBean(body)
                    if (baseBean?.code == "200") {
                        val type = object : TypeToken<NetworkBroadResponse<MeetingData>>() {}.type
                        val roomBean =
                            Gson().fromJson<NetworkBroadResponse<MeetingData>>(body, type)
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


    fun updateMeetingRoomInfo(context: Context?,roomId:String, allMic: Boolean=false, allMute: Boolean=false, completion: (UpdateRoomBean?) -> Unit){
        val map = mapOf(
            "roomId" to roomId,
            "allMic" to allMic,
            "allMute" to allMute
        )
        OKGoHelper().postNoAes(context, UrlBroadConstants.HOST + UrlBroadConstants.UPDATE_MEETING_INFO, Gson().toJson(map),
            UpdateRoomBean(), callBack = object :
                OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? UpdateRoomBean)?.let {
                         completion.invoke(it)


                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {


                }

            })

    }

    fun updateMeetingRoomState(context: Context?,roomId:String, state:String="active", completion: (UpdateRoomBean?) -> Unit){
        val map = mapOf(
            "roomId" to roomId,
            "status" to state,
        )
        OKGoHelper().postNoAes(context, UrlBroadConstants.HOST + UrlBroadConstants.UPDATE_MEETING_INFO, Gson().toJson(map),
            UpdateRoomBean(), callBack = object :
                OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? UpdateRoomBean)?.let {
                        completion.invoke(it)


                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                    completion.invoke(null)


                }

            })

    }

    fun kickUser(context: Context?,roomId:String, userId:String, completion: (NoResultBean?) -> Unit){
        val url = "${UrlBroadConstants.HOST}/v2/users/channels/$roomId/kicked/$userId"
        OKGoHelper().postNoAes(context, url, "",
            NoResultBean(), callBack = object :
                OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? NoResultBean)?.let {
                        completion.invoke(it)
                    }
                }

                override fun onFail(bean: BaseBroadResponse?) {
                        completion.invoke(null)
                }
            })


    }



    fun getKickUserList(context: Context?,roomId: String, completion: (KickListBean?) -> Unit){
         val url  = "${UrlBroadConstants.HOST}/v2/users/channels/$roomId/kicked"

        OKGoHelper().getNoAes(context, url, "",
            KickListBean(), callBack = object :
                OKGoHelper.RequestCallBack {
                override fun onSuccess(bean: BaseBroadResponse?) {
                    (bean as? KickListBean)?.let {
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
                        completion: (AddMeetingTimeBean?) -> Unit){
        val map = mapOf(
            "roomId" to roomId,
            "ownerId" to ownerId,
            "meetingTime" to meetingTime,
            "groupId" to groupId,
        )
          OKGoHelper().postNoAes(context,UrlBroadConstants.HOST+"/"+UrlBroadConstants.ADD_MEETING_TIME,Gson().toJson(map),
              AddMeetingTimeBean(),
              callBack = object :OKGoHelper.RequestCallBack{
                  override fun onSuccess(bean: BaseBroadResponse?) {
                      (bean as? AddMeetingTimeBean)?.let {
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