package com.cboda.chatui.quickmeet.lifecycle

import androidx.annotation.CallSuper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

abstract class LifecycleImpl(val lifecycleOwner: LifecycleOwner) : DefaultLifecycleObserver {
    protected var mIsDestroy = false
    var mIsInit = false
    var mIsResume = true

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        onPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onDestroy()
    }

    @CallSuper
    open fun onStart() {

    }

    @CallSuper
    open fun onResume() {
        mIsDestroy = false
        mIsResume = true
    }

    @CallSuper
    open fun onPause() {
        mIsResume = false
    }

    @CallSuper
    open fun onStop() {

    }

    @CallSuper
    open fun onDestroy() {
        mIsDestroy = true
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    fun lifecycleResumeState():Boolean {
        return lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }
}