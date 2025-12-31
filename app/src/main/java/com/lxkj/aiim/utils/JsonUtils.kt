package com.lxkj.aiim.utils

import com.cboda.baselibrary.bean.BaseResponse
import com.cboda.baselibrary.bean.CommonDataListBean
import com.cboda.baselibrary.bean.CommonResultBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lxkj.aiim.bean.*

class JsonUtils {
    companion object {
        private val gson = Gson()
        fun getSuccessJsonBean(json: String?): BaseResponse? {
            var bean: BaseResponse? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, BaseResponse::class.java)
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

        fun getCommonResultBean(json: String?): CommonResultBean? {
            var bean: CommonResultBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, CommonResultBean::class.java)
                } catch (e: Exception) {
                    AppUtil.debug("JsonError", "getCommonResultBean--> $e")
                }
            }
            return bean
        }


        fun getCommonDataListBean(json: String?): CommonDataListBean? {
            var bean: CommonDataListBean? = null
            json?.apply {
                try {
                    bean = gson.fromJson(this, CommonDataListBean::class.java)
                    // 使用 TypeToken 保留泛型信息，避免 LinkedTreeMap 类型转换错误
//                    val type = object : TypeToken<CommonDataListBean>() {}.type
//                    bean = gson.fromJson(this, type) as? CommonDataListBean
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