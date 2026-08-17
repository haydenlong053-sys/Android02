package com.cboda.intelligent.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getIsDebug
import com.cboda.baselibrary.utils.zipLogFiles
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.HyzxBroadActivity
import com.cboda.intelligent.activity.IssueBroadActivity
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.activity.QianbaoBroadActivity
import com.cboda.intelligent.activity.SafeCenterBroadActivity
import com.cboda.intelligent.activity.UserSetBroadActivity
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.bean.ActionRefreshBroadEvent
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.databinding.FragmentNewMineBinding
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.copyToClipboard
import com.cboda.intelligent.utils.showGuidePasswordTip
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.MineViewModel
import com.freddy.chat.IMTestMainActivity
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NewMineBroadFragment : BaseBroadFragment<FragmentNewMineBinding, MineViewModel>() {
    private var isVip = false
    private var vipExpireText = ""
    private var isHasPassword = false

    override fun initView() {
        addHeight(binding.cardVip)
        refreshVipFromPrefs()
        bindVipViews()
        registerEventBusIfNeeded()
    }

    override fun initListener() {
        binding.cardVip.click {
            if (!HttpsBroadConfig.isReviewEnv) {
                startActivity(Intent(requireContext(), HyzxBroadActivity::class.java))
            }
        }
        binding.mTvOpenVip.click {
            if (!HttpsBroadConfig.isReviewEnv) {
                startActivity(Intent(requireContext(), HyzxBroadActivity::class.java))
            }
        }

        // 顶部入口：资产、客服
        binding.layoutViewMoney?.click {
            if (isHasPassword) {
                startActivity(Intent(requireContext(), QianbaoBroadActivity::class.java))
            } else {
                showGuidePasswordTip(requireContext())
            }
        }
        binding.layoutViewService?.click {
            MMKVBroadUtils.getKeFuId()?.let { keFuId ->
                AppBroadUtils.goChatP2p(keFuId, "", context)
            }
        }

        binding.itemFavorite.click {
            JumpBroadUtils.startImMyCollectActivity(requireContext())
        }
        binding.itemSafeCenter.click {
            if (isHasPassword) {
                startActivity(Intent(requireContext(), SafeCenterBroadActivity::class.java))
            } else {
                showGuidePasswordTip(requireContext())
            }
        }
        binding.itemFaq.click {
            startActivity(Intent(requireContext(), IssueBroadActivity::class.java))
//            startActivity(Intent(requireContext(), IMTestMainActivity::class.java))
        }
        binding.itemSettings.click {
            startActivity(Intent(requireContext(), UserSetBroadActivity::class.java))
        }

        binding.itemUploadLog.click {
            val logPath = zipLogFiles()
            eLogD("NewMineBroadFragmentClass", "logPath:$logPath")
            viewModel.postFile(logPath) { netUrl, _ ->
                if (!TextUtils.isEmpty(netUrl)) {
                    eLogD("NewMineBroadFragmentClass", "netUrl:$netUrl")
                    if (getIsDebug()) {
                        copyToClipboard(binding.itemUploadLog.context, netUrl)
                    }
                    showToastMain(com.cboda.baselibrary.R.string.post_log_success)
                }
            }
        }
    }

//    override fun lazyLoad() {
//        fetchUserInfo()
//    }
//
//    override fun onResumeTwo() {
//        fetchUserInfo()
//    }

    override fun onResume() {
        super.onResume()
        fetchUserInfo()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentNewMineBinding {
        return FragmentNewMineBinding.inflate(inflater, parent, false)
    }

    override fun onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroyView()
    }

    private fun registerEventBusIfNeeded() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun refreshVipFromPrefs() {
        isVip = MMKVBroadUtils.getUserVip()
        vipExpireText = SharePrefBroadBroadUtil.getString(requireContext(), "vipTime", "")
        isHasPassword = SharePrefBroadBroadUtil.getString(requireContext(), "paypwd", "").isNotEmpty()
    }

    private fun bindVipViews() {
        if (HttpsBroadConfig.isReviewEnv) {
            binding.cardVip.setImageResource(R.drawable.ic_mine_bg_no_vip)
            binding.mTvOpenVipDes1.text = getString(com.cboda.baselibrary.R.string.welcome_to_seek_four)
            binding.mTvOpenVip.visibility = View.GONE
            binding.mTvOpenVipDes2.visibility = View.GONE
            binding.mTvVipEndTime.visibility = View.GONE
            binding.mTvOpenVipDes1.visibility = View.VISIBLE
            binding.layoutViewMoney.isGone = false
            return
        }
        binding.layoutViewMoney.isGone = false
        if (isVip) {
            binding.cardVip.setImageResource(R.drawable.ic_mine_bg_has_vip)
            binding.mTvOpenVip.visibility = View.GONE
            binding.mTvOpenVipDes1.visibility = View.GONE
            binding.mTvOpenVipDes2.visibility = View.VISIBLE
            binding.mTvVipEndTime.visibility = View.VISIBLE
            binding.mTvVipEndTime.text = vipExpireText
        } else {
            binding.cardVip.setImageResource(R.drawable.ic_mine_bg_no_vip)
            binding.mTvOpenVip.visibility = View.VISIBLE
            binding.mTvOpenVipDes1.visibility = View.VISIBLE
            binding.mTvOpenVipDes2.visibility = View.GONE
            binding.mTvVipEndTime.visibility = View.GONE
        }
    }

    private fun fetchUserInfo() {
        if (!isAdded || activity == null) return
        val params = HashMap<String, Any>()
        OkHttpBroadHelper.getInstance().post_json(
            requireActivity(),
            BroadUrl.userInfo,
            params,
            object : BaseBroadCallback<ResultBroadBean>() {
                override fun onBeforeRequest(request: Request?) {}
                override fun onFailure(request: Request?, e: Exception?) {}
                override fun onResponse(response: Response?) {}

                override fun onSuccess(response: Response?, resultBean: ResultBroadBean) {
                    val data = resultBean.data ?: return
                    val vipFlag = data.isvip ?: "0"
                    val vipTimeRaw = data.vipTime ?: ""
                    val vipTimeText = if (vipFlag == "1" && vipTimeRaw.isNotEmpty()) {
                        getString(com.cboda.baselibrary.R.string.mine_vip_time_to) + " " + vipTimeRaw
                    } else {
                        ""
                    }
                    val hasPaypwd = data.paypwd?.isNotEmpty() ?: false
                    isHasPassword = hasPaypwd

                    MMKVBroadUtils.saveUserVip(vipFlag)
                    SharePrefBroadBroadUtil.saveString(requireContext(), "vipTime", vipTimeText)
                    SharePrefBroadBroadUtil.saveString(requireContext(), "paypwd", data.paypwd ?: "")

                    isVip = vipFlag == "1"
                    vipExpireText = vipTimeText
                    bindVipViews()
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {}
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: ActionRefreshBroadEvent) {
        fetchUserInfo()
    }
}
