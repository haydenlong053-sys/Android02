package com.lxkj.aiim.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.lxkj.aiim.R
import com.lxkj.aiim.ui.shape.BottomArcShape
import com.lxkj.aiim.ui.shape.TopArcShape
import com.lxkj.aiim.ui.shape.TopRoundedBottomArcShape
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.lxkj.aiim.base.BaseLazyFragment
import com.lxkj.aiim.dialog.pop.AuthPasswordPop
import com.lxkj.aiim.lj.utils.SharePrefBroadUtil
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.bean.ResultBean
import okhttp3.Request
import okhttp3.Response
import com.lxkj.aiim.lj.QianbaoActivity
import com.lxkj.aiim.lj.SafeCenterActivity
import com.lxkj.aiim.lj.CiecelActivity
import com.lxkj.aiim.lj.MyCollectActivity
import com.lxkj.aiim.lj.IssueActivity
import com.lxkj.aiim.lj.UserSetActivity
import com.lxkj.aiim.lj.HyzxActivity

import com.lxkj.aiim.lj.event.ActionRefreshEvent

import com.lxkj.aiim.lj.utils.showGuidePasswordTip

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MineFragment : BaseLazyFragment() {
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
        return R.layout.fragment_wode
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
        val vipFlag = SharePrefBroadUtil.getString(requireContext(), "isvip", "0")
        isVip = vipFlag == "1"
        // 优先从偏好中读取到期时间（若未写入则为空字符串）
        vipExpireText = SharePrefBroadUtil.getString(requireContext(), "vipTime", "")
    }

    private fun fetchUserInfo() {
        val params = HashMap<String, Any>()
        OkHttpHelper.getInstance().post_json(
            requireActivity(), Url.userInfo, params,
            object : BaseCallback<ResultBean>() {
                override fun onBeforeRequest(request: Request?) {}
                override fun onFailure(request: Request?, e: Exception?) {}
                override fun onResponse(response: Response?) {}
                override fun onSuccess(response: Response?, resultBean: ResultBean) {
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
                    SharePrefBroadUtil.saveString(requireContext(), "isvip", vipFlag)
                    SharePrefBroadUtil.saveString(requireContext(), "vipTime", vipTimeText)

                    // 更新当前页面状态
                    isVip = vipFlag == "1"
                    vipExpireText = vipTimeText
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {}
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: ActionRefreshEvent) {
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
            painter = painterResource(id = R.mipmap.bg_mine_top_blue),
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
            ).clickable(indication = null, // 没有点击效果
                interactionSource = remember { MutableInteractionSource() }) {
                // 跳转到会员中心（原本 llHyzx 点击）
                context.startActivity(Intent(context, HyzxActivity::class.java))
            }
    ) {

        // 背景图，顶部圆角 12dp，底部凹形 16dp
        Image(
            painter = painterResource(id = R.mipmap.bg_mine_vip),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(117.dp)
                .clip(TopRoundedBottomArcShape(12f, 16f)),
            contentScale = ContentScale.Crop
        )
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
            Text(if (isVip) "VIP会员" else "非会员", color = Color(0xffFEECD7), fontSize = 10.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_mine_vip_logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isVip) "SeekWeb4 VIP会员" else "开启会员，享受尊贵特权",
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
            if (!isVip){
                Box(
                    modifier = Modifier
                        .width(73.dp)
                        .height(27.dp)
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.bg_btn_vip),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text =  "立即开通",
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            val items = listOf(
                MenuItem("个人资产", "管理您的数字财富", R.mipmap.ic_mine_service),
                MenuItem("安全中心", "保护账户，安心使用", R.mipmap.ic_mine_safe),
                MenuItem("灵感广场", "分享时刻，看见灵感", R.mipmap.ic_mine_moment),
                MenuItem("我的收藏", "我收藏的都在这里", R.mipmap.ic_mine_collect),
                MenuItem("常见问题", "快速解答关于我们", R.mipmap.ic_mine_feedback),
                MenuItem("系统设置", "个性化您的使用体验", R.mipmap.ic_mine_setting)
            )

            // 两列网格：逐行渲染
            items.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)) {
                    rowItems.forEach { item ->
                        MenuCard(item, modifier = Modifier.weight(1f)) {
                            when (it.title) {
                                // 服务中心（个人资产）门控：未设置支付密码则弹引导
                                "个人资产" -> {
                                    if (isHasPassword) {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                QianbaoActivity::class.java
                                            )
                                        )
                                    } else {
                                        showGuidePasswordTip(context)
                                    }
                                }
                                // 安全中心同样进行支付密码设置引导
                                "安全中心" -> {
                                    if (isHasPassword) {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                SafeCenterActivity::class.java
                                            )
                                        )
                                    } else {
                                        showGuidePasswordTip(context)
                                    }
                                }

                                "灵感广场" -> context.startActivity(
                                    Intent(
                                        context,
                                        CiecelActivity::class.java
                                    )
                                )

                                "我的收藏" -> context.startActivity(
                                    Intent(
                                        context,
                                        MyCollectActivity::class.java
                                    )
                                )

                                "常见问题" -> context.startActivity(
                                    Intent(
                                        context,
                                        IssueActivity::class.java
                                    )
                                )

                                "系统设置" -> context.startActivity(
                                    Intent(
                                        context,
                                        UserSetActivity::class.java
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

private data class MenuItem(val title: String, val subtitle: String, val iconRes: Int)

@Composable
private fun MenuCard(item: MenuItem, modifier: Modifier = Modifier, onClick: (MenuItem) -> Unit) {

    Card(
        modifier = modifier
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
                    text = item.title,
                    color = Color.Black.copy(0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.subtitle, color = Color.Black.copy(0.65f), fontSize = 10.sp)
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