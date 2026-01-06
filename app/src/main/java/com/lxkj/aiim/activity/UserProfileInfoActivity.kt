package com.lxkj.aiim.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.compose.navgraph.ProfileFlowHost
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.viewmodel.ProfileViewModel

class UserProfileInfoActivity : BaseSimpleActivity() {
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
                      AppUtil.showToast(this,"onExternalBack")
                    }
                )
            }

        }


    }
}