package com.lxkj.aiim.bean

import android.os.Parcel
import android.os.Parcelable
import com.cboda.baselibrary.bean.BaseBroadResponse

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/4/6.
 */
class CreatePayBean : BaseBroadResponse() {
    var zfbResult: String? = null
    var wxMap: WxMapBean? = null
    var isZero: String? = null //是否是0元单  1：是  2：否
    var orderId: String? = null
    var id:String? = null

    class WxMapBean() : Parcelable {
        var appid: String? = null
        var partnerid: String? = null
        var prepayid: String? = null

        //        var package:String?=null
        var noncestr: String? = null
        var sign: String? = null
        var timestamp: String? = null

        constructor(parcel: Parcel) : this() {
            appid = parcel.readString()
            partnerid = parcel.readString()
            prepayid = parcel.readString()
            noncestr = parcel.readString()
            sign = parcel.readString()
            timestamp = parcel.readString()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(appid)
            parcel.writeString(partnerid)
            parcel.writeString(prepayid)
            parcel.writeString(noncestr)
            parcel.writeString(sign)
            parcel.writeString(timestamp)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<WxMapBean> {
            override fun createFromParcel(parcel: Parcel): WxMapBean {
                return WxMapBean(parcel)
            }

            override fun newArray(size: Int): Array<WxMapBean?> {
                return arrayOfNulls(size)
            }
        }

    }
}