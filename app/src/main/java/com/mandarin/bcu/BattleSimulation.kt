package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.circularreveal.CircularRevealRelativeLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.asynchs.BAdder
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.io.DefineItf
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

class BattleSimulation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences("configuration", Context.MODE_PRIVATE)
        val ed: Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_designNight)
            } else {
                setTheme(R.style.AppTheme_designDay)
            }
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val devMode = shared.getBoolean("DEV_MOE", false)

        AppWatcher.config = AppWatcher.config.copy(enabled = devMode)
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = devMode)
        LeakCanary.showLeakDisplayActivityLauncherIcon(devMode)

        DefineItf.check(this)

        setContentView(R.layout.activity_battle_simulation)

        SoundHandler.inBattle = true

        val intent = intent
        val bundle = intent.extras

        if (bundle != null) {
            val mapcode = bundle.getInt("mapcode")
            val stid = bundle.getInt("stid")
            val posit = bundle.getInt("stage")
            val star = bundle.getInt("star")
            val item = bundle.getInt("item")

            BAdder(this, mapcode, stid, posit, star, item).execute()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onBackPressed() {
        val exit = findViewById<Button>(R.id.battleexit)
        exit.performClick()
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

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    public override fun onPause() {
        super.onPause()
        if (SoundHandler.MUSIC.isInitialized && !SoundHandler.MUSIC.isReleased) {
            if (SoundHandler.MUSIC.isRunning || SoundHandler.MUSIC.isPlaying) {
                SoundHandler.MUSIC.pause()
            }
        }

        SoundHandler.timer?.pause()
    }

    public override fun onResume() {
        super.onResume()
        if (SoundHandler.MUSIC.isInitialized && !SoundHandler.MUSIC.isReleased) {
            if ((!SoundHandler.MUSIC.isRunning || !SoundHandler.MUSIC.isPlaying) && SoundHandler.musicPlay) {
                SoundHandler.MUSIC.start()
            }

            SoundHandler.timer?.resume()
        }
    }
}