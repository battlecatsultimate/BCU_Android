package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.stage.asynchs.StageAdder
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

class StageInfo : AppCompatActivity() {
    private var mapcode = 0
    private var stid = 0
    private var posit = 0
    private var custom = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        when {
            shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        if (!shared.getBoolean("DEV_MODE", false)) {
            AppWatcher.config = AppWatcher.config.copy(enabled = false)
            LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        } else {
            AppWatcher.config = AppWatcher.config.copy(enabled = true)
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }

        DefineItf.check(this)

        setContentView(R.layout.activity_stage_info)

        val bck = findViewById<FloatingActionButton>(R.id.stginfobck)

        bck.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                StaticStore.infoOpened = null
                StaticStore.stageSpinner = -1
                finish()
            }
        })

        val result = intent
        val extra = result.extras

        if (extra != null) {
            mapcode = extra.getInt("mapcode")
            stid = extra.getInt("stid")
            posit = extra.getInt("posit")
            custom = extra.getBoolean("custom")
        }

        StageAdder(this, mapcode, stid, posit, custom).execute()
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    override fun onBackPressed() {
        val bck = findViewById<FloatingActionButton>(R.id.stginfobck)

        bck.performClick()
    }

    public override fun onDestroy() {
        super.onDestroy()

        StaticStore.toast = null
    }
}