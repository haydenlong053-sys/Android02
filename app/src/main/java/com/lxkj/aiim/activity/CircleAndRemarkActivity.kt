package com.lxkj.aiim.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.compose.RemarkAndCirclePage
import com.lxkj.aiim.viewmodel.SocialViewModel

class CircleAndRemarkActivity: BaseSimpleActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_base
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    override fun useStatusBarDarkFont(): Boolean {
        return true
    }

    override fun isEnableKeyboard(): Boolean {
        return true
    }
    private val socialViewModel: SocialViewModel by viewModels()

    companion object{

        const val  TYPE_APPLY="type_apply"
        const val  TYPE_EDIT="type_edit"
         fun toPage(activity: Activity,startForResult: ActivityResultLauncher<Intent>, nickname: String, selectedTag: String?, type:String?=TYPE_APPLY){
             val intent = Intent(activity, CircleAndRemarkActivity::class.java)
             intent.putExtra("nickname", nickname)
             intent.putExtra("selectedTag", selectedTag)
             intent.putExtra("type",type)
             startForResult.launch(intent)
         }
        fun toPage(activity: Activity, nickname: String, selectedTag: String?, type:String?=TYPE_APPLY){
            val intent = Intent(activity, CircleAndRemarkActivity::class.java)
            intent.putExtra("nickname", nickname)
            intent.putExtra("selectedTag", selectedTag)
            intent.putExtra("type",type)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialRemark = intent.getStringExtra("nickname") ?: ""
        val initialSelectedTag = intent.getStringExtra("selectedTag")
        val fromType = intent.getStringExtra("type")?:TYPE_APPLY

        // 页面内已实现 AppBar（居中标题+返回箭头），此处无需设置 Activity 标题
        setContent {
            MaterialTheme {
                RemarkAndCirclePage(
                    fromType=if (fromType == TYPE_APPLY) 1 else 2,
                    initialRemark = initialRemark,
                    vm = socialViewModel,
                    initialSelectedTag = initialSelectedTag,
                    onConfirm = { remark, circle ->
                        setResult(RESULT_OK, Intent().apply {
                            putExtra("remark", remark)
                            putExtra("circleTag", circle?.characterTags)
                            putExtra("circleId", circle?.id?:-1)
                        })
                        finish()
                    },
                    onBack = {
                        finish()
                    }
                )
            }
        }

    }
}