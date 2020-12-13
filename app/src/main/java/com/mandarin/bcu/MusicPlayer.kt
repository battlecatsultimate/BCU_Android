package com.mandarin.bcu

import android.app.Activity
import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.music.coroutine.MusicLoader
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Identifier
import common.util.stage.Music
import java.lang.ref.WeakReference
import java.util.*

class MusicPlayer : AppCompatActivity() {
    companion object {
        var sound = SoundPlayer()
        var music: Identifier<Music>? = null
        var next = false
        var looping = false
        var completed = false
        var paused = false
        var prog = 0
        var volume = 99
        var opened = false

        fun reset() {
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

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        registerReceiver(musicreceive, IntentFilter(Intent.ACTION_HEADSET_PLUG))

        setContentView(R.layout.activity_music_player)

        try {
            val bundle = intent.extras

            if (bundle != null) {
                if (!opened) {
                    music = StaticStore.transformIdentifier(bundle.getString("Data"))
                    opened = true
                }

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

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    class MusicReceiver : BroadcastReceiver {
        private var unregister = false
        private var rotated = true
        private val ac: WeakReference<Activity>?

        constructor() {
            ac = null
        }

        constructor(ac: Activity) {
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
                    0 -> if(sound.isInitialized && !sound.isReleased) {
                        if(sound.isRunning || sound.isPlaying) {
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
