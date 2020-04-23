package com.mandarin.bcu

import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.music.asynchs.MusicLoader
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.lang.ref.WeakReference

class MusicPlayer : AppCompatActivity() {
    companion object {
        var sound = SoundPlayer()
        var posit = 0
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
            posit = 0
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

        if (!shared.getBoolean("DEV_MODE", false)) {
            AppWatcher.config = AppWatcher.config.copy(enabled = false)
            LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        } else {
            AppWatcher.config = AppWatcher.config.copy(enabled = true)
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }

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

    class MusicReceiver : BroadcastReceiver {
        private var unregister = false
        private var rotated = true
        private val ac: WeakReference<Activity>?

        constructor(ac: Activity) {
            this.ac = WeakReference(ac)
        }

        constructor() {
            this.ac = null
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
