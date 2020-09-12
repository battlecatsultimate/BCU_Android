package com.mandarin.bcu

import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.music.asynchs.MusicLoader
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.lang.ref.WeakReference
import java.util.*

class MusicPlayer : AppCompatActivity() {
    companion object {
        var sound : SoundPlayer? = SoundPlayer()
        var posit = 1
        var pid = 0
        var next = false
        var looping = false
        var completed = false
        var paused = false
        var prog = 0
        var volume = 99
        var opened = false

        fun reset() {
            pid = 0
            posit = 1
            next = false
            looping = false
            completed = false
            paused = false
            opened = false
            prog = 0
            volume = 99
        }
    }

    private var musicreceive = MusicReceiver(this)
    private var musicasync: MusicLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

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

        val devMode = shared.getBoolean("DEV_MOE", false)

        AppWatcher.config = AppWatcher.config.copy(enabled = devMode)
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = devMode)
        LeakCanary.showLeakDisplayActivityLauncherIcon(devMode)

        DefineItf.check(this)

        registerReceiver(musicreceive, IntentFilter(Intent.ACTION_HEADSET_PLUG))

        setContentView(R.layout.activity_music_player)

        try {
            val bundle = intent.extras

            if (bundle != null) {
                if (!opened) {
                    posit = bundle.getInt("Music")
                    pid = bundle.getInt("PID")
                    opened = true
                }

                sound = SoundPlayer()

                musicasync = MusicLoader(this)

                musicasync?.execute()
            } else {
                return
            }

        } catch (e: IllegalStateException) {
            StaticStore.showShortMessage(this@MusicPlayer, R.string.music_err)
            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MusicPlayer)
            finish()
        }
    }

    override fun onBackPressed() {
        val back: FloatingActionButton = findViewById(R.id.musicbck)
        back.performClick()
    }

    override fun onDestroy() {
        musicreceive.unregister()
        musicasync?.destroyed = true
        musicasync = null
        unregisterReceiver(musicreceive)
        sound?.release()
        sound = null
        StaticStore.toast = null
        super.onDestroy()
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

    class MusicReceiver(ac: Activity) : BroadcastReceiver() {
        private var unregister = false
        private var rotated = true
        private val ac: WeakReference<Activity>?

        init {
            this.ac = WeakReference(ac)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if(rotated) {
                rotated = false
                return
            }

            if(unregister) return

            if(intent?.action.equals(Intent.ACTION_HEADSET_PLUG)) {

                when(intent?.getIntExtra("state",-1) ?: -1) {
                    0 -> if(sound?.isInitialized == true && sound?.isReleased == false) {
                        if(sound?.isRunning == true || sound?.isPlaying == true) {
                            val play: FloatingActionButton = ac?.get()?.findViewById(R.id.musicplay) ?: return

                            play.performClick()
                        }
                    }
                }
            }
        }

        fun unregister() {
            unregister = true
        }

    }
}
