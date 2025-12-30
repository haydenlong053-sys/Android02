package com.lxkj.aiim.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.compose.navgraph.ProfileFlowHost
import com.lxkj.aiim.lj.UserInfoActivity
import com.lxkj.aiim.utils.AppUtil

class EditProfessionActivity: BaseSimpleActivity() {

    companion object{
        fun toPage(context: Activity, profession:String){
            val intent= Intent(context, EditProfessionActivity::class.java)
            intent.putExtra("profession",profession)
            context.startActivityForResult(intent, UserInfoActivity.EDIT_PROFESSION)
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
        val oldProfession =intent?.getStringExtra("profession")?:""
        setContent {
            MaterialTheme {
                ProfileFlowHost(
                    "edit_job",
                    oldProfession,
                    onFinalJob = {job->
                        val resultData= Intent()
                        resultData.putExtra("profession",job)
                        setResult(UserInfoActivity.EDIT_PROFESSION,resultData)
                        finish()
                    },
                    onBack = {finish()}

                )
            }
        }
    }

}