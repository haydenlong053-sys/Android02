package com.cboda.intelligent.fragment

import android.os.Bundle
import android.view.View
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.R
import com.cboda.intelligent.ui.shape.TopRoundedBottomArcShape
import com.cboda.intelligent.base.BaseLazyBroadFragment
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.bean.ResultBroadBean
import okhttp3.Request
import okhttp3.Response
import com.cboda.intelligent.activity.QianbaoBroadActivity
import com.cboda.intelligent.activity.SafeCenterBroadActivity
import com.cboda.intelligent.activity.CiecelBroadActivity
import com.cboda.intelligent.activity.MyCollectBroadActivity
import com.cboda.intelligent.activity.IssueBroadActivity
import com.cboda.intelligent.activity.UserSetBroadActivity
import com.cboda.intelligent.activity.HyzxBroadActivity

import com.cboda.intelligent.bean.ActionRefreshBroadEvent

import com.cboda.intelligent.utils.showGuidePasswordTip

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MineBroadFragment : BaseLazyBroadFragment() {
    private var isVip by mutableStateOf(false)
    private var vipExpireText by mutableStateOf("")
    private var isHasPassword by mutableStateOf(false)


    override fun loadData() {
    }

    override fun onResume() {
        super.onResume()
        // 恢复原本 userInfo 逻辑：进入页面时请求用户信息并同步 VIP 状态
        fetchUserInfo()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_wo_broad_de
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        refreshVipFromPrefs()
        val compose = view.findViewById<ComposeView>(R.id.compose_mine)
        compose.setContent {
            MineScreen(
                isVip = isVip,
                vipExpireText = vipExpireText,
                isHasPassword = isHasPassword
            )
        }
    }


    private fun refreshVipFromPrefs() {
        isVip = MMKVBroadUtils.getUserVip()
        // 优先从偏好中读取到期时间（若未写入则为空字符串）
        vipExpireText = SharePrefBroadBroadUtil.getString(requireContext(), "vipTime", "")
    }

    private fun fetchUserInfo() {
        val params = HashMap<String, Any>()
        OkHttpBroadHelper.getInstance().post_json(
            requireActivity(), BroadUrl.userInfo, params,
            object : BaseBroadCallback<ResultBroadBean>() {
                override fun onBeforeRequest(request: Request?) {}
                override fun onFailure(request: Request?, e: Exception?) {}
                override fun onResponse(response: Response?) {}
                override fun onSuccess(response: Response?, resultBean: ResultBroadBean) {
                    val data = resultBean.data
                    val vipFlag = data.isvip ?: "0"
                    val vipTimeRaw = data.vipTime ?: ""
                    val vipTimeText = if (vipFlag == "1" && vipTimeRaw.isNotEmpty()) {
                        vipTimeRaw + getString(R.string.到期)
                    } else {
                        ""
                    }

                    // 支付密码状态
                    val hasPaypwd = data?.paypwd?.isNotEmpty() ?: false
                    isHasPassword = hasPaypwd

                    // 同步到全局偏好，保持与旧逻辑一致
                    MMKVBroadUtils.saveUserVip(vipFlag)
                    SharePrefBroadBroadUtil.saveString(requireContext(), "vipTime", vipTimeText)

                    // 更新当前页面状态
                    isVip = vipFlag == "1"
                    vipExpireText = vipTimeText
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {}
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: ActionRefreshBroadEvent) {
        fetchUserInfo()

    }
}

@Composable
private fun MineScreen(isVip: Boolean, vipExpireText: String, isHasPassword: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        // 顶部蓝色背景，位于 VIP 图片的下层
        Image(
            painter = painterResource(id = R.mipmap.bg_mine_top_broad_blue),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.FillBounds
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { VipBanner(isVip = isVip, vipExpireText = vipExpireText) }
            // 移除中间间距，使上下弧面视觉连接
            item { MenuContainer(isHasPassword = isHasPassword) }
        }
    }
}

@Composable
private fun VipBanner(isVip: Boolean, vipExpireText: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            )
            .clickable(
                indication = null, // 没有点击效果
                interactionSource = remember { MutableInteractionSource() }) {
                if (!HttpsBroadConfig.isReviewEnv) {
                    // 跳转到会员中心（原本 llHyzx 点击）
                    context.startActivity(Intent(context, HyzxBroadActivity::class.java))
                }
            }
    ) {

        // 背景图，顶部圆角 12dp，底部凹形 16dp
        Image(
            painter = painterResource(id = if (!HttpsBroadConfig.isReviewEnv) R.mipmap.bg_mine_broad_vip else R.mipmap.bg_mine_broad_vip2),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(117.dp)
                .clip(TopRoundedBottomArcShape(12f, 16f)),
            contentScale = ContentScale.Crop
        )
        if (!HttpsBroadConfig.isReviewEnv) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        Color(0xff090E14), RoundedCornerShape(
                            topStart = 12.dp, bottomEnd = 12.dp
                        )
                    )
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Text(if (isVip) stringResource(R.string.vip_member) else stringResource(R.string.非会员), color = Color(0xffFEECD7), fontSize = 10.sp)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_mine_vip_broad_logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isVip && !HttpsBroadConfig.isReviewEnv) stringResource(R.string.seekweb4_vip_member) else if (!HttpsBroadConfig.isReviewEnv) stringResource(R.string.open_vip_privilege) else stringResource(
                        com.cboda.baselibrary.R.string.welcome_to_seek_four
                    ),
                    color = Color(0xFFFFD3A9),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isVip) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = vipExpireText,
                        color = Color(0xA6FFD3A9),
                        fontSize = 12.sp
                    )
                }
            }
            if (!isVip && !HttpsBroadConfig.isReviewEnv) {
                Box(
                    modifier = Modifier
                        .width(73.dp)
                        .height(27.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.bg_btn_broad_vip),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = stringResource(R.string.open_now),
                        color = Color(0xFF1A1A1C),
                        fontSize = 12.sp
                    )


                }
            }

        }
    }
}

