package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.stage.coroutine.StageAdder
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.util.stage.Stage
import java.util.*

class StageInfo : AppCompatActivity() {
    private var custom = false

    @SuppressLint("SourceLockedOrientationActivity")
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

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

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
            val data = StaticStore.transformIdentifier<Stage>(extra.getString("Data")) ?: return

            custom = extra.getBoolean("custom")

            StageAdder(this, data).execute()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(StaticStore.SisOpen) {
                    val treasure = findViewById<FloatingActionButton>(R.id.stginfotrea)

                    treasure.performClick()
                } else {
                    bck.performClick()
                }
            }
        })
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

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()

        StaticStore.toast = null
    }
}