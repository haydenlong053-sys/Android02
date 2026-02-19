package com.cboda.intelligent.compose.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 带 icon 的自定义按钮
 *
 * @param text 按钮文字
 * @param onClick 点击事件
 * @param iconResId icon 资源（可选）
 * @param iconPosition icon 位置：Top / Bottom / Start / End
 * @param bgModifier 外部 modifier
 * @param enabled 是否启用
 * @param loading 是否显示 loading 状态
 * @param width 按钮宽度（默认 wrap）
 * @param height 按钮高度（默认 wrap）
 * @param cornerRadius 圆角半径（当 shape 为 RoundedCornerShape 时有效）
 * @param shape 按钮形状：RoundedCornerShape 或 CircleShape
 * @param backgroundColor 背景颜色
 * @param contentColor 文字和 icon 颜色
 * @param borderStroke 描边（可选）
 * @param iconSize icon 大小
 * @param iconTextSpacing icon 与文字间距
 * @param textStyle 文字样式
 * @param contentPadding 内部 padding
 */
@Composable
fun IconTextButton(
    text: String,
    onClick: () -> Unit,
    iconResId: Int? = null,
    iconPosition: IconPosition = IconPosition.Start,
    bgModifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    width: Dp? = null,
    height: Dp? = null,
    cornerRadius: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderStroke: BorderStroke? = null,
    iconSize: Dp = 24.dp,
    iconTextSpacing: Dp = 8.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = bgModifier
            .then(
                if (width != null) Modifier.width(width) else Modifier.wrapContentWidth()
            )
            .then(
                if (height != null) Modifier.height(height) else Modifier.wrapContentHeight()
            )
            .clip(shape)
            .background(
                if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f)
            )
            .border(
                borderStroke ?: BorderStroke(0.dp, Color.Transparent),
                shape
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = when (iconPosition) {
                IconPosition.Start -> Arrangement.Center
                IconPosition.End -> Arrangement.Center
                IconPosition.Top, IconPosition.Bottom -> Arrangement.Center
            }
        ) {
            // icon 在左/上
            if (iconResId != null && (iconPosition == IconPosition.Start)) {
                IconPart(iconResId, iconSize, contentColor, loading)
                Spacer(Modifier.width(iconTextSpacing))
            }

            // 上下布局时，把文字放中间
            if (iconPosition == IconPosition.Top || iconPosition == IconPosition.Bottom) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (iconPosition == IconPosition.Top) {
                        IconPart(iconResId!!, iconSize, contentColor, loading)
                        Spacer(Modifier.height(iconTextSpacing))
                    }

                    Text(
                        text = if (loading) "加载中..." else text,
                        style = textStyle,
                        color = if (enabled) contentColor else contentColor.copy(alpha = 0.6f)
                    )

                    if (iconPosition == IconPosition.Bottom) {
                        Spacer(Modifier.height(iconTextSpacing))
                        IconPart(iconResId!!, iconSize, contentColor, loading)
                    }
                }
            } else {
                // 左右布局
                Text(
                    text = if (loading) "加载中..." else text,
                    style = textStyle,
                    color = if (enabled) contentColor else contentColor.copy(alpha = 0.6f)
                )
            }

            // icon 在右/下
            if (iconResId != null && (iconPosition == IconPosition.End )) {
               Spacer(Modifier.width(iconTextSpacing))
                IconPart(iconResId, iconSize, contentColor, loading)
            }
        }
    }
}

@Composable
private fun IconPart(
    iconResId: Int,
    size: Dp,
    tint: Color,
    loading: Boolean
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = tint,
            strokeWidth = 3.dp
        )
    } else {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = tint
        )
    }
}