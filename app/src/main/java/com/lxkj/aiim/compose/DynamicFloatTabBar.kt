package com.lxkj.aiim.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wildfire.chat.kit.third.utils.UIUtils.density
import com.cboda.baselibrary.utils.eLogD

@Preview(showBackground = true, backgroundColor = 0xff000000)
@Composable
fun DynamicTabScreen() {
    // 示例数据
    val tabs = remember {
        listOf("推荐", "热点", "视频", "娱乐", "体育", "科技", "游戏", "财经")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 这里就是你要的动态 TabBar
        DynamicFloatTabBar(
            tabs = tabs,
            onTabSelected = { index ->
                // 这里可以触发页面切换（配合 Pager 使用更佳）
                println("选中了第 $index 个 tab：${tabs[index]}")
            },
            unreadCounts = listOf("99","101","2","0")
        )

        // 下面放你的页面内容（示例）
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "当前选中：${tabs.getOrNull(0) ?: ""}",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 动态浮动风格 TabBar（带底部指示器动画 + 文字动画）
 */
@Composable
fun DynamicFloatTabBar(
    tabs: List<String>,
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    selectedTextColor: Color = Color(0xff3B47DD),
    unselectedTextColor: Color = Color.Black.copy(0.9f),
    indicatorColor: Color = Color(0xff3B47DD),
    indicatorHeight: Dp = 2.dp,
    indicatorCornerRadius: Dp = 100.dp,
    indicatorFixedWidth: Dp? = null,
    unreadCounts: List<String> = emptyList(),
    minCellWidth: Dp = 72.dp,
    contentPaddingStart: Dp = 0.dp,
    contentPaddingEnd: Dp = 0.dp,
) {
    var selectedTabIndex by remember { mutableIntStateOf(selectedIndex.coerceIn(0, tabs.lastIndex)) }
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val contentPaddingStartPx = with(density) { contentPaddingStart.toPx() }
    val contentPaddingEndPx = with(density) { contentPaddingEnd.toPx() }
    val isFixedLayout by remember(tabs, rowWidthPx, minCellWidth, contentPaddingStartPx, contentPaddingEndPx) {
//        val canFixedByCount = tabs.size <= 4
//        val effectiveWidthPx = (rowWidthPx - contentPaddingStartPx - contentPaddingEndPx).coerceAtLeast(0f)
//        val cellWidthDp = with(density) { if (effectiveWidthPx > 0 && tabs.isNotEmpty()) (effectiveWidthPx / tabs.size.toFloat()).toDp() else 0.dp }
//        val canFixedByWidth = cellWidthDp >= minCellWidth
//        mutableStateOf(canFixedByCount && canFixedByWidth)
        //先不考虑计算分布，减少手机ui适配问题，默认固定均分布局
        mutableStateOf(true)
    }
    androidx.compose.runtime.LaunchedEffect(selectedIndex) {
        selectedTabIndex = selectedIndex.coerceIn(0, tabs.lastIndex)
    }

    // 存储每个标题「文字本身」的宽度（px）
    val textWidths = remember(tabs) { mutableStateMapOf<Int, Int>() }

    // 每个 Tab 的左右 padding 总和 = 18.dp + 18.dp = 36.dp
    val tabHorizontalPaddingPx = with(density) { 24.dp.toPx() }
    val scrollState = rememberScrollState()

    val indicatorWidth by animateDpAsState(
        targetValue = indicatorFixedWidth ?: (textWidths[selectedTabIndex]?.let { with(density) { it.toDp() } } ?: 0.dp),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 600f),
        label = "indicator_width"
    )

    val indicatorOffsetX by animateDpAsState(
        targetValue = if (isFixedLayout && rowWidthPx > 0) {
            val effectiveWidthPx = (rowWidthPx - contentPaddingStartPx - contentPaddingEndPx).coerceAtLeast(0f)
            val cellWidthDp = with(density) { (effectiveWidthPx / tabs.size.toFloat()).toDp() }
            contentPaddingStart + cellWidthDp * selectedTabIndex + cellWidthDp / 2 - indicatorWidth / 2
        } else if (textWidths.isEmpty()) 0.dp else {
            var offsetPx = tabHorizontalPaddingPx / 2f
            for (i in 0 until selectedTabIndex) {
                val width = textWidths[i] ?: 0
                offsetPx += width + tabHorizontalPaddingPx
            }
            val currentTextWidth = textWidths[selectedTabIndex] ?: 0
            offsetPx += currentTextWidth / 2f
            with(density) { (contentPaddingStart + offsetPx.toDp() - indicatorWidth / 2 - scrollState.value.toDp()) }
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 800f),
        label = "indicator_offset"
    )

    FloatShadowBar(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
//        contentColor = Color.White.copy(0.12f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = contentPaddingStart, end = contentPaddingEnd)
                    .then(if (isFixedLayout) Modifier else Modifier.horizontalScroll(scrollState))
                    .onGloballyPositioned { rowWidthPx = it.size.width },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start // 自动居中分布，也可用 SpaceEvenly
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = index == selectedTabIndex

                    val textColor by animateColorAsState(
                        if (isSelected) selectedTextColor else unselectedTextColor,
                        label = "color"
                    )
                    val textScale by animateFloatAsState(if (isSelected) 1f else 1f, label = "scale")
                    if (isFixedLayout) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            ClickableText(
                                text = title,
                                isSelected = isSelected,
                                textColor = textColor,
                                scale = textScale,
                                badgeText = unreadCounts.getOrNull(index),
                                onClick = {
                                    selectedTabIndex = index
                                    onTabSelected(index)
                                },
                                onTextWidthMeasured = { widthPx ->
                                    textWidths[index] = widthPx
                                }
                            )
                        }
                    } else {
                        ClickableText(
                            text = title,
                            isSelected = isSelected,
                            textColor = textColor,
                            scale = textScale,
                            badgeText = unreadCounts.getOrNull(index),
                            onClick = {
                                selectedTabIndex = index
                                onTabSelected(index)
                            },
                            onTextWidthMeasured = { widthPx ->
                                textWidths[index] = widthPx
                            }
                        )
                    }
                }
            }

            // 底部指示器
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = indicatorOffsetX)
                    .width(indicatorWidth)
                    .height(indicatorHeight)
                    .offset(y=(-2).dp)
                    .background(indicatorColor, RoundedCornerShape(indicatorCornerRadius))
            )
        }
    }
}

/** 提取出来的可点击文字，支持测量真实文字宽度 */
@Composable
private fun ClickableText(
    text: String,
    isSelected: Boolean,
    textColor: Color,
    scale: Float,
    badgeText: String?,
    onClick: () -> Unit,
    onTextWidthMeasured: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 0.dp, vertical = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.W400 else FontWeight.W400,
            onTextLayout = { result ->
                if (result.didOverflowWidth.not()) {
                    // 拿到真实文字宽度（不包含 padding）
                    onTextWidthMeasured(result.size.width)
                }
            }
        )

        val displayBadge = run {
            if (badgeText.isNullOrEmpty()) null else {
                val n = badgeText.toIntOrNull()
                when {
                    n == null -> if (badgeText == "0") null else badgeText
                    n <= 0 -> null
                    n > 99 -> "99+"
                    else -> n.toString()
                }
            }
        }
        if (!displayBadge.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-6).dp)
                    .background(Color(0xFFEB534B), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = displayBadge,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W400,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
