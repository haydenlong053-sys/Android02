package com.lxkj.aiim.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lxkj.aiim.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush

data class FrostedNavItem(
    val icon: Painter,
    val label: String,
    val badge: Int = 0
)

@Composable
fun FrostedBottomNavBar(
    items: List<FrostedNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundAlpha: Float = 0.72f,
    blurRadius: Dp = 18.dp,
    backgroundBrush: Brush? = null
) {
    // 导航栏容器：hazeChild 让其对父容器进行毛玻璃采样
    Box(
        modifier = modifier
            .height(64.dp)
            .fillMaxWidth(0.9f)
            .shadow(6.dp, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        // 背景层：使用传入的 Brush 绘制并进行模糊，模拟“导航栏背景被模糊”
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .then(
                    if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier.background(Color.Transparent)
                )
                .blur(blurRadius)
        )
        // 半透明白覆盖层：不模糊，只提供乳白色观感
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.White.copy(alpha = backgroundAlpha))
                .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )
        // 轻微高光层：顶部到下方的渐变提升“玻璃感”
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onItemSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (item.badge > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    val countText = if (item.badge > 99) "99+" else item.badge.toString()
                                    Text(
                                        text = countText,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            painter = item.icon,
                            contentDescription = item.label,
                            // 提高对比度：未选中也使用深色而不是不指定
                            tint = if (selectedIndex == index) MaterialTheme.colorScheme.primary else Color(0xFF222222)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.label,
                        // 提高文字对比度，避免在 90% 白底毛玻璃上发灰
                        color = if (selectedIndex == index) Color(0xFF111111) else Color(0xFF333333),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FrostedBottomNavBarPreview() {
    var selectedIndex by remember { mutableStateOf(0) }
    val bottomMargin = 24.dp
    

    // 使用内置矢量图标以避免资源缺失导致预览空白
    val previewItems = listOf(
        FrostedNavItem(icon = rememberVectorPainter(Icons.Filled.Home), label = "消息", badge = 12),
        FrostedNavItem(icon = rememberVectorPainter(Icons.Filled.Search), label = "通讯录", badge = 3),
        FrostedNavItem(icon = rememberVectorPainter(Icons.Filled.Favorite), label = "发现"),
        FrostedNavItem(icon = rememberVectorPainter(Icons.Filled.Person), label = "我的")
    )

    MaterialTheme {
        // 顶层内容：聊天列表 + haze 采样背景（使用默认 Haze 样式以兼容不同版本）
        // 顶层内容：聊天列表 + 渐变背景，不使用 haze
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFECEFF1),
                            Color(0xFFCFD8DC),
                            Color(0xFFB0BEC5)
                        )
                    )
                )
        ) {
            // 模拟聊天消息列表
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items((1..30).toList()) { idx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE0E0E0))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(text = "聊天对象 $idx", color = Color(0xFF222222), fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(text = "最近一条消息内容...", color = Color(0xFF888888), fontSize = 12.sp)
                        }
                        Text(text = "下午 6:${idx % 60}", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                    }
                    Divider(color = Color(0xFFEAEAEA))
                }
            }

            // 底部悬浮毛玻璃导航栏（仅导航栏背景模糊）
            FrostedBottomNavBar(
                items = previewItems,
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomMargin),
                cornerRadius = 24.dp,
                backgroundAlpha = 0.80f,
                blurRadius = 18.dp,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFECEFF1),
                        Color(0xFFCFD8DC),
                        Color(0xFFB0BEC5)
                    )
                )
            )
        }
    }
}