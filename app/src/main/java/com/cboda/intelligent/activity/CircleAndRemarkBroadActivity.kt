//package com.cboda.intelligent.activity
//
//import android.app.Activity
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.compose.setContent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.viewModels
//import androidx.compose.material3.MaterialTheme
//import com.cboda.intelligent.R
//import com.cboda.intelligent.base.BaseSimpleBroadActivity
//import com.cboda.intelligent.compose.RemarkAndCirclePage
//import com.cboda.intelligent.viewmodel.SocialViewBroadModel
//
//class CircleAndRemarkBroadActivity: BaseSimpleBroadActivity() {
//    override fun getLayoutId(): Int {
//        return R.layout.activity_broad_base
//    }
//
//    override fun showOrHideTitleLayout(): Boolean {
//        return false
//    }
//
//    override fun transparentStatus(): Boolean {
//        return true
//    }
//
//    override fun useStatusBarDarkFont(): Boolean {
//        return true
//    }
//
//    override fun isEnableKeyboard(): Boolean {
//        return true
//    }
//    private val socialViewModel: SocialViewBroadModel by viewModels()
//
//    companion object{
//
//        const val  TYPE_APPLY="type_apply"
//        const val  TYPE_EDIT="type_edit"
//         fun toPage(activity: Activity,startForResult: ActivityResultLauncher<Intent>, nickname: String, selectedTag: String?, type:String?=TYPE_APPLY){
//             val intent = Intent(activity, CircleAndRemarkBroadActivity::class.java)
//             intent.putExtra("nickname", nickname)
//             intent.putExtra("selectedTag", selectedTag)
//             intent.putExtra("type",type)
//             startForResult.launch(intent)
//         }
//        fun toPage(activity: Activity, nickname: String, selectedTag: String?, type:String?=TYPE_APPLY){
//            val intent = Intent(activity, CircleAndRemarkBroadActivity::class.java)
//            intent.putExtra("nickname", nickname)
//            intent.putExtra("selectedTag", selectedTag)
//            intent.putExtra("type",type)
//            activity.startActivity(intent)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val initialRemark = intent.getStringExtra("nickname") ?: ""
//        val initialSelectedTag = intent.getStringExtra("selectedTag")
//        val fromType = intent.getStringExtra("type")?:TYPE_APPLY
//
//        // 页面内已实现 AppBar（居中标题+返回箭头），此处无需设置 Activity 标题
//        setContent {
//            MaterialTheme {
//                RemarkAndCirclePage(
//                    fromType=if (fromType == TYPE_APPLY) 1 else 2,
//                    initialRemark = initialRemark,
//                    vm = socialViewModel,
//                    initialSelectedTag = initialSelectedTag,
//                    onConfirm = { remark, circle ->
//                        setResult(RESULT_OK, Intent().apply {
//                            putExtra("remark", remark)
//                            putExtra("circleTag", circle?.characterTags)
//                            putExtra("circleId", circle?.id?:-1)
//                        })
//                        finish()
//                    },
//                    onBack = {
//                        finish()
//                    }
//                )
//            }
//        }
//
//    }
//}