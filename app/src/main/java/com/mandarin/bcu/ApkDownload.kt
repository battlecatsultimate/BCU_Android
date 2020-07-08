package com.mandarin.bcu

import android.Manifest
import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.asynchs.DownloadApk
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

class ApkDownload : AppCompatActivity() {
    private var path = ""

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

        setContentView(R.layout.activity_apk_download)

        path = StaticStore.getExternalPath(this)+"apk/"

        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 786)

        if (ContextCompat.checkSelfPermission(this@ApkDownload, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val result = intent
            if (result.getStringExtra("ver") != null) {
                val ver = result.getStringExtra("ver") ?: StaticStore.VER
                val filestart = "BCU_Android_"
                val apk = ".apk"
                val realpath = path + filestart + ver + apk
                val raw = "?raw=true"
                val url = "https://github.com/battlecatsultimate/bcu-resources/blob/master/resources/android/"
                val realurl = url + filestart + ver + apk + raw
                val retry = findViewById<Button>(R.id.apkretry)
                retry.visibility = View.GONE
                val prog = findViewById<ProgressBar>(R.id.apkprog)
                prog.isIndeterminate = true
                prog.max = 100
                val state = findViewById<TextView>(R.id.apkstate)
                state.setText(R.string.down_state_rea)
                DownloadApk(this@ApkDownload, ver, realurl, path, realpath).execute()
                retry.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        DownloadApk(this@ApkDownload, ver, realurl, path, realpath).execute()
                        retry.visibility = View.GONE
                    }
                })
            }
        }
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

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }
}