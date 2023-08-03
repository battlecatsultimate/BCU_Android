package com.mandarin.bcu.androidutil.supports

import android.app.Application
import android.content.SharedPreferences
import leakcanary.AppWatcher
import leakcanary.LeakCanary

object LeakCanaryManager {
    fun initCanary(shared: SharedPreferences, application: Application) {
        val devMode = shared.getBoolean("DEV_MOE", false)

        if (devMode) {
            AppWatcher.manualInstall(application)
        }

        LeakCanary.config = LeakCanary.config.copy(dumpHeap = devMode)
        LeakCanary.showLeakDisplayActivityLauncherIcon(devMode)
    }
}