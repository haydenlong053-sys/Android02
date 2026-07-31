package com.cboda.intelligent.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.setBackColor
import com.cboda.baselibrary.utils.setTxtColor
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.SquareUserProfileBean
import com.cboda.intelligent.databinding.SquareUserActivityBinding
import com.cboda.intelligent.fragment.SquareUserMomentTabFragment
import com.cboda.intelligent.fragment.SquareUserSquareTabFragment
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.viewmodel.CleverBodyViewBroadModel
import com.cboda.intelligent.widge.TextIndicatorAdapter
import kotlinx.coroutines.launch

class SquareUserActivity : BaseSimple2BroadActivity() {

    private var mBinding: SquareUserActivityBinding? = null
    private val viewModel: CleverBodyViewBroadModel by viewModels()
    private var userId: String = ""
    private var currentProfile: SquareUserProfileBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = binding as? SquareUserActivityBinding
        setupPageBackground()
        userId = intent.getStringExtra(EXTRA_USER_ID).orEmpty()
            .ifBlank { MMKVBroadUtils.getRealUserId().orEmpty() }
        initView()
        loadUserProfile()
    }

    private fun setupPageBackground() {
        setRootBgColor(com.cboda.baselibrary.R.color.bg_main2)
        mIvBgTop.setImageResource(R.drawable.ic_gc_top_bg)
        mIvBgTop.scaleType = ImageView.ScaleType.FIT_XY
        mIvBgTop.layoutParams.height = resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_214)
        mIvBgTop.requestLayout()
    }

    private fun initView() {
        val binding = mBinding ?: return
        BarUtils.addMarginTopEqualStatusBarHeight(binding.backLayout)
        binding.backLayout.click { finish() }
        binding.backIcon.click { finish() }
        binding.followBtn.click { toggleFollow() }
        binding.followerCountLayout.click {
            SquareFollowerListActivity.start(this, userId)
        }
        binding.followingCountLayout.click {
            SquareAttentionActivity.start(this, userId)
        }
        initViewPagerAndIndicator()
    }

    private fun initViewPagerAndIndicator() {
        val binding = mBinding ?: return
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(SquareUserSquareTabFragment.newInstance(userId))
        vp2FragmentAdapter.addFragment(SquareUserMomentTabFragment.newInstance(userId))
        binding.userViewPagerLayout.viewPager2.adapter = vp2FragmentAdapter
        binding.userViewPagerLayout.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.userViewPagerLayout.viewPager2.offscreenPageLimit = 1
        binding.userViewPagerLayout.viewPager2.isSaveEnabled = false

        val tabResIds = intArrayOf(
            com.cboda.baselibrary.R.string.bottom_bar_gc,
            com.cboda.baselibrary.R.string.circle_friend,
        )
        val commonNavigator = CommonBroadNavigator(this)
        commonNavigator.isAdjustMode = false
        commonNavigator.isSkimOver = true
        val indicatorAdapter = TextIndicatorAdapter(
            this,
            tabResIds,
            com.cboda.baselibrary.R.dimen.dp_14,
            R.layout.text_indicator_tv,
            24.0,
        )
        commonNavigator.adapter = indicatorAdapter
        binding.userMagicIndicator.navigator = commonNavigator
        bindIndicatorAndViewPager(
            binding.userMagicIndicator,
            binding.userViewPagerLayout.viewPager2,
            indicatorAdapter,
            magicIndicatorNeedOnPageScrolled = true,
        )
        val initialTab = intent.getIntExtra(EXTRA_INITIAL_TAB, TAB_SQUARE)
            .coerceIn(TAB_SQUARE, TAB_MOMENT)
        binding.userViewPagerLayout.viewPager2.setCurrentItem(initialTab, false)
    }

    private fun loadUserProfile() {
        if (userId.isBlank()) return
        lifecycleScope.launch {
            viewModel.getUserProfile(userId).observe(this@SquareUserActivity) { profile ->
                if (profile == null) return@observe
                currentProfile = profile
                bindProfile(profile)
            }
        }
    }

    private fun bindProfile(profile: SquareUserProfileBean) {
        val binding = mBinding ?: return
        val isSelf = userId == MMKVBroadUtils.getRealUserId()
        val grade = profile.grade.takeIf { profile.gradeStatus == "1" && it.isNotBlank() }
        binding.userAvatar.setData(
            profile.icon,
            profile.isLiang == "1",
            profile.isvip == "1",
            grade,
            null,
        )
        binding.likeCountValue.text = anyToInt(profile.likeCount).toString()
        binding.followingCountValue.text = anyToInt(profile.followingCount).toString()
        binding.followerCountValue.text = anyToInt(profile.followerCount).toString()
        binding.userName.text = profile.name
        binding.userSign.text = profile.sign
        binding.userSign.isGone = profile.sign.isBlank()
        binding.followBtn.isGone = isSelf
        if (!isSelf) {
            updateFollowButton(isFollowed(profile.isFollow))
        }
    }

    private fun isFollowed(isFollow: String): Boolean {
        return anyToInt(isFollow) == 1
    }

    private fun toggleFollow() {
        val profile = currentProfile ?: return
        if (userId.isBlank() || userId == MMKVBroadUtils.getRealUserId()) return
        val currentlyFollowed = isFollowed(profile.isFollow)
        lifecycleScope.launch {
            viewModel.followUser(userId, !currentlyFollowed).observe(this@SquareUserActivity) { success ->
                if (success == true) {
                    loadUserProfile()
                }
            }
        }
    }

    private fun updateFollowButton(followed: Boolean) {
        val binding = mBinding ?: return
        binding.followBtn.text = getString(
            if (followed) com.cboda.baselibrary.R.string.square_user_unfollow else com.cboda.baselibrary.R.string.square_user_follow,
        )
        if (followed) {
            binding.followBtn.setBackColor(com.cboda.baselibrary.R.color.grayF0)
            binding.followBtn.setTxtColor(com.cboda.baselibrary.R.color.black_45)
        } else {
            binding.followBtn.setBackColor(com.cboda.baselibrary.R.color.color_3B47DD)
            binding.followBtn.setTxtColor(com.cboda.baselibrary.R.color.white)
        }
    }

    override fun getViewBindingClass() = SquareUserActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = false

    companion object {
        private const val EXTRA_USER_ID = "userId"
        private const val EXTRA_INITIAL_TAB = "initialTab"
        const val TAB_SQUARE = 0
        const val TAB_MOMENT = 1

        fun start(context: Context, userId: String) {
            start(context, userId, TAB_SQUARE)
        }

        fun start(context: Context, userId: String, initialTab: Int) {
            context.startActivity(
                Intent(context, SquareUserActivity::class.java)
                    .putExtra(EXTRA_USER_ID, userId)
                    .putExtra(EXTRA_INITIAL_TAB, initialTab),
            )
        }
    }
}
