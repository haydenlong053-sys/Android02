package com.cboda.intelligent.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

val MyFontNumBoldFamily = FontFamily(
    Font(com.cboda.baselibrary.R.font.broad_num_bold, FontWeight.Normal),
    // ... 更多字重
)

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
        "0"
    }
}

// 将输入金额反推回原单位数值（例如输入 1.23, decimals 18 -> 1230000000000000000）
fun getOriginalAmount(
    amount: String,
    decimals: Int
): String {
    if (amount.isEmpty()) return "0"
    return try {
        BigDecimal(amount)
            .movePointRight(decimals)
            .stripTrailingZeros()
            .toPlainString()
    } catch (e: Exception) {
        "0"
    }
}

fun formatSixDecimalValue(value: String?,decimals: Int = 6): String {
    if (value.isNullOrEmpty()) return "0"
    return try {
        val d = value.toDouble()
        if (d == 0.0) "0"
        else {
            val pattern = "0." + "#".repeat(decimals)
            val formatted = DecimalFormat(pattern, DecimalFormatSymbols(java.util.Locale.US)).format(d)
            formatted
        }
    } catch (e: Exception) {
        "0"
    }
}

fun formatSixDecimalChange(value: String?, decimals: Int = 6): String {
    if (value.isNullOrEmpty()) return "+0%"
    return try {
        val d = value.toDouble()
        if (d == 0.0) "+0%"
        else {
            val pattern = "0." + "#".repeat(decimals)
            val formatted = DecimalFormat(pattern, DecimalFormatSymbols(java.util.Locale.US)).format(d)
            val sign = if (d > 0) "+" else ""
            "$sign$formatted%"
        }
    } catch (e: Exception) {
        "+0%"
    }
}
