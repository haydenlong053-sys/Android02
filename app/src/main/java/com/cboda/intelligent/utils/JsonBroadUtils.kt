package com.cboda.intelligent.utils

import com.blankj.utilcode.util.GsonUtils
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.intelligent.bean.*

class JsonBroadUtils {
    companion object {
        private val gson = GsonUtils.getGson()
        fun getSuccessJsonBean(json: String?): BaseBroadResponse? {
            var bean: BaseBroadResponse? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, BaseBroadResponse::class.java)
                } catch (e: Exception) {
                    AppBroadUtils.debug("JsonError", "getSuccessJsonBean--> $e")
                }
            }
            return bean
        }

        fun getSuccessJsonBean2(json: String?): BaseBroadResponse2? {
            var bean: BaseBroadResponse2? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, BaseBroadResponse2::class.java)
                } catch (e: Exception) {
                    AppBroadUtils.debug("JsonError", "getSuccessJsonBean--> $e")
                }
            }
            return bean
        }


        fun getNoResultBean(json: String?): NoResultBroadBean? {
            var bean: NoResultBroadBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, NoResultBroadBean::class.java)
                } catch (e: Exception) {
                    AppBroadUtils.debug("JsonError", "getNoResultBean--> $e")
                }
            }
            return bean
        }

        fun getCommonResultBean(json: String?): CommonResultBroadBean? {
            var bean: CommonResultBroadBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, CommonResultBroadBean::class.java)
                } catch (e: Exception) {
                    AppBroadUtils.debug("JsonError", "getCommonResultBean--> $e")
                }
            }
            return bean
        }


        fun getCommonDataListBean(json: String?): CommonDataListBroadBean? {
            var bean: CommonDataListBroadBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, CommonDataListBroadBean::class.java)
                    // 使用 TypeToken 保留泛型信息，避免 LinkedTreeMap 类型转换错误
//                    val type = object : TypeToken<CommonDataListBroadBean>() {}.type
//                    bean = gson.fromJson(this, type) as? CommonDataListBroadBean
                } catch (e: Exception) {
                    AppBroadUtils.debug("JsonError", "getCommonDataListBean--> $e")
                }
            }
            return bean
        }


        fun getUrlBean(json: String?): UrlBroadBean? {
            var bean: UrlBroadBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, UrlBroadBean::class.java)
                } catch (e: Exception) {
                    AppBroadUtils.debug("JsonError", "getUrlBean--> $e")
                }
            }
            return bean
        }

        fun getUrlsBean(json: String?): UrlsBroadBean? {
            var bean: UrlsBroadBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, UrlsBroadBean::class.java)
                } catch (e: Exception) {
                    AppBroadUtils.debug("JsonError", "getUrlsBean--> $e")
                }
            }
            return bean
        }

        /**
         * 基础格式检查
         */
        fun isBasicJsonFormat(text: String): Boolean {
            return text.startsWith("{") && text.endsWith("}") ||
                    text.startsWith("[") && text.endsWith("]")
        }


    }
}