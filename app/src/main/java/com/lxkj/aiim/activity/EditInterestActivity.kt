package com.lxkj.aiim.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.cboda.baselibrary.utils.eLogD
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.compose.navgraph.ProfileFlowHost
import com.lxkj.aiim.lj.UserInfoActivity
import com.lxkj.aiim.utils.AppUtil

class EditInterestActivity: BaseSimpleActivity() {

    companion object{
        fun toPage(context: Activity, interestList:List<String>){
            val intent= Intent(context, EditInterestActivity::class.java)
            intent.putStringArrayListExtra("interest", ArrayList(interestList))
            context.startActivityForResult(intent, UserInfoActivity.EDIT_INTEREST)
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
        val oldInterestList =intent?.getStringArrayListExtra("interest")
        eLogD("看看数据","oldin:${oldInterestList.toString()}")
        setContent {
            MaterialTheme {
                ProfileFlowHost(
                    "edit_interests",
                    "",
                    interestList = oldInterestList?:emptyList(),
                    onFinalInterest = {
                        val resultData= Intent()
                        resultData.putStringArrayListExtra("interest", ArrayList(it))
                        setResult(UserInfoActivity.EDIT_INTEREST,resultData)
                        finish()
                    },
                    onBack = {finish()}

                )
            }
        }
    }

}