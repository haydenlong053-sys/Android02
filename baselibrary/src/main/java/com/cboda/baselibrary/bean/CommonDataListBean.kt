package com.cboda.baselibrary.bean

import android.text.TextUtils
import com.xdroid.IndexBar.bean.BaseIndexPinyinBean

class CommonDataListBean:BaseResponse() {
    var dataList: MutableList<CommonBean>? = null
    class CommonBean: BaseIndexPinyinBean() {
        var id:String? = null
        var type:String? = null
        var isSelect = false
        var name:String? = null
        var icon:String? = null
        var sinfo:String? = null
        var state:String? = null
        var emoji:String? = null
        var bei:String? = null
        var userId:String? = null
        var zimu:String? = null
        var sex:String? = null
        var sign:String? = null
        var isZy:String? = null
        var joinTime: String?=null
        var grade:Int?=null

        override fun getTarget(): String {
            return getDisplayName().toString()
        }

        fun getDisplayName(): String? {
            return if (!TextUtils.isEmpty(bei)) {
                bei
            } else if (!TextUtils.isEmpty(name)) {
                name
            } else {
                zimu
            }
        }
    }
}