package com.cboda.intelligent.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewBroadModel : ViewModel() {

    private val _isScrollingDown = MutableStateFlow(false)
    val isScrollingDown = _isScrollingDown.asStateFlow()

    /**
     * 由 Fragment 中的列表滚动监听器调用。
     * @param isScrollingDown true 表示正在向下滚动，false 表示停止或向上滚动。
     */
    fun setScrollingDirection(isScrollingDown: Boolean) {
        if (_isScrollingDown.value != isScrollingDown) {
            _isScrollingDown.value = isScrollingDown
        }
    }
}