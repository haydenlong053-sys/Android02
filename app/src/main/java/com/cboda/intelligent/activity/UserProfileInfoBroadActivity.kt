package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.compose.navgraph.ProfileFlowHost
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.viewmodel.ProfileViewModel

class UserProfileInfoBroadActivity : BaseSimpleBroadActivity() {
    private  val profileModel: ProfileViewModel by viewModels()
    override fun getLayoutId(): Int {
        return R.layout.activity_base
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ProfileFlowHost(
                    "",
                    "",
                    emptyList<String>(),
                    onFinalJob = {},
                    onBack = {},
                    onFinalInterest = {},
                    // 当 Compose 流程需要退出时，触发 Fragment/Activity 的返回操作
                    onExternalBack = {
                      AppBroadUtils.showToast(this,"onExternalBack")
                    }
                )
            }

        }


    }
}