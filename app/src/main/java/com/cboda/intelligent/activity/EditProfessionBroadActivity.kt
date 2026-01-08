package com.cboda.intelligent.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.compose.navgraph.ProfileFlowHost

class EditProfessionBroadActivity: BaseSimpleBroadActivity() {

    companion object{
        fun toPage(context: Activity, profession:String){
            val intent= Intent(context, EditProfessionBroadActivity::class.java)
            intent.putExtra("profession",profession)
            context.startActivityForResult(intent, UserInfoBroadActivity.EDIT_PROFESSION)
        }
    }
    override fun getLayoutId(): Int {
        return R.layout.activity_broad_base
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val oldProfession =intent?.getStringExtra("profession")?:""
        setContent {
            MaterialTheme {
                ProfileFlowHost(
                    "edit_job",
                    oldProfession,
                    onFinalJob = {job->
                        val resultData= Intent()
                        resultData.putExtra("profession",job)
                        setResult(UserInfoBroadActivity.EDIT_PROFESSION,resultData)
                        finish()
                    },
                    onBack = {finish()}

                )
            }
        }
    }

}