package com.lxkj.aiim.lj

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.cboda.intelligent.databinding.ActivitySecurityAuthEditBinding
import com.lxkj.aiim.dialog.pop.CommonSelectPop
import com.lxkj.aiim.lj.bean.CommonSelectBean
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.lj.utils.isSubmitCanGo
import com.lxkj.aiim.viewmodel.SafeCenterViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.getValue

//密保验证设置页面
class SecurityAuthEditActivity : BaseSimple2Activity() {
    val idDiy = "b7ebf2de09614b9faf14b9c0d6a956df"
    private val viewModel: SafeCenterViewModel by viewModels()

    private var mTableList: List<CommonSelectBean>? = null
    private var selectBean: CommonSelectBean? = null
    private var mBind: ActivitySecurityAuthEditBinding? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_security_auth_edit
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivitySecurityAuthEditBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = binding as? ActivitySecurityAuthEditBinding
        getToolbarTitle()?.text = getResources().getString(R.string.设置密保问题)
        initListener()
        initData()
    }

    private fun initListener() {
        mBind?.mLlQuestionTable?.setOnClickListener {
            if (mTableList?.isNotEmpty() == true) {
                val commonSelectPop = CommonSelectPop(
                    this@SecurityAuthEditActivity,
                    getString(R.string.请选择密保问题),
                    mTableList!!
                ) { mBean ->
                    selectBean = mBean
                    mBind?.mTvQuestionTable?.text =
                        selectBean?.title
                    if (idDiy == selectBean?.id) {
                        mBind?.mEtDiyQuestion?.isVisible =
                            true
                        mBind?.mTvDiyDes?.isVisible = true
                    } else {
                        mBind?.mEtDiyQuestion?.isVisible =
                            false
                        mBind?.mTvDiyDes?.isVisible = false
                    }
                    checkGo()
                }
                commonSelectPop.showPopupWindow()
            }
        }

        mBind?.mTvCancel?.setOnClickListener {
            finish()
        }

        mBind?.mTvSubmit?.setOnClickListener {
            val questionTableStr = mBind?.mTvQuestionTable?.text?.trim().toString()
            val questionDiyTableStr = mBind?.mEtDiyQuestion?.text?.trim().toString()
            val answerStr = mBind?.mEtAnswer?.text?.trim().toString()

            if (idDiy == selectBean?.id && questionDiyTableStr.isEmpty()) {
                return@setOnClickListener
            }
            if (questionTableStr.isEmpty() || answerStr.isEmpty()) {
                return@setOnClickListener
            }

            if (idDiy == selectBean?.id) {
                lifecycleScope.launch {
                    viewModel.goSecurityInfo(selectBean?.id, questionDiyTableStr, answerStr)
                        .observe(this@SecurityAuthEditActivity) {
                            goNest()

                        }
                }
            } else {
                lifecycleScope.launch {
                    viewModel.goSecurityInfo(selectBean?.id, selectBean?.title, answerStr)
                        .observe(this@SecurityAuthEditActivity) {
                            goNest()
                        }
                }
            }
        }
        mBind?.mTvSubmit?.isSubmitCanGo(false)
        mBind?.mEtAnswer?.addTextChangedListener {
            checkGo()
        }
        mBind?.mEtDiyQuestion?.addTextChangedListener {
            checkGo()
        }
    }

    private fun checkGo() {
        if (idDiy == selectBean?.id) {
            //选中了自定义密保
            mBind?.mTvSubmit?.isSubmitCanGo(
                mBind?.mEtDiyQuestion?.text?.trim()?.toString()
                    ?.isNotEmpty() == true && mBind?.mEtAnswer?.text?.trim()?.toString()
                    ?.isNotEmpty() == true
            )
        } else {
            mBind?.mTvSubmit?.isSubmitCanGo(
                mBind?.mEtAnswer?.text?.trim()?.toString()?.isNotEmpty() == true
            )
        }
    }

    private fun initData() {

        lifecycleScope.launch {
            viewModel.getQuestionList().observe(this@SecurityAuthEditActivity) {
                mTableList = it
                if (mTableList?.isNotEmpty() == true) {
                    mBind?.mTvQuestionTable?.text = mTableList!![0].title
                    selectBean = mTableList!![0]
                }
            }
        }


    }

    override fun showBgTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    private fun goNest() {
        finish()
        EventBus.getDefault().post(ActionEventBean().apply {
            this.isFinishAction = true
        })
    }


}

