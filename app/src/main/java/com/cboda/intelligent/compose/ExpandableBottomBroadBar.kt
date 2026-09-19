// ExpandableBottomBar.kt — 简版固定底栏（对齐反编译 APK）
package com.cboda.intelligent.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ExpandableBottomBarBroadItem(
    val icon: Painter,
    val selectedIcon: Painter? = null,
    val text: String,
    val badge: String = "0"
)

private val BarTopCornerRadius = 28.dp
private val BarHeight = 64.dp
private val BrandPurple = Color(0xFF5F19E2)
private val UnselectedGrey = Color(0xFFB0AEC2)
private val BadgeRed = Color(0xFFEB534B)
private val ShadowColor = Color(0x1A000000)

/**
 * 简版主页底栏：固定贴底白底 + 顶部圆角，仅展示「社交 / 中心」等 Tab。
 * [expanded] / [onExpandedChange] / auto* 参数保留以兼容旧调用，新 UI 不再展开收起。
 */
@Composable
fun ExpandableBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    @Suppress("UNUSED_PARAMETER") expanded: Boolean = true,
    @Suppress("UNUSED_PARAMETER") onExpandedChange: (Boolean) -> Unit = {},
    items: List<ExpandableBottomBarBroadItem>,
    @Suppress("UNUSED_PARAMETER") autoSwitchOnUnreadIcon: Boolean = false,
    @Suppress("UNUSED_PARAMETER") autoJumpOnUnread: Boolean = false,
    modifier: Modifier = Modifier,
) {
    fun parseBadgeStr(badge: String): Int {
        return when {
            badge.isEmpty() -> 0
            badge.endsWith("+") -> badge.dropLast(1).toIntOrNull() ?: 0
            else -> badge.toIntOrNull() ?: 0
        }
    }

    val shape = RoundedCornerShape(topStart = BarTopCornerRadius, topEnd = BarTopCornerRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                ambientColor = ShadowColor,
                spotColor = ShadowColor,
            )
            .clip(shape)
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                BottomNavItem(
                    icon = item.icon,
                    selectedIcon = item.selectedIcon ?: item.icon,
                    label = item.text,
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    badgeCount = parseBadgeStr(item.badge),
                )
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
    val tint = if (selected) BrandPurple else UnselectedGrey
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = BadgeRed) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            fontSize = 10.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        ) {
            Icon(
                painter = if (selected) selectedIcon else icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = tint,
            )
        }
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            lineHeight = TextUnit(13f, TextUnitType.Sp),
            maxLines = 1,
        )
    }
}
