package com.lxkj.aiim.utils

import com.cboda.baselibrary.bean.BaseBroadResponse
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lxkj.aiim.bean.*

class JsonUtils {
    companion object {
        private val gson = Gson()
        fun getSuccessJsonBean(json: String?): BaseBroadResponse? {
            var bean: BaseBroadResponse? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, BaseBroadResponse::class.java)
                } catch (e: Exception) {
                    AppUtil.debug("JsonError", "getSuccessJsonBean--> $e")
                }
            }
            return bean
        }

        fun getSuccessJsonBean2(json: String?): BaseResponse2? {
            var bean: BaseResponse2? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, BaseResponse2::class.java)
                } catch (e: Exception) {
                    AppUtil.debug("JsonError", "getSuccessJsonBean--> $e")
                }
            }
            return bean
        }



        fun getNoResultBean(json: String?): NoResultBean? {
            var bean: NoResultBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, NoResultBean::class.java)
                } catch (e: Exception) {
                    AppUtil.debug("JsonError", "getNoResultBean--> $e")
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
                    AppUtil.debug("JsonError", "getCommonResultBean--> $e")
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
                    AppUtil.debug("JsonError", "getCommonDataListBean--> $e")
                }
            }
            return bean
        }


        fun getUrlBean(json: String?): UrlBean? {
            var bean: UrlBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, UrlBean::class.java)
                } catch (e: Exception) {
                    AppUtil.debug("JsonError", "getUrlBean--> $e")
                }
            }
            return bean
        }

        fun getUrlsBean(json: String?): UrlsBean? {
            var bean: UrlsBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, UrlsBean::class.java)
                } catch (e: Exception) {
                    AppUtil.debug("JsonError", "getUrlsBean--> $e")
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