package com.cboda.intelligent.viewmodel

import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.toJson2
import com.cboda.intelligent.utils.AppBroadUtils
import com.freddy.im.http.ImAppHttpExample
import com.freddy.im.http.model.im.AppVersionRes
import com.freddy.im.listener.OnHttpImListener

/**
 * 版本更新：GET {@code im/app/version}（文档 appVersion）。
 */
class VersionUpdateViewBroadModel : BaseViewModel() {

    fun versionUpdate(): SingleLiveBroadData<AppVersionRes> {
        val liveData = SingleLiveBroadData<AppVersionRes>()
        val versionName = AppBroadUtils.getVersionName(getAppContext())
        eLogD("versionUpdate", "platform=android version=$versionName")
        ImAppHttpExample.requestGetAppVersion(
            "android",
            versionName,
            object : OnHttpImListener<AppVersionRes> {
                override fun onHttpImSuccess(result: AppVersionRes) {
                    eLogD("versionUpdate", toJson2(result))
                    liveData.postValue(result)
                }
            },
            null,
        )
        return liveData
    }
}
