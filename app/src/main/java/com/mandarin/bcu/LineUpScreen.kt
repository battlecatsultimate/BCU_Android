package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.lineup.asynchs.LUAdder
import java.util.*

class LineUpScreen : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
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
        setContentView(R.layout.activity_line_up_screen)
        val line = LineUpView(this)
        line.id = R.id.lineupView
        val layout = findViewById<LinearLayout>(R.id.lineuplayout)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val w = size.x.toFloat()
        val h = w / 5.0f * 3
        line.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h.toInt())
        layout.addView(line)
        LUAdder(this, supportFragmentManager).execute()
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
        StaticStore.SaveLineUp()
        StaticStore.updateList = false
        StaticStore.filterReset()
        StaticStore.set = null
        StaticStore.lu = null
        StaticStore.combos.clear()
        StaticStore.lineunitname = null
        super.onBackPressed()
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