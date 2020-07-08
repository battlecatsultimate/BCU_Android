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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.asynchs.Downloader
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*
import kotlin.collections.ArrayList

open class DownloadScreen : AppCompatActivity() {
    private var path: String? = null
    private var fileneed: ArrayList<String>? = null
    private var musics: ArrayList<String>? = null
    private var downloading: String? = null
    private var extracting: String? = null

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

        setContentView(R.layout.activity_download_screen)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        path = StaticStore.getExternalPath(this)
        downloading = getString(R.string.down_state_doing)
        extracting = getString(R.string.down_zip_ex)

        val result = intent

        fileneed = result.getStringArrayListExtra("fileneed")
        musics = result.getStringArrayListExtra("music")

        val retry = findViewById<Button>(R.id.retry)

        retry.visibility = View.GONE

        val prog = findViewById<ProgressBar>(R.id.downprog)

        prog.max = 100

        Downloader(path ?: StaticStore.getExternalPath(this), fileneed ?: ArrayList(), musics ?: ArrayList(), downloading ?: "Downloading Files : ", extracting ?: "Extracting Files : ", this@DownloadScreen).execute()

        listeners()
    }

    private fun listeners() {
        val retry: Button = findViewById(R.id.retry)

        retry.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                retry.visibility = View.GONE
                Downloader(path ?: StaticStore.getExternalPath(this@DownloadScreen), fileneed ?: ArrayList(), musics ?: ArrayList(), downloading ?: "Downloading Files : ", extracting ?: "Extracting Files : ", this@DownloadScreen).execute()
            }
        })
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        val state = findViewById<TextView>(R.id.downstate)
        val prog = findViewById<ProgressBar>(R.id.downprog)

        bundle.putString("state", state.text.toString())
        bundle.putInt("prog", prog.progress)

        super.onSaveInstanceState(bundle)
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