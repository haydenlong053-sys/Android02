package com.cboda.intelligent.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.math.BigDecimal
import java.math.RoundingMode

fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        enabled = enabled,
        onClick = onClick
    )
}

// 在 composable 外或在 viewModel 裡計算好
fun getFormattedAmount(
    balance: String,
    decimals: Int,
    showDecimals: Int = 6,           // 顯示幾位小數
    minShowDecimals: Int = 2
): String {
    if (balance == "0" || balance.isEmpty()) return "0"

    return try {
        val amount = BigDecimal(balance)
            .divide(
                BigDecimal.TEN.pow(decimals),
                decimals + 10,           // 足夠的精度避免截斷
                RoundingMode.DOWN
            )

        // 格式化顯示（去掉不必要的尾0）
        amount.stripTrailingZeros()
            .setScale(
                amount.scale().coerceAtMost(showDecimals).coerceAtLeast(minShowDecimals),
                RoundingMode.DOWN
            )
            .toPlainString()
    } catch (e: Exception) {
        "Error"
    }
}
