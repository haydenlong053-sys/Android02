package com.freddy.webtrc.floatcall

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.freddy.webtrc.activity.CallActivity
import com.freddy.webtrc.utils.CallUtil

/**
 * 点击悬浮窗后的中转页：按保活会话重新打开 [CallActivity]。
 */
class CallDummyActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!CallSessionHolder.active && !CallUtil.isRoomConnected()) {
            finish()
            return
        }
        val intent = Intent(this, CallActivity::class.java).apply {
            if (CallSessionHolder.active) {
                CallSessionHolder.fillIntent(this)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
