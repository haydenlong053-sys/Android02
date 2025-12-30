package com.lxkj.aiim.holder

import android.content.Context
import androidx.lifecycle.*
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.google.gson.Gson
import com.lxkj.aiim.bean.ProvinceAndCityBean
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * 简述：选择城市holder
 */
class SelectCityHolder(private val mActivity: Context?) :
    LifecycleObserver {
    private var bf: BufferedReader? = null
    private var mPvProvinceAndCity: OptionsPickerView<Any>? = null
    var callBack: PreviewBack? = null
    private var mProvinceList: List<ProvinceAndCityBean.ProvinceBean>? = null
    private var mArrayProvince: ArrayList<ProvinceAndCityBean.ProvinceBean> = ArrayList()
    private var mArrayProvinceStr: ArrayList<String?> = ArrayList()
    private var provinceCityString = ArrayList<ArrayList<ProvinceAndCityBean.ProvinceBean.CityBean>>()
    private var provinceCityStr = ArrayList<ArrayList<String?>>()
    private var mAreaBigList: ArrayList<ArrayList<ArrayList<ProvinceAndCityBean.ProvinceBean.CityBean.AreaBean>>> = ArrayList()
    private var mAreaBigListStr: ArrayList<ArrayList<ArrayList<String?>>> = ArrayList()

    init {
        initProvinceAndCity()
    }

    private fun initProvinceAndCity() {
        Thread {
            val cityJson = getJson(mActivity)
            val provinceAndCityBean = Gson().fromJson(
                cityJson,
                ProvinceAndCityBean::class.java
            )
            mProvinceList = provinceAndCityBean.data
            mProvinceList?.forEachIndexed { _, provinceBean ->
                mArrayProvince.add(provinceBean)
                mArrayProvinceStr.add(provinceBean.regionName)
                val mArrayCity = ArrayList<ProvinceAndCityBean.ProvinceBean.CityBean>()
                val mArrayCityStr = ArrayList<String?>()
                val mAreaBeanList = ArrayList<ArrayList<ProvinceAndCityBean.ProvinceBean.CityBean.AreaBean>>()
                val mAreaBeanListStr = ArrayList<ArrayList<String?>>()

                provinceBean.children?.forEachIndexed { _, cityBean ->
                    mArrayCity.add(cityBean)
                    mArrayCityStr.add(cityBean.regionName)
                    val mAreaList = ArrayList<ProvinceAndCityBean.ProvinceBean.CityBean.AreaBean>()
                    val mAreaListStr = ArrayList<String?>()
                    cityBean.children?.forEachIndexed { _, areaBean ->
                        mAreaList.add(areaBean)
                        mAreaListStr.add(areaBean.regionName)
                    }
                    mAreaBeanList.add(mAreaList)
                    mAreaBeanListStr.add(mAreaListStr)
                }

                mAreaBigListStr.add(mAreaBeanListStr)
                mAreaBigList.add(mAreaBeanList)
                provinceCityString.add(mArrayCity)
                provinceCityStr.add(mArrayCityStr)

            }

        }.start()
    }

    fun show() {
        if (mPvProvinceAndCity == null) {
            mPvProvinceAndCity = OptionsPickerBuilder(
                mActivity
            ) { options1, options2, options3, v ->
                callBack?.onPreviewBack("${mArrayProvince[options1].regionName}${provinceCityString[options1][options2].regionName}${mAreaBigList[options1][options2][options3].regionName}",
                    mArrayProvince[options1].regionName,provinceCityString[options1][options2].regionName,
                    mAreaBigList[options1][options2][options3].regionName )
            }.setCyclic(false, false, false).setOutSideCancelable(true)
                .setLineSpacingMultiplier(2.0f).setCancelText("取消").setSubmitText("确定").build()

            mPvProvinceAndCity?.setPicker(
                mArrayProvinceStr as List<*>?,
                provinceCityStr as List<MutableList<Any>>?,
                mAreaBigListStr as List<MutableList<MutableList<Any>>>?
            )
        }

        mPvProvinceAndCity?.show()


    }

    interface PreviewBack {
        fun onPreviewBack(detailAddress:String?,province:String?,city: String?,discrict:String?)
    }

    private fun getJson(context: Context?): String {
        //将json数据变成字符串
        val stringBuilder = StringBuilder()
        try {
            //获取assets资源管理器
            val assetManager = context?.assets
            //通过管理器打开文件并读取
            bf = BufferedReader(
                InputStreamReader(
                    assetManager?.open("city_json.txt")
                )
            )
            var line: String?
            while (bf?.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        bf?.close()
        bf = null
        callBack = null
    }
}