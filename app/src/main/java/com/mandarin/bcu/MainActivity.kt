package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.asynchs.UploadLogs
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private var sendcheck = false
    private var notshowcheck = false
    private var send = false
    private var show = false

    companion object {
        @JvmField
        var isRunning = false

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed = shared.edit()

        if (!shared.contains("initial")) {
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.putBoolean("frame", true)
            ed.putBoolean("apktest", false)
            ed.putInt("default_level", 50)
            ed.putInt("Language", 0)
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
        deleter(File(Environment.getDataDirectory().absolutePath+"/data/com.mandarin.bcu/temp/"))

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter(StaticStore.getExternalLog(this), shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)))

        setContentView(R.layout.activity_main)

        SoundHandler.musicPlay = shared.getBoolean("music", true)
        SoundHandler.mu_vol = if(shared.getBoolean("music", true)) {
            StaticStore.getVolumScaler(shared.getInt("mus_vol", 99))
        } else {
            0f
        }
        SoundHandler.sePlay = shared.getBoolean("SE", true)
        SoundHandler.se_vol = if(shared.getBoolean("SE", true)) {
            StaticStore.getVolumScaler((shared.getInt("se_vol", 99) * 0.85).toInt())
        } else {
            0f
        }
        StaticStore.upload = shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)

        val result = intent
        var conf = false
        val bundle = result.extras

        if (bundle != null)
            conf = bundle.getBoolean("Config")

        val upath = Environment.getDataDirectory().absolutePath + "/data/com.mandarin.bcu/upload/"
        val upload = File(upath)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (upload.exists() && upload.listFiles()?.isNotEmpty() == true && connectivityManager.activeNetwork != null) {
            if (shared.getBoolean("ask_upload", true) && !StaticStore.dialogisShowed && !conf) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

                val builder = AlertDialog.Builder(this)
                val inflater = LayoutInflater.from(this)
                val v = inflater.inflate(R.layout.error_dialog, null)

                builder.setView(v)

                val yes = v.findViewById<Button>(R.id.errorupload)
                val no = v.findViewById<Button>(R.id.errorno)
                val group = v.findViewById<RadioGroup>(R.id.radio)
                val donotshow = v.findViewById<RadioButton>(R.id.radionotshow)
                val always = v.findViewById<RadioButton>(R.id.radiosend)

                always.setOnClickListener {
                    if (sendcheck && send) {
                        group.clearCheck()

                        val editor = shared.edit()

                        editor.putBoolean("upload", false)
                        editor.putBoolean("ask_upload", true)
                        editor.apply()

                        sendcheck = false
                        notshowcheck = false
                        send = false
                        show = false
                    } else if (sendcheck || send) {
                        send = true
                    }
                }
                donotshow.setOnClickListener {
                    if (notshowcheck && show) {
                        group.clearCheck()

                        val editor = shared.edit()

                        editor.putBoolean("upload", false)
                        editor.putBoolean("ask_upload", true)
                        editor.apply()

                        sendcheck = false
                        notshowcheck = false
                        send = false
                        show = false
                    } else if (notshowcheck || show) {
                        show = true
                    }
                }
                group.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId == donotshow.id) {
                        val editor = shared.edit()

                        editor.putBoolean("upload", false)
                        editor.putBoolean("ask_upload", false)
                        editor.apply()

                        notshowcheck = true
                        sendcheck = false
                        send = false
                        show = false
                    } else {
                        val editor = shared.edit()

                        editor.putBoolean("upload", true)
                        editor.putBoolean("ask_upload", false)
                        editor.apply()

                        notshowcheck = false
                        sendcheck = true
                        send = false
                        show = false
                    }
                }
                val dialog = builder.create()

                dialog.setCancelable(true)
                dialog.show()

                no.setOnClickListener {
                    deleter(upload)
                    dialog.dismiss()
                }

                yes.setOnClickListener {
                    UploadLogs(this@MainActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    StaticStore.showShortMessage(this@MainActivity, R.string.main_err_start)
                    dialog.dismiss()
                }

                dialog.setOnDismissListener {
                    when {
                        shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }

                    StaticStore.dialogisShowed = true
                }
            } else if (shared.getBoolean("upload", false)) {
                StaticStore.showShortMessage(this, R.string.main_err_upload)

                UploadLogs(this@MainActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }

        isRunning = true

        val animbtn = findViewById<Button>(R.id.anvibtn)
        val stagebtn = findViewById<Button>(R.id.stgbtn)
        val emlistbtn = findViewById<Button>(R.id.eninfbtn)
        val basisbtn = findViewById<Button>(R.id.basisbtn)
        val medalbtn = findViewById<Button>(R.id.medalbtn)
        val bgbtn = findViewById<Button>(R.id.bgbtn)
        val config = findViewById<FloatingActionButton>(R.id.mainconfig)
        val musbtn = findViewById<Button>(R.id.mubtn)

        animbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_kasa_jizo), null, null, null)
        stagebtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_castle), null, null, null)
        emlistbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_enemy), null, null, null)
        emlistbtn.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        basisbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_basis), null, null, null)
        medalbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_medal), null, null, null)
        bgbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_bg), null, null, null)
        bgbtn.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        musbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_music), null, null, null)

        animbtn.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                animationview()
            }
        })

        stagebtn.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                stageinfoview()
            }
        })

        config.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                gotoconfig()
            }
        })

        emlistbtn.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                gotoenemyinf()
            }
        })

        basisbtn.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@MainActivity, LineUpScreen::class.java)
                startActivity(intent)
            }
        })

        medalbtn.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@MainActivity, MedalList::class.java)
                startActivity(intent)
            }
        })

        bgbtn.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@MainActivity, BackgroundList::class.java)
                startActivity(intent)
            }
        })

        musbtn.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@MainActivity, MusicList::class.java)
                startActivity(intent)
            }

        })
    }

    private fun animationview() {
        val intent = Intent(this, AnimationViewer::class.java)

        startActivity(intent)
    }

    private fun stageinfoview() {
        val intent = Intent(this, MapList::class.java)

        startActivity(intent)
    }

    private fun gotoconfig() {
        val intent = Intent(this, ConfigScreen::class.java)

        startActivity(intent)
        finish()
    }

    private fun gotoenemyinf() {
        val intent = Intent(this, EnemyList::class.java)

        startActivity(intent)
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
        isRunning = false

        StaticStore.dialogisShowed = false

        StaticStore.clear()

        super.onBackPressed()
    }

    public override fun onDestroy() {
        isRunning = false

        StaticStore.dialogisShowed = false
        StaticStore.toast = null

        super.onDestroy()
    }

    private fun deleter(f: File) {
        if (f.isDirectory) {
            val lit = f.listFiles() ?: return

            for (g in lit)
                deleter(g)
        } else
            f.delete()

    }
}