package com.cboda.intelligent.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.activity.UserInfoBroadActivity
import com.cboda.intelligent.activity.UserSetBroadActivity
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.bean.ActionRefreshBroadEvent
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.databinding.FragmentNewMineBinding
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.GlideBroadUtil
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.MineViewModel
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NewMineBroadFragment : BaseBroadFragment<FragmentNewMineBinding, MineViewModel>() {

    override fun initView() {
        addHeight(binding.nsvMineContent)
        registerEventBusIfNeeded()
        bindUserProfileFromCache()
    }

    override fun initListener() {
        binding.cardUserProfile.click {
            startActivity(Intent(requireContext(), UserInfoBroadActivity::class.java))
        }
        binding.layoutViewService.click {
            MMKVBroadUtils.getKeFuId()?.let { keFuId ->
                AppBroadUtils.goChatP2p(keFuId, "", context)
            }
        }
        binding.itemFavorite.click {
            JumpBroadUtils.startImMyCollectActivity(requireContext())
        }
        binding.itemSettings.click {
            startActivity(Intent(requireContext(), UserSetBroadActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // 先刷本地缓存（资料页已 write MMKV），再拉 getUser 校正
        bindUserProfileFromCache()
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

    private fun bindUserProfileFromCache() {
        val name = MMKVBroadUtils.getUserName().orEmpty()
        val icon = MMKVBroadUtils.getUserImg().orEmpty()
        binding.tvUserName.text = name
        GlideBroadUtil.setHeadImag(requireContext(), icon, binding.ivUserAvatar)
    }

    private fun bindUserProfile(data: ResultBroadBean.Data) {
        val name = data.name?.takeIf { it.isNotEmpty() } ?: MMKVBroadUtils.getUserName().orEmpty()
        val icon = data.icon?.takeIf { it.isNotEmpty() } ?: data.img.orEmpty()
        val idValue = when {
            !TextUtils.isEmpty(data.idno) -> data.idno
            !TextUtils.isEmpty(data.mId) -> data.mId
            !TextUtils.isEmpty(data.userId) -> data.userId
            else -> data.id.orEmpty()
        }
        binding.tvUserName.text = name
        binding.tvUserId.text = "ID:$idValue"
        GlideBroadUtil.setHeadImag(requireContext(), icon, binding.ivUserAvatar)

        if (!TextUtils.isEmpty(name)) {
            MMKVBroadUtils.saveUserName(name)
        }
        if (!TextUtils.isEmpty(icon)) {
            MMKVBroadUtils.saveUserImg(icon)
        }
    }

    private fun fetchUserInfo() {
        if (!isAdded || activity == null) return
        val params = HashMap<String, Any>()
        val userId = MMKVBroadUtils.getRealUserId()
        if (!userId.isNullOrEmpty()) {
            params["userId"] = userId
        }
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

                    MMKVBroadUtils.saveUserVip(vipFlag)
                    SharePrefBroadBroadUtil.saveString(requireContext(), "vipTime", vipTimeText)
                    SharePrefBroadBroadUtil.saveString(requireContext(), "paypwd", data.paypwd ?: "")

                    if (isAdded) {
                        bindUserProfile(data)
                    }
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {}
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: ActionRefreshBroadEvent) {
        fetchUserInfo()
    }
}
