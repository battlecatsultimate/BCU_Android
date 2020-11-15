package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.coroutine.BPAdder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.lineup.LineUpView
import common.CommonStatic
import common.battle.BasisSet
import common.util.stage.Stage
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

        val devMode = shared.getBoolean("DEV_MOE", false)

        AppWatcher.config = AppWatcher.config.copy(enabled = devMode)
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = devMode)
        LeakCanary.showLeakDisplayActivityLauncherIcon(devMode)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_battle_prepare)

        val line = LineUpView(this)

        line.id = R.id.lineupView

        val layout = findViewById<LinearLayout>(R.id.preparelineup)

        val w: Float = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            StaticStore.getScreenWidth(this).toFloat() / 2.0f
        else
            StaticStore.getScreenWidth(this).toFloat()

        val h = w / 5.0f * 3

        line.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h.toInt())

        layout.addView(line)

        val intent = intent
        val result = intent.extras

        if (result != null) {
            val data = StaticStore.transformIdentifier<Stage>(result.getString("Data")) ?: return

            if (result.containsKey("selection")) {
                BPAdder(this, data, result.getInt("selection")).execute()
            } else {
                BPAdder(this, data).execute()
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

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }
}