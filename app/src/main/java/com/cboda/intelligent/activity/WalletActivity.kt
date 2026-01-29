package com.cboda.intelligent.activity

import android.os.Bundle
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.compose.navgraph.WTFlowHost
import com.cboda.intelligent.compose.navgraph.WTRoute

class WalletActivity: BaseSimpleBroadActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_compose_host
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return false
    }

    override fun isEnableImmerBar(): Boolean {
        return false
    }

    override fun fitsSystemWindows(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val startRoute = intent.getStringExtra("start_route") ?: WTRoute.CREATE_WT

        val container = findViewById<FrameLayout>(R.id.compose_container)
        container.addView(ComposeView(this).apply {
            setContent {
                MaterialTheme {
                    Surface {
                        WTFlowHost(
                            startDestination = startRoute,
                            userId = MMKVBroadUtils.getUserAppToken() ?: "",
                            onBack = { finish() }
                        )
                    }
                }
            }
        })
    }
}
