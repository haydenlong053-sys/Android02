package com.cboda.baselibrary.bean

import com.cboda.baselibrary.utils.MMKVBroadUtils

/**
 * 简述： 常量类
 * Created by ysh on 2023/3/1.
 */
class BroadConstant {

    companion object {
        const val TYPE_ALL_INT = 0
        const val TYPE_PIC = 1
        const val TYPE_VIDEO = 2
        //测试用
        const val TEST_IMAGE = "https://img0.baidu.com/it/u=887570163,3972811143&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1679418000&t=86adc373ec0857a073593c4934843bc1"
        const val TEST_IMAGE1 = "https://img0.baidu.com/it/u=3898093452,3993207433&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1684861200&t=abd02adfd518ffb4f62b54bb98b2ecfb"

        //正式
        const val TX_APP_ID = 1600054361

        const val TX_IM_APP_ID = 1600054361

        const val SECRETKEY = "d93728fe20f2cefd8cfe3c53f9ef6e3d44fc762903278c2dc752fb1858fa0fc1"

        //90 {"msg":"操作成功","uid":"6b148bfe7b294be09c625c4136934595","code":200,"rytoken":"eJw1js0OgjAQhN*lVw3ZlrJQEk*GwMFwQYJXC61ujfIjMRrju0tAjzPzzWTebL8rPPPsaDAsRpARwHr2HmZgMRMesEXfm8ux66hhMUeACUSBS0KNuY1kaS6g5jLS1oRaKKkNqBpFUEvuo-JloIL-Gp0muO2BTJaU1jiHFeVD3-u6qFyiZJ675JWuzoey3aaZlZtfcaTrdJSHAkMhBYfPF-DhN1A_","success":true,"isPerfect":"1","apptoken":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiI2YjE0OGJmZTdiMjk0YmUwOWM2MjVjNDEzNjkzNDU5NSJ9.yMlq3plUy5mHiYx4kuK-t6-IYfqIjqpvDfTGJRRVPnA","zhuxiaostate":"0"}
        // 91 {"msg":"操作成功","uid":"e509bedef9d64dd0b54c92c1f95ad762","code":200,"rytoken":"eJw1jt0KgkAUhN9lryOOi3vWFbopEtOCoh*qu9VdYxNNViklevdE63JmvhnmTQ7r-VS3lbGa*AiuBzAZvKe2xCd0CmTUtcplVRlFfAcBehApjolRumxMZoaCZiASrXQmFLpKQcLcVNDUyQSTiiP9r5lbD2NsvWMUSq9j29AuzlfzwE0M6T0Put0yKuti3rxaGZxWl9mv2JiiP*pwipwyFPzzBUqVOKI_","success":true,"isPerfect":"1","apptoken":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiJlNTA5YmVkZWY5ZDY0ZGQwYjU0YzkyYzFmOTVhZDc2MiJ9.md-akDwNwy1ijLMGPgct638JErqYwc46GWMPK3bSvvE","zhuxiaostate":"0"}

        const val WORD_GROUP_ID = "12344321"

        const val PUBLIC_CHAT_GROUP_ID = "@TGS#_@TGS#cIGTTJIM62CZ"


        const val RESULT_OK = "200"

        var userId: String? = null
        fun getUid(): String? {
            if (userId.isNullOrEmpty()) {
                userId = MMKVBroadUtils.getUserId()
            }
            return userId
        }

        var realUserId: String? = null
        fun getRealUid(): String? {
            if (realUserId.isNullOrEmpty()) {
                realUserId = MMKVBroadUtils.getRealUserId()
            }
            return realUserId
        }
    }
}