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
import com.mandarin.bcu.androidutil.stage.asynchs.StageLoader
import common.system.MultiLangCont
import common.util.stage.MapColc
import java.util.*

class StageList : AppCompatActivity() {
    private var mapcode = 0
    private var stid = 0
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
        if (shared.getInt("Orientation", 0) == 1) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else if (shared.getInt("Orientation", 0) == 2) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else if (shared.getInt("Orientation", 0) == 0) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        setContentView(R.layout.activity_stage_list)
        val result = intent
        val extra = result.extras
        if (extra != null) {
            mapcode = extra.getInt("mapcode")
            stid = extra.getInt("stid")
        }
        val name = findViewById<TextView>(R.id.stglistname)
        val mc = MapColc.MAPS[mapcode]
        if (mc != null) {
            val stm = mc.maps[stid]
            var stname = MultiLangCont.SMNAME.getCont(stm)
            if (stname == null) stname = ""
            name.text = stname
        }
        val stageLoader = StageLoader(this, mapcode, stid)
        stageLoader.execute()
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]

        if(language == "")
            language = Resources.getSystem().configuration.locales.get(0).toString()

        config.setLocale(Locale(language))
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
        mustDie(this)
    }

    fun mustDie(`object`: Any?) {
        if (MainActivity.watcher != null) {
            MainActivity.watcher!!.watch(`object`)
        }
    }
}