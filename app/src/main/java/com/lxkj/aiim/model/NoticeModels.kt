package com.lxkj.aiim.model

import java.io.Serializable

data class NoticeInfoBean(
    var contentUrl: String?=null,//公告富文本URL
    var endTime: String?=null,
    var forcePopup: Boolean=false,//是否强制弹出 (false:不强制，true: 强制)
    var id: String?=null,//公告ID
    var imageUrl: String?=null,//公告缩略图
    var intervalNumber: Int=0,//弹窗时间间隔
    var intervalUnit: Int=0,//时间单位(1:秒 2:分 3:时 4:天)
    var isRead: Boolean=false,//是否已读 (false:未读，true: 已读)
    var lang: String?=null,//公告语言（cn）
    var noticeTitle: String?=null,//公告标题
    var previewContent: String?=null,//公告简述
    var noticeType: Int=0,//公告类型
    var startTime: String?=null,
    var createTime: String?=null,
    var status: Boolean=false//公告状态（true:正常，false：关闭）
): Serializable