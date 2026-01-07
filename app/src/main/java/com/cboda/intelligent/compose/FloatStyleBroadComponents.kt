package com.cboda.intelligent.compose


// BlurFabButton.kt
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.cboda.intelligent.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun FloatManagerBar(
    modifier: Modifier = Modifier,
    showAllSelect: Boolean = false,
    showAllRead: Boolean = false,
    enableDelete: Boolean = false,
    borderColor: Color = Color(0xffE2E3FF),
    borderWidth: Dp = 0.5.dp,
    shadowElevation: Dp = 1.dp,
    contentColor: Color = Color.White.copy(0.65f),
    toSelectAll: (Boolean) -> Unit,
    toSelectRead: (Boolean) -> Unit,
    toDelete: () -> Unit
) {
    val textColor = Color(0xff3B47DD)
    val disabledTextColor = Color.Black.copy(0.65f)
    val bgColor = contentColor
    val lightShadow = Color(0xA6FFFFFF)
    val darkShadow = Color(0x14000001)
    val upperOffset = -shadowElevation
    val lowerOffset = shadowElevation
    val radius = 4.dp
    val spread = 0.dp
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(35.dp))
            .padding(5.dp)
            .fillMaxWidth()
            .height(58.dp)
            .dropShadow(
                RoundedCornerShape(35.dp),
                shadow = androidx.compose.ui.graphics.shadow.Shadow(
                    radius = radius,
                    color = lightShadow,
                    spread = spread,
                    offset = DpOffset(upperOffset, upperOffset)
                ),
            )
            .dropShadow(
                RoundedCornerShape(35.dp),
                shadow = androidx.compose.ui.graphics.shadow.Shadow(
                    radius = radius,
                    color = darkShadow,
                    spread = spread,
                    offset = DpOffset(lowerOffset, lowerOffset)
                ),
            )
            .background(
                color = bgColor,
                shape = RoundedCornerShape(35.dp)
            )
            .border(borderWidth, borderColor, RoundedCornerShape(35.dp))
            .padding(horizontal = 0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextButton(
                onClick = { toSelectAll(showAllSelect) }, colors = ButtonDefaults.textButtonColors(
                    contentColor = textColor

                )
            ) {
                Text(if (showAllSelect) "全选" else "取消全选")
            }
            TextButton(
                { toSelectRead(showAllRead) }, colors = ButtonDefaults.textButtonColors(
                    contentColor = textColor

                )
            ) {
                Text(if (showAllRead) "全已读" else "已读")
            }
            TextButton(
                { toDelete() }, enabled = enableDelete, colors = ButtonDefaults.textButtonColors(
                    contentColor = textColor,
                    disabledContentColor = disabledTextColor

                )
            ) {
                Text("删除")
            }
        }
    }

}

@Composable
fun FloatShadowBar(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 1.dp,
    contentColor: Color = Color.White.copy(0.65f),
    content: @Composable () -> Unit,
) {
    val bgColor = contentColor
    val lightShadow = Color(0xA6FFFFFF)
    val darkShadow = Color(0x14000001)
    val upperOffset = -shadowElevation
    val lowerOffset = shadowElevation
    val radius = 4.dp
    val spread = 0.dp
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(35.dp))
            .padding(5.dp)
            .fillMaxWidth()
            .height(42.dp)
            .dropShadow(
                RoundedCornerShape(35.dp),
                shadow = androidx.compose.ui.graphics.shadow.Shadow(
                    radius = radius,
                    color = lightShadow,
                    spread = spread,
                    offset = DpOffset(upperOffset, upperOffset)
                ),
            )
            .dropShadow(
                RoundedCornerShape(35.dp),
                shadow = androidx.compose.ui.graphics.shadow.Shadow(
                    radius = radius,
                    color = darkShadow,
                    spread = spread,
                    offset = DpOffset(lowerOffset, lowerOffset)
                ),
            )
            .background(
                color = bgColor,
                shape = RoundedCornerShape(35.dp)
            )
            .border(borderWidth, borderColor, RoundedCornerShape(35.dp))
            .padding(horizontal = 0.dp)
    ) {
        content()
    }


}

