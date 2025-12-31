package com.lxkj.aiim.lj

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.getUuIdCreate
import com.cboda.baselibrary.utils.setEdTextChange
import com.cboda.baselibrary.utils.setStrokeColorAndStartAlpha
import com.blankj.utilcode.util.EncryptUtils
import com.lxkj.aiim.base.BaseFragment
import com.lxkj.aiim.databinding.RegisteredFragmentBinding
import com.lxkj.aiim.lj.utils.click
import com.lxkj.aiim.lj.utils.isSubmitCanGo
import com.lxkj.aiim.viewmodel.LoginViewModel

class RegisteredFragment : BaseFragment<RegisteredFragmentBinding, LoginViewModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = RegisteredFragmentBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        binding.loginAccount.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.loginPwd.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
        binding.updatePwd.setStrokeColorAndStartAlpha(noFocusColor = com.cboda.baselibrary.R.color.color_080E1E)
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