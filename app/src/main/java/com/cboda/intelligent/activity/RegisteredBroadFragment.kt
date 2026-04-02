package com.cboda.intelligent.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.getUuIdCreate
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.blankj.utilcode.util.EncryptUtils
import com.cboda.baselibrary.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.RegisteredBroadFragmentBinding
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.viewmodel.LoginViewBroadModel

class RegisteredBroadFragment : BaseBroadFragment<RegisteredBroadFragmentBinding, LoginViewBroadModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = RegisteredBroadFragmentBinding.inflate(inflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_080E1E)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_080E1E)
        binding.updatePwd.setStrokeColorAndStartAlpha(noFocusColor = R.color.color_080E1E)
        binding.loginBtn.isSubmitCanGo(false)
        val uuIdCreate = getUuIdCreate()
        binding.content.text =
            (uuIdCreate + EncryptUtils.encryptMD5ToString(uuIdCreate)).uppercase()
        viewModel.getMnemonicWords().observe(this) {
            if (!TextUtils.isEmpty(it)) {
                binding.content.text = it
            }
        }

    }

    override fun initListener() {
        super.initListener()
        binding.loginAccount.setEdTextChange {
            val pwd = binding.loginPwd.text.toString()
            val secondPwd = binding.updatePwd.text.toString()
            binding.loginBtn.isSubmitCanGo(
                !TextUtils.isEmpty(it) && !TextUtils.isEmpty(pwd) && !TextUtils.isEmpty(
                    secondPwd
                )
            )
            binding.loginLookPwd.isGone = TextUtils.isEmpty(it)
        }
        binding.loginPwd.setEdTextChange {
            val account = binding.loginAccount.text.toString()
            val secondPwd = binding.updatePwd.text.toString()
            binding.loginBtn.isSubmitCanGo(
                !TextUtils.isEmpty(it) && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(
                    secondPwd
                )
            )
        }
        binding.updatePwd.setEdTextChange {
            val account = binding.loginAccount.text.toString()
            val pwd = binding.loginPwd.text.toString()
            binding.loginBtn.isSubmitCanGo(
                !TextUtils.isEmpty(it) && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(
                    pwd
                )
            )
        }
        binding.loginBtn.click {
            viewModel.registerAccount(
                binding.loginAccount.text.toString(),
                binding.loginPwd.text.toString(),
                binding.updatePwd.text.toString()
            )
        }
        binding.loginLookPwd.click {
            binding.loginAccount.setText("")
        }
    }

}