@Composable
fun BlurButton(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    size: Dp = 72.dp,
    borderColor: Color = Color.White,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 1.dp,
    iconPainter: Painter,
    normalTint: Color = Color.Black,
    selectTint: Color = Color(0xFF3B47DD),
    onClick: () -> Unit = {},
) {
    val bgColor = Color(0xA6FFFFFF)
    val lightShadow = Color(0xA6FFFFFF)
    val darkShadow = Color(0x14000001)
    val upperOffset = -shadowElevation
    val lowerOffset = shadowElevation
    val radius = 4.dp
    val spread = 0.dp
    val density = LocalDensity.current



    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .padding(5.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
                onExpandedChange(!expanded)

            }
    ) {
        // 外圆边框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dropShadow(
                    CircleShape,
                    shadow = androidx.compose.ui.graphics.shadow.Shadow(
                        radius = radius,
                        color = lightShadow,
                        spread = spread,
                        offset = DpOffset(upperOffset, upperOffset)
                    ),
                )
                .dropShadow(
                    CircleShape,
                    shadow = androidx.compose.ui.graphics.shadow.Shadow(
                        radius = radius,
                        color = darkShadow,
                        spread = spread,
                        offset = DpOffset(lowerOffset, lowerOffset)
                    ),

                    )
                .background(bgColor, CircleShape)
                .border(borderWidth, borderColor, CircleShape)

//                .blur(30.dp, BlurredEdgeTreatment.Unbounded)
        )

        // 内圆：半透明 + 高斯模糊（兼容 Android 9+）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(borderWidth) // 留出边框 + 间距
                .clip(CircleShape)

//                .background(Color.White.copy(alpha = innerAlpha))
        )

        // 中心 + 号（旋转）
        // 中心图标（支持旋转）
        // 中心 + 图片（旋转）
        Image(
            painter = iconPainter,
            contentDescription = "添加",
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(if (expanded) selectTint else normalTint) // 可选：强制黑色
        )
    }

}

/**
 * 可复用的「模糊 FAB」按钮
 *
 * @param modifier               外部传入的 Modifier
 * @param size                   按钮尺寸（默认 72.dp）
 * @param blurRadius             模糊半径（越大越模糊，性能越差，推荐 12~20）
 * @param innerAlpha             内圆透明度（0f~1f，默认 0.65f）
 * @param borderColor            外圆边框颜色（默认白色）
 * @param borderWidth            外圆边框宽度（默认 1.dp）
 * @param shadowElevation        外阴影高度（默认 12.dp）
 * @param onClick                点击回调（展开/收起由内部控制）
 * @param popupContent           弹出窗内容（Lambda）
 */
@Composable
fun BlurFabButton(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    blurRadius: Float = 18f,
    innerAlpha: Float = 0.35f,
    borderColor: Color = Color.White,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 1.dp,
    addIconPainter: Painter,
    onClick: () -> Unit = {},
    popupContent: @Composable (dismiss: () -> Unit) -> Unit
) {
    val bgColor = Color(0xA6FFFFFF)
    val lightShadow = Color(0xA6FFFFFF)
    val darkShadow = Color(0x14000001)
    val upperOffset = -shadowElevation
    val lowerOffset = shadowElevation
    val radius = 4.dp
    val spread = 0.dp
    val density = LocalDensity.current
    var expanded by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val fadeDurationMs = 180
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(fadeDurationMs),
        label = "popup alpha"
    )
    val scope = rememberCoroutineScope()
//    val shadowElevationPx = with(density) { shadowElevation.toPx() }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) -45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "fab rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .padding(5.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
                expanded = true
                visible = true
            }
    ) {
        // 外圆边框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dropShadow(
                    CircleShape,
                    shadow = androidx.compose.ui.graphics.shadow.Shadow(
                        radius = radius,
                        color = lightShadow,
                        spread = spread,
                        offset = DpOffset(upperOffset, upperOffset)
                    ),
                )
                .dropShadow(
                    CircleShape,
                    shadow = androidx.compose.ui.graphics.shadow.Shadow(
                        radius = radius,
                        color = darkShadow,
                        spread = spread,
                        offset = DpOffset(lowerOffset, lowerOffset)
                    ),

                    )
                .background(bgColor, CircleShape)
                .border(borderWidth, borderColor, CircleShape)

//                .blur(30.dp, BlurredEdgeTreatment.Unbounded)
        )

        // 内圆：半透明 + 高斯模糊（兼容 Android 9+）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(borderWidth) // 留出边框 + 间距
                .clip(CircleShape)

