package com.lxkj.aiim.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.compose.navgraph.ProfileFlowHost
import com.lxkj.aiim.lj.UserInfoActivity
import com.lxkj.aiim.utils.AppUtil

class EditAddressActivity: BaseSimpleActivity() {

    companion object{
        fun toPage(context: Activity, profession:String){
            val intent= Intent(context, EditAddressActivity::class.java)
            intent.putExtra("address",profession)
            context.startActivityForResult(intent, UserInfoActivity.EDIT_ADDRESS)
        }
    }
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
        val oldProfession =intent?.getStringExtra("address")?:""
        setContent {
            MaterialTheme {
                ProfileFlowHost(
                    "edit_address",
                    oldProfession,
                    onFinalJob = {job->
                        val resultData= Intent()
                        resultData.putExtra("address",job)
                        setResult(UserInfoActivity.EDIT_ADDRESS,resultData)
                        finish()
                    },
                    onBack = {finish()}

                )
            }
        }
    }

}