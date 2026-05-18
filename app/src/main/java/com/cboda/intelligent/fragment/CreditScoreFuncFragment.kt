package com.cboda.intelligent.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.FragmentCreditScoreFuncBinding
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import kotlinx.coroutines.launch

class CreditScoreFuncFragment : BaseBroadFragment<FragmentCreditScoreFuncBinding, NewMirrorViewBroadModel>() {


    companion object {
        fun newInstance(type: Int = 0) = CreditScoreFuncFragment().apply {
            val args = Bundle()
            args.putInt("type", type)
            arguments = args
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?) = FragmentCreditScoreFuncBinding.inflate(inflater, parent, false)

    override fun initView() {
        super.initView()
        initMyData()

    }

    private fun initMyData() {
//        binding.webView.loadUrl(BroadUrl.getCheckinRewardsUrl())
        lifecycleScope.launch {
            viewModel.getCreditScoreFunc()
                .observe(viewLifecycleOwner) { resultBean ->
                    if (resultBean.scoreInfo.isNullOrEmpty()) {
                        binding.mTvDes.text = ""
                    } else {
                        binding.mTvDes.text = Html.fromHtml(resultBean.scoreInfo, Html.FROM_HTML_MODE_COMPACT)

                    }
                }

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
    }


}