//                .background(Color.White.copy(alpha = innerAlpha))
        )

        // 中心 + 号（旋转）
        // 中心图标（支持旋转）
        // 中心 + 图片（旋转）
        Image(
            painter = addIconPainter,
            contentDescription = "添加",
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.Center)
                .graphicsLayer { rotationZ = rotation },
            contentScale = ContentScale.Fit,
            colorFilter = if (expanded) ColorFilter.tint(Color(0xFF3B47DD)) else ColorFilter.tint(
                Color.Black
            ) // 可选：强制黑色
        )
    }

    // Popup
    if (expanded) {
        Popup(
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    // 边距与间隙
                    val marginPx = with(density) { 16.dp.roundToPx() }
                    val gapPx = with(density) { 8.dp.roundToPx() }

                    // X：右对齐到锚点右侧
                    var x = anchorBounds.right - popupContentSize.width
                    x = x.coerceIn(marginPx, windowSize.width - popupContentSize.width - marginPx)

                    // Y：紧贴锚点下方
                    var y = anchorBounds.bottom + gapPx
                    y = y.coerceIn(marginPx, windowSize.height - popupContentSize.height - marginPx)

                    return IntOffset(x, y)
                }
            },
            onDismissRequest = {
                // 外部点击关闭：走淡出动画后再真正关闭
                scope.launch {
                    visible = false
                    delay(fadeDurationMs.toLong())
                    expanded = false
                }
            },
            properties = PopupProperties(focusable = true)
        ) {
            // 仅渲染实际卡片内容，避免 fillMaxSize 导致弹窗尺寸=全屏
            val requestDismissWithFade: () -> Unit = {
                scope.launch {
                    visible = false
                    delay(fadeDurationMs.toLong())
                    expanded = false
                }
            }
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.graphicsLayer(alpha = alpha)
            ) {
                popupContent(requestDismissWithFade)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MorePopUpPreview() {
    MorePopUp(onItemClick = {})
}

data class MorePopBroadItem(val icon: Painter, val label: String)

@Composable
private fun rememberDefaultMorePopItems(): List<MorePopBroadItem> {
    return listOf(
        MorePopBroadItem(icon = painterResource(R.mipmap.ic_pop_search), label = "搜索"),
        MorePopBroadItem(
            icon = painterResource(R.mipmap.ic_pop_add_group),
            label = "建群聊"
        ),
        MorePopBroadItem(
            icon = painterResource(R.mipmap.ic_pop_add_contact),
            label = "加好友"
        ),
        MorePopBroadItem(icon = painterResource(R.mipmap.ic_pop_scan), label = "扫一扫")
    )
}

@Composable
fun MorePopUp(
    items: List<MorePopBroadItem> = rememberDefaultMorePopItems(),
    onItemClick: (Int) -> Unit = {}
) {
    val bgColor = Color(0xA6FFFFFF)
    val lightShadow = Color(0xA6FFFFFF)
    val darkShadow = Color(0x14000001)
    val upperOffset = (-2).dp
    val lowerOffset = 2.dp
    val radius = 12.dp
    val spread = 0.dp

    Box(
        modifier = Modifier
            .wrapContentSize()
            .dropShadow(
                RoundedCornerShape(16.dp),
                shadow = androidx.compose.ui.graphics.shadow.Shadow(
                    radius = radius,
                    color = lightShadow,
                    spread = spread,
                    offset = DpOffset(upperOffset, upperOffset)
                ),
            )
            .dropShadow(
                RoundedCornerShape(16.dp),
                shadow = androidx.compose.ui.graphics.shadow.Shadow(
                    radius = radius,
                    color = darkShadow,
                    spread = spread,
                    offset = DpOffset(lowerOffset, lowerOffset)
                ),
            )
            .background(
                color = bgColor,
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .padding(vertical = 10.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemClick(index) },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.Fit,
//                        colorFilter = ColorFilter.tint(Color(0xFF222222))
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = item.label,
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W400
                    )
                }
            }
        }
    }
}


@Composable
fun TipToInfoPopUp(onDismiss: () -> Unit, okClick: () -> Unit) {
    Popup(
        alignment = Alignment.Center,
        properties = PopupProperties(
            focusable = true,
        ),
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 40.dp)
                .clip(RoundedCornerShape(16.dp))

        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)                                   // 你想要的高度
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(R.drawable.bg_dialog_gradient_top),
                        contentDescription = "dialog_top",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxWidth()

                    )
                    Text(
                        "请补充你的信息",
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        color = Color.Black.copy(0.9f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)    // 纯白
                ) {
                    // 这里放你的弹窗内容
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(Modifier.height(14.dp))
                        Text("前往个人信息设置", color = Color.Black.copy(0.65f))
                        Spacer(Modifier.height(40.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDismiss.invoke() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF3B47DD),
                                ),
                                border = BorderStroke(1.dp, Color(0xFF3B47DD))
                            ) {
                                Text("取消")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            ElevatedButton(
                                onClick = { okClick.invoke() },
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color(0xFF3B47DD),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("好的")
                            }
                        }
                        Spacer(Modifier.height(24.dp))

                    }

                }


            }

        }
    }

}

@Preview(showBackground = true)
@Composable
fun TipToInfoPopUpPreview() {
    TipToInfoPopUp(onDismiss = {}, okClick = {})
}


@Preview
@Composable
fun BlurButtonPreview() {
    BlurButton(
        iconPainter = painterResource(R.mipmap.ic_manager_black),
        expanded = false,
        onExpandedChange = {})
}

@Preview
@Composable
fun FloatManagerPreview() {
    FloatManagerBar(toSelectAll = {}, toSelectRead = {}, toDelete = {})
}
