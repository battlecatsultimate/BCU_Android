package com.mandarin.bcu.androidutil.adapters

import android.os.SystemClock
import android.view.View

abstract class SingleClick : View.OnClickListener {
    private var mLastClickTime: Long = 0
    abstract fun onSingleClick(v: View?)
    override fun onClick(v: View) {
        val currentTime = SystemClock.uptimeMillis()
        val elapsed = currentTime - mLastClickTime
        if (elapsed <= INTERVAL) {
            return
        }

        mLastClickTime = currentTime
        onSingleClick(v)
    }

    companion object {
        private const val INTERVAL: Long = 1000
    }
}