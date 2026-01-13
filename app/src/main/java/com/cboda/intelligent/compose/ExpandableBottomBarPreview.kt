package com.cboda.intelligent.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cboda.intelligent.R

@Preview(showBackground = true)
@Composable
fun ExpandableBottomBarPreview() {
    // 预览状态：默认展开以展示毛玻璃效果
    val selectedIndexState = remember { mutableStateOf(0) }
    val expandedState = remember { mutableStateOf(true) }

    val previewItems = listOf(
        ExpandableBottomBarBroadItem(
            icon = painterResource(R.mipmap.ic_xiaoxi_broad_false),
            selectedIcon = painterResource(R.mipmap.ic_xiaoxi_broad_true),
            text = "消息",
            badge = "12"
        ),
        ExpandableBottomBarBroadItem(
            icon = painterResource(R.mipmap.ic_tongxunlu_broad_false),
            selectedIcon = painterResource(R.mipmap.ic_tongxunlu_broad_t),
            text = "通讯录",
            badge = "3"
        ),
        ExpandableBottomBarBroadItem(
            icon = painterResource(R.mipmap.ic_faxian_broad_false),
            selectedIcon = painterResource(R.mipmap.ic_faxian_broad_t),
            text = "发现",
            badge = "0"
        ),
        ExpandableBottomBarBroadItem(
            icon = painterResource(R.mipmap.ic_wode_broad_false),
            selectedIcon = painterResource(R.mipmap.ic_wode_broad_t),
            text = "我的",
            badge = "0"
        )
    )

    MaterialTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // 模拟聊天消息列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding( 4.dp) // 留出导航栏空间
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

            // 悬浮导航栏（毛玻璃 + 90% 透明度在组件内部通过背景alpha实现）
            ExpandableBottomBar(
                selectedIndex = selectedIndexState.value,
                onItemSelected = { selectedIndexState.value = it },
                expanded = expandedState.value,
                onExpandedChange = { expandedState.value = it },
                items = previewItems,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}


