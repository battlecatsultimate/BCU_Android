package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.stage.asynchs.StageLoader
import common.system.MultiLangCont
import common.util.pack.Pack
import common.util.stage.MapColc
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

class StageList : AppCompatActivity() {
    private var mapcode = 0
    private var stid = 0
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

        setContentView(R.layout.activity_stage_list)

        val result = intent
        val extra = result.extras

        if (extra != null) {
            mapcode = extra.getInt("mapcode")
            stid = extra.getInt("stid")
            custom = extra.getBoolean("custom")
        }

        val name = findViewById<TextView>(R.id.stglistname)

        val index = StaticStore.mapcode.indexOf(mapcode)

        val mc = if(index < StaticStore.BCmaps) {
            MapColc.MAPS[mapcode]
        } else {
            val p = Pack.map[mapcode] ?: return

            p.mc
        }

        if (mc != null) {
            val stm = mc.maps[stid]
            var stname = MultiLangCont.SMNAME.getCont(stm) ?: stm.name
            if (stname == null)
                stname = number(stid)
            name.text = stname
        }

        val stageLoader = StageLoader(this, mapcode, stid, custom)

        stageLoader.execute()
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
        val bck = findViewById<FloatingActionButton>(R.id.stglistbck)

        bck.performClick()
    }

    public override fun onDestroy() {
        super.onDestroy()

        StaticStore.toast = null
    }

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }
}