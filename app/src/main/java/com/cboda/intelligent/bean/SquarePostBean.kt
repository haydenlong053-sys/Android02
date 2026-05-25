package com.cboda.intelligent.bean

data class SquarePostBean(
    val id: String = "",
    val userId: String = "",
    val imgs: Array<String>? = null,
    val content: String = "",
    val type: String = "",

    val video: String = "",
    val odicStake: String = "",
    val viewCount: String = "",

    val odicStakeLabel: String = "",
    val time: String = "",

    val commentCount: String = "",
    val likeCount: String = "",
    val commentGateTypes: String = "",

    val commentCreditMin: String = "",
    val canComment: String = "",
    val name: String = "",
    val icon: String = "",
    val grade: String = "",

    val creditScore: String = "",
    val gradeStatus: String = "",
    val isLiang: String = "",
    val isvip: String = "",
    val odicStakeStatus: String = "",
    val registDay: String = "",
    val quoteCount: String = "",
    val quote: Duote? = null,
//    "id": 2001,
//    "userId": "uuid",
//"content": "文字",
//"type": "1", // 1图片 2视频
//"imgs": [
//"https://a.jpg"
//], // 图片数组
//"video": "", // 视频URL，type=2时有值
//"odicStake": "2.00", // 该帖质押ODIC金额
//"viewCount": 100,
//"likeCount": 10,
//"commentCount": 3,
//"time": "2026-05-19 14:30:25", // 含时分秒
//"isLike": "0",
//"commentGateTypes": "1,2", // 评论门槛 1普通 2会员 3信用分，逗号可多选
//"commentCreditMin": 60, // 信用分门槛，未选3时可能为空字符串
//"canComment": "1", // 当前用户能否评论 0否 1是
//"commentGateTip": "", // 不能评论时的提示，canComment=0时有值
//"odicStakeStatus": "", //0 质押中 / 1 已退还 / 2 举报没收
//"stakeRefundTime": "", // 退还时间，未退还为空 ""
//"odicStakeLabel": "", // 展示文案，如 已质押 2 ODIC
//"name": "昵称",
//"icon": "https://xxx/a.jpg",
//"sign": "",
//"isvip": "0",
//"isLiang": "0",
//"lianghao": "",
//"idno": "",
//"grade": "0",
//"creditScore": "0",
//"gradeStatus": 0

)


