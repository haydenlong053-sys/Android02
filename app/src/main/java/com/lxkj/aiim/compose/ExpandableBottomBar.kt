// ExpandableBottomBar.kt
package com.lxkj.aiim.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lxkj.aiim.R

data class ExpandableBottomBarItem(
    val icon: Painter,
    val selectedIcon: Painter? = null,
    val text: String,
    val badge: String = "0"
)

private val FabSize = 64.dp
private val BarCornerRadius = 35.dp
private val BarHeight = 64.dp
private const val ExpandedWidthDpValue = 299


@Composable
fun ExpandableBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    // 【核心修改】状态提升：由外部控制是否展开
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<ExpandableBottomBarItem>,
    // 是否在收起态根据未读自动切换显示对应模块图标
    autoSwitchOnUnreadIcon: Boolean = false,
    // 是否在收起态点击时自动跳转到有未读的模块
    autoJumpOnUnread: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // 将角标字符串解析为数值，支持如 "99+" 的形式（按99计算）
    fun parseBadgeStr(badge: String): Int {
        return when {
            badge.isEmpty() -> 0
            badge.endsWith("+") -> badge.dropLast(1).toIntOrNull() ?: 0
            else -> badge.toIntOrNull() ?: 0
        }
    }
    // 【核心修改】外部点击收起的逻辑：
    // 如果展开，就在底层显示一个全屏的、透明的可点击层(Scrim)
    // 点击它会调用 onExpandedChange(false) 来通知外部收起
    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null, // 没有点击效果
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onExpandedChange(false) // 通知外部收起
                }
        )
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    // 展开宽度改为屏幕宽度减 76dp
    val targetWidthDp = screenWidthDp - 76.dp

    val horizontalPadding by animateDpAsState(
        targetValue = if (expanded) {
            (screenWidthDp - targetWidthDp) / 2
        } else {
            (screenWidthDp - FabSize) / 2
        }.coerceAtLeast(0.dp),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "padding"
    )

    val widthDp by animateDpAsState(
        targetValue = if (expanded) targetWidthDp else FabSize,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "width"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (expanded) BarCornerRadius else FabSize / 2,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "corner"
    )

    // 外部容器，用于对齐和应用 MainActivity 中传递的 modifier (例如，用于平移动画)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight) ,// 包含垂直padding的高度，为阴影和动画提供空间
        contentAlignment = Alignment.Center
    ) {
        // 导航栏本身，使用 padding 来实现居中，而不是在父级 Box 中

        Box(
            modifier = Modifier
                .padding(horizontal = horizontalPadding, vertical = 0.dp)
                .height(BarHeight)
                .width(widthDp)
                .shadow(4.dp, RoundedCornerShape(cornerRadius), clip = false)

//                .clip(RoundedCornerShape(cornerRadius))
//                                .blur(if (expanded) 6.dp else 6.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(Color(0xF2FFFFFF),RoundedCornerShape(cornerRadius))
                // 边框改为在内容之前绘制，避免在收起态时覆盖角标
                .drawWithContent {
                    val shape = RoundedCornerShape(cornerRadius)
                    val outline = shape.createOutline(size, layoutDirection, this)
                    drawOutline(
                        outline = outline,
                        color = Color(0xffE2E3FF),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawContent()
                }


                .clickable(
                    enabled = !expanded, // 只在收起状态下可点击展开
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // 收起态点击：按需自动跳转到有未读的模块
                    if (autoJumpOnUnread) {
                        val firstUnreadIndex = items.indexOfFirst { parseBadgeStr(it.badge) > 0 }
                        if (firstUnreadIndex >= 0) {
                            onItemSelected(firstUnreadIndex)
                        }
                    }
                    onExpandedChange(true) // 点击圆形时，通知外部展开
                }
        ) {
            if (expanded) {
                // 展开状态：在 Row 背后添加一个可滑动的选中指示器
                androidx.compose.foundation.layout.BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    val totalWidth = maxWidth
                    val itemCount = items.size.coerceAtLeast(1)
                    val slotWidth = totalWidth / itemCount
                    // 指示器高度保持覆盖图标与文字区域（不降低高度）
                    val indicatorHeight = BarHeight - 8.dp
                    // 圆角采用导航栏相同圆角，形成长胶囊视觉但不改变高度
                    val indicatorCorner = BarCornerRadius

                    val targetOffsetX = slotWidth * selectedIndex
                    val indicatorOffsetX by animateDpAsState(
                        targetValue = targetOffsetX,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
                        label = "indicator_offset"
                    )

                    // 指示器放在内容之前绘制（底层），确保图标和角标在上方
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(indicatorHeight) // 保持覆盖图标与文字
                                .width(slotWidth)
                                .align(Alignment.CenterStart) // 垂直居中，水平从起点开始
                                .offset(x = indicatorOffsetX)
                                .clip(RoundedCornerShape(indicatorCorner))
                                .background(Color(0x1A4F60FF)) // 轻微高亮，透明背景
                        )
                    }

                    Row(
                        modifier = Modifier
                            .matchParentSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEachIndexed { index, item ->
                            BottomNavItem(
                                icon = item.icon,
                                selectedIcon = item.selectedIcon ?: item.icon,
                                label = item.text,
                                selected = selectedIndex == index,
                                onClick = { onItemSelected(index) },
                                badgeCount = parseBadgeStr(item.badge)
                            )
                        }
                    }
                }
            } else {
                // 收起时：显示“有新未读”的那个item图标，并叠加总未读角标
                // 优先展示第一个 badge>0 的 item；若都为0，则展示当前选中项
                val firstUnreadIndex = items.indexOfFirst { parseBadgeStr(it.badge) > 0 }
                val activeIndex = if (autoSwitchOnUnreadIcon && firstUnreadIndex >= 0) firstUnreadIndex else selectedIndex
                val activeItem = items.getOrNull(activeIndex)
                val fabIcon: Painter = activeItem?.selectedIcon ?: activeItem?.icon
                    ?: painterResource(R.mipmap.ic_xiaoxi_true)

                // 计算总未读角标（仅数值型，超99显示99+）
                val totalBadgeCount = items.sumOf { parseBadgeStr(it.badge) }

                Box(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (totalBadgeCount > 0) {
                                Badge(containerColor = Color(0xFFEB534B)) {
                                    val countText = if (totalBadgeCount > 99) "99+" else totalBadgeCount.toString()
                                    Text(
                                        text = countText,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xffCCCEDF).copy(0.26f),CircleShape),
                            contentAlignment = Alignment.Center){
                            Icon(
                                painter = fabIcon,
                                contentDescription = activeItem?.text ?: "展开",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.BottomNavItem(
    icon: Painter,
    selectedIcon: Painter,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = Color(0xFFEB534B)
                        ) {
                            val countText = if (badgeCount > 99) "99+" else badgeCount.toString()
                            Text(
                                text = countText,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            ) {
                Icon(
                    painter = if (selected) selectedIcon else icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }
        Text(
            lineHeight = TextUnit(13f, TextUnitType.Sp),
            text = label,
            color = if (selected) Color(0xff3B47DD) else Color(0xff9FA1B7),
            fontSize = 10.sp,
//            modifier = Modifier.offset(y = (-2).dp),
            maxLines = 1
        )

    }
}

