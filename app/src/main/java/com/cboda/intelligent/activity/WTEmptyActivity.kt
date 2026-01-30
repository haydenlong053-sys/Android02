package com.cboda.intelligent.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.R
import com.cboda.intelligent.compose.navgraph.WTRoute
import com.cboda.intelligent.compose.wallet.WTEmptyScreen

class WTEmptyActivity : BaseSimpleBroadActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_compose_host
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }


    override fun showBgTopTitleLayout() = true

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRootBgColor(R.color.white)
        val container = findViewById<FrameLayout>(R.id.compose_container)
        container.addView(ComposeView(this).apply {
            setContent {
                MaterialTheme {
                    Surface {
                        WTEmptyScreen(
                            onBack = { finish() },
                            onImportClick = { startWalletActivity(WTRoute.importWt()) },
                            onWatchClick = { startWalletActivity(WTRoute.WATCH_WT) },
                            onCreateSuccess = { wallet, name, address ->
                                startWalletActivity(WTRoute.backupMnemonic(wallet, name, address))
                                finish()
                            }
                        )
                    }
                }
            }
        })
    }

    private fun startWalletActivity(route: String) {
        val intent = Intent(this, WalletActivity::class.java)
        intent.putExtra("start_route", route)
        startActivity(intent)
    }
}
