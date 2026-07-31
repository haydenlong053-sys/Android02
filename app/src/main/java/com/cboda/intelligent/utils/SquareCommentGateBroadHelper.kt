package com.cboda.intelligent.utils

import android.content.Context
import com.cboda.intelligent.R

object SquareCommentGateBroadHelper {

    const val GATE_TYPE_EVERYONE = "1"
    const val GATE_TYPE_VIP = "2"
    const val GATE_TYPE_CREDIT = "3"
    const val MIN_CREDIT_SCORE = 60
    const val MAX_CREDIT_SCORE = 999

    fun buildDisplayLabel(
        context: Context,
        types: Set<String>,
        creditMin: Int,
    ): String {
        val selected = types.filter { it.isNotBlank() }.toSet()
        if (selected.isEmpty() || selected == setOf(GATE_TYPE_EVERYONE)) {
            return context.getString(com.cboda.baselibrary.R.string.square_publish_everyone)
        }
        val labels = mutableListOf<String>()
        if (GATE_TYPE_VIP in selected) {
            labels.add(context.getString(com.cboda.baselibrary.R.string.square_comment_gate_vip_title))
        }
        if (GATE_TYPE_CREDIT in selected) {
            labels.add(
                context.getString(
                    com.cboda.baselibrary.R.string.square_comment_gate_credit_value,
                    creditMin.coerceIn(MIN_CREDIT_SCORE, MAX_CREDIT_SCORE),
                ),
            )
        }
        return labels.joinToString("、")
    }

    fun buildCommentGateTypes(types: Set<String>): String {
        return types.filter { it.isNotBlank() }
            .sorted()
            .joinToString(",")
            .ifBlank { GATE_TYPE_EVERYONE }
    }

    fun buildCommentCreditMin(types: Set<String>, creditMin: Int): String {
        return if (GATE_TYPE_CREDIT in types) {
            creditMin.coerceIn(MIN_CREDIT_SCORE, MAX_CREDIT_SCORE).toString()
        } else {
            ""
        }
    }
}
