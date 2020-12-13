package com.mandarin.bcu.androidutil.supports

import android.content.SharedPreferences
import leakcanary.AppWatcher
import leakcanary.LeakCanary

object LeakCanaryManager {
    fun initCanary(shared: SharedPreferences) {
        val devMode = shared.getBoolean("DEV_MOE", false)

        AppWatcher.config = AppWatcher.config.copy(enabled = devMode)
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = devMode)
        LeakCanary.showLeakDisplayActivityLauncherIcon(devMode)
    }
}