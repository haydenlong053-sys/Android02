package com.base.commonlibrary.bean

import java.io.Serializable

class GlobalNoticeBean : Serializable {
    var contentUrl: String? = null
    var endTime: String? = null
    var forcePopup: Boolean = false
    var id: String? = null
    var imageUrl: String? = null
    var intervalNumber: Int = 0
    var intervalUnit: Int = 0
    var isRead: Boolean = false
    var lang: String? = null
    var noticeTitle: String? = null
    var previewContent: String? = null
    var noticeType: Int = 0
    var startTime: String? = null
    var createTime: String? = null
    var status: Boolean = false
    var jumpAppAction=0
    constructor()
    constructor(id: String?, createTime: String?, noticeTitle: String?) {
        this.id = id
        this.createTime = createTime
        this.noticeTitle = noticeTitle
    }




    override fun toString(): String {
        return "GlobalNoticeBean(contentUrl=$contentUrl, endTime=$endTime, forcePopup=$forcePopup, id=$id, imageUrl=$imageUrl, intervalNumber=$intervalNumber, intervalUnit=$intervalUnit, isRead=$isRead, lang=$lang, noticeTitle=$noticeTitle, previewContent=$previewContent, noticeType=$noticeType, startTime=$startTime, createTime=$createTime, status=$status)"
    }
}


