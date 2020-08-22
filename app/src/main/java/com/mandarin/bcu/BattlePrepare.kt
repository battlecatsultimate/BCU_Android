package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.asynchs.BPAdder
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.lineup.LineUpView
import common.battle.BasisSet
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

class BattlePrepare : AppCompatActivity() {
    companion object {
        var rich = false
        var sniper = false
    }

    @SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
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

        setContentView(R.layout.activity_battle_prepare)

        val line = LineUpView(this)

        line.id = R.id.lineupView

        val layout = findViewById<LinearLayout>(R.id.preparelineup)

        val display = windowManager.defaultDisplay
        val size = Point()

        display.getSize(size)

        val w: Float

        w = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            size.x / 2.0f
        else
            size.x.toFloat()

        val h = w / 5.0f * 3

        line.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h.toInt())

        layout.addView(line)

        val intent = intent
        val result = intent.extras

        if (result != null) {
            val mapcode = result.getInt("mapcode")
            val stid = result.getInt("stid")
            val posit = result.getInt("stage")

            if (result.containsKey("selection")) {
                BPAdder(this, mapcode, stid, posit, result.getInt("selection")).execute()
            } else {
                BPAdder(this, mapcode, stid, posit).execute()
            }
        }
    }

    override fun onActivityResult(code: Int, code1: Int, data: Intent?) {
        super.onActivityResult(code, code1, data)
        val line = findViewById<LineUpView>(R.id.lineupView)
        line.updateLineUp()
        val setname = findViewById<TextView>(R.id.lineupname)
        setname.text = setLUName
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

    private val setLUName: String
        get() = BasisSet.current().name + " - " + BasisSet.current().sele.name

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }
}