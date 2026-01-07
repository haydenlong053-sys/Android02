package com.cboda.intelligent.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CountdownTimerBroadFlow(
    private val lifecycle: Lifecycle,
    private val totalSeconds: Int
) : DefaultLifecycleObserver {

    private var job: Job? = null
    private val _remainingTime = MutableStateFlow(totalSeconds)
    val remainingTime: StateFlow<Int> get() = _remainingTime

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> get() = _isFinished

    init {
        lifecycle.addObserver(this)
    }

    fun start() {
        cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            val endTime = System.currentTimeMillis() + totalSeconds * 1000L
            while (isActive) {
                val remaining = ((endTime - System.currentTimeMillis()) / 1000).toInt()
                if (remaining >= 0) {
                    _remainingTime.value = remaining
//                    Log.e("remainingTime",remaining.toString())
                    delay(1000L)
                } else {
                    _remainingTime.value = 0
                    _isFinished.value = true
                    break
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cancel()
        lifecycle.removeObserver(this)
    }
}


