package com.cboda.chatui.quickmeet.view

import android.view.MotionEvent

interface OnTouchHandleEvent {

    fun onItemClick(e: MotionEvent) {

    }

    fun updateLocation(translationX: Float, translationY: Float) {

    }
}