@Composable
private fun MenuContainer(isHasPassword: Boolean) {
    val context = LocalContext.current
    Box(modifier = Modifier.padding(horizontal = 0.dp)) {
        val items = arrayListOf<MenuItem>()
        //设置页6大模块数据（审核屏蔽个人资产入口）
        if (!HttpsBroadConfig.isReviewEnv) {
            items.add(MenuItem(stringResource(R.string.personal_assets), stringResource(R.string.manage_digital_wealth), R.mipmap.ic_mine_broad_service, R.string.personal_assets))
        }
        items.add(MenuItem(stringResource(R.string.security_center), stringResource(R.string.protect_account_safe), R.mipmap.ic_mine_broad_safe, R.string.security_center))
        items.add(MenuItem(stringResource(R.string.inspiration_square), stringResource(R.string.share_moments_see_inspiration), R.mipmap.ic_mine_broad_moment, R.string.inspiration_square))
        items.add(MenuItem(stringResource(R.string.我的收藏), stringResource(R.string.my_collections_here), R.mipmap.ic_mine_broad_collect, R.string.我的收藏))
        items.add(MenuItem(stringResource(R.string.常见问题), stringResource(R.string.quick_answers_about_us), R.mipmap.ic_mine_broad_feedback, R.string.常见问题))
        items.add(MenuItem(stringResource(R.string.system_settings), stringResource(R.string.personalize_experience), R.mipmap.ic_mine_broad_setting, R.string.system_settings))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            // 两列网格：逐行渲染
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    rowItems.forEach { item ->
                        MenuCard(item, modifier = Modifier.weight(1f)) {
                            when (it.titleResId) {
                                // 服务中心（个人资产）门控：未设置支付密码则弹引导
                                R.string.personal_assets -> {
                                    if (isHasPassword) {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                QianbaoBroadActivity::class.java
                                            )
                                        )
                                    } else {
                                        showGuidePasswordTip(context)
                                    }
                                }
                                // 安全中心同样进行支付密码设置引导
                                R.string.security_center -> {
                                    if (isHasPassword) {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                SafeCenterBroadActivity::class.java
                                            )
                                        )
                                    } else {
                                        showGuidePasswordTip(context)
                                    }
                                }

                                R.string.inspiration_square -> context.startActivity(
                                    Intent(
                                        context,
                                        CiecelBroadActivity::class.java
                                    )
                                )

                                R.string.我的收藏 -> context.startActivity(
                                    Intent(
                                        context,
                                        MyCollectBroadActivity::class.java
                                    )
                                )

                                R.string.常见问题 -> context.startActivity(
                                    Intent(
                                        context,
                                        IssueBroadActivity::class.java
                                    )
                                )

                                R.string.system_settings -> context.startActivity(
                                    Intent(
                                        context,
                                        UserSetBroadActivity::class.java
                                    )
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private data class MenuItem(val title: String, val subtitle: String, val iconRes: Int, val titleResId: Int)

@Composable
private fun MenuCard(item: MenuItem, modifier: Modifier = Modifier, onClick: (MenuItem) -> Unit) {

    Card(
        modifier = modifier
            .wrapContentHeight()
//            .heightIn(min = 70.dp)
            .height(70.dp)
            .padding(horizontal = 6.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = item.title,
                    color = Color.Black.copy(0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = item.subtitle, color = Color.Black.copy(0.65f), fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            // right chevron or badge could be added here if needed
        }
    }
}

@Preview(showBackground = true, name = "MineScreen Preview")
@Composable
private fun MineScreenPreview() {
    MineScreen(
        isVip = false,
        vipExpireText = "",
        isHasPassword = false
    )
}