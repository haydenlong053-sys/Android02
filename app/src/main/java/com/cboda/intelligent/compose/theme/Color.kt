package com.cboda.intelligent.compose.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 浅色
val LightPrimary = Color(0xff3B47DD)  // 主按钮
val LightOnPrimary = Color.White

val LightBackground = Color(0xFFF7F8FA)
val LightOnBackground = Color.Black

val LightSurface = Color.White
val LightOnSurface = Color.Black

val LightSurfaceVariant = Color(0xFFF7F8FA)
val LightOnSurfaceVariant = Color.Black.copy(alpha = 0.65f)  // 次要文字 65%

val LightOutline = Color.Black.copy(alpha = 0.12f)           // 分割线 12%

val LightError = Color(0xffEB534B)   // 警告/拒绝按钮（可替换成你的红）
val LightOnError = Color.White

val LightOutlineNormal=Color(0xff3B47DD)

// 深色
val DarkPrimary = Color(0xff3B47DD)
val DarkOnPrimary = Color.White

val DarkBackground = Color(0xFF191D23)
val DarkOnBackground = Color.White

val DarkSurface = Color(0xFF191D23)
val DarkOnSurface = Color.White

val DarkSurfaceVariant = Color(0xFF28313B)  // 或 #181A1D
val DarkOnSurfaceVariant = Color.White.copy(alpha = 0.65f)

val DarkOutline = Color.White.copy(alpha = 0.12f)

val DarkError = Color(0xffEB534B)
val DarkOnError = Color.Black

val DarkOutlineNormal=Color.White.copy(0.16f)

// 如果需要更多（如 tertiary、次按钮背景等），继续加
// 浅色文字变体（基于 onBackground = Black）
val LightTextPrimary   = LightOnBackground              // #000000 100% → 主要/标题
val LightTextSecondary = LightOnBackground.copy(alpha = 0.65f)  // 次要内容 65%
val LightTextTertiary  = LightOnBackground.copy(alpha = 0.45f)  // 辅助 45%
val LightTextHint      = LightOnBackground.copy(alpha = 0.35f)  // 提示 35%

// 深色文字变体（基于 onBackground = White）
val DarkTextPrimary   = DarkOnBackground
val DarkTextSecondary = DarkOnBackground.copy(alpha = 0.65f)
val DarkTextTertiary  = DarkOnBackground.copy(alpha = 0.45f)
val DarkTextHint      = DarkOnBackground.copy(alpha = 0.35f)

// 如果你想统一用一个对象管理（更清晰）
object AppTextColors {
    val Primary   @Composable get() = if (isSystemInDarkTheme()) DarkTextPrimary   else LightTextPrimary
    val Secondary @Composable get() = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary
    val Tertiary  @Composable get() = if (isSystemInDarkTheme()) DarkTextTertiary  else LightTextTertiary
    val Hint      @Composable get() = if (isSystemInDarkTheme()) DarkTextHint      else LightTextHint
}

object AppLineColors{
    val  OutLineNormal @Composable get() =  if (isSystemInDarkTheme()) DarkOutlineNormal else LightOutlineNormal
}

object  AppButtonSolidColors{
    val OutLineBtnSolid  @Composable get() =  if (isSystemInDarkTheme()) DarkSurface else Color.Transparent

}