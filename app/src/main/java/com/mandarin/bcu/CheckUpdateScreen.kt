package com.mandarin.bcu

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.asynchs.CheckApk
import common.system.fake.ImageBuilder
import java.io.*
import java.util.*

open class CheckUpdateScreen : AppCompatActivity() {
    private val LIB_REQUIRED = StaticStore.LIBREQ
    private val PATH = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/apk/"
    private var path: String? = null
    private var config = false

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
            ed.putInt("Orientation", 0)
            ed.putBoolean("Lay_Port", true)
            ed.putBoolean("Lay_Land", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        if (!shared.contains("apktest")) {
            ed.putBoolean("apktest", true)
            ed.apply()
        }

        if (!shared.contains("default_level")) {
            ed.putInt("default_level", 50)
            ed.apply()
        }

        if (!shared.contains("apktest")) {
            ed.putBoolean("apktest", false)
            ed.apply()
        }

        if (!shared.contains("Language")) {
            ed.putInt("Language", 0)
            ed.apply()
        }

        if (!shared.contains("frame")) {
            ed.putBoolean("frame", true)
            ed.apply()
        }

        if (!shared.contains("Orientation")) {
            ed.putInt("Orientation", 0)
            ed.apply()
        }

        if (!shared.contains("Lay_Port")) {
            ed.putBoolean("Lay_Port", true)
            ed.apply()
        }

        if (!shared.contains("Lay_Land")) {
            ed.putBoolean("Lay_Land", true)
            ed.apply()
        }

        if (!shared.contains("Skip_Text")) {
            ed.putBoolean("Skip_Text", false)
            ed.apply()
        }

        if (!shared.contains("upload")) {
            ed.putBoolean("upload", false)
            ed.apply()
        }

        if (!shared.contains("ask_upload")) {
            ed.putBoolean("ask_upload", true)
            ed.apply()
        }

        if (!shared.contains("music")) {
            ed.putBoolean("music", true)
            ed.apply()
        }

        if (!shared.contains("mus_vol")) {
            ed.putInt("mus_vol", 99)
            ed.apply()
        }

        if (!shared.contains("SE")) {
            ed.putBoolean("SE", true)
            ed.apply()
        }

        if (!shared.contains("se_vol")) {
            ed.putInt("se_vol", 99)
            ed.apply()
        }

        if (!shared.contains("DEV_MODE")) {
            ed.putBoolean("DEV_MODE", false)
            ed.apply()
        }

        when {
            shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        val preferences = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter(StaticStore.LOGPATH, preferences.getBoolean("upload", false) || preferences.getBoolean("ask_upload", true)))

        setContentView(R.layout.activity_check_update_screen)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (MainActivity.isRunning) finish()

        deleter(File(PATH))

        val result = intent

        if (result.extras != null) {
            val extra = result.extras

            config = extra!!.getBoolean("Config")
        }

        val checkstate = findViewById<TextView>(R.id.mainstup)
        val mainprog = findViewById<ProgressBar>(R.id.mainprogup)
        val retry = findViewById<Button>(R.id.checkupretry)

        retry.visibility = View.GONE

        path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU"

        ImageBuilder.builder = BMBuilder()

        retry.setOnClickListener {
            if (connectivityManager.activeNetworkInfo != null) {
                retry.visibility = View.GONE
                mainprog.visibility = View.VISIBLE
                val lang = false
                val checkApk = CheckApk(path ?: Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU", lang, this@CheckUpdateScreen, cando())
                checkApk.execute()
            } else {
                StaticStore.showShortMessage(this@CheckUpdateScreen, R.string.needconnect)
            }
        }

        if (connectivityManager.activeNetworkInfo != null) {
            if (!config) {
                val lang = false
                val checkApk = CheckApk(path ?: Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU", lang, this, cando())
                checkApk.execute()
            } else {
                val lang = false
                val checkApk = CheckApk(path ?: Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU", lang, this, cando(), config)
                checkApk.execute()
            }
        } else {
            if (cando()) {
                val lang = false
                CheckApk(path ?: Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU", lang, this, cando(), config).execute()

            } else {
                mainprog.visibility = View.GONE
                retry.visibility = View.VISIBLE
                checkstate.setText(R.string.main_internet_no)
                StaticStore.showShortMessage(this, R.string.needconnect)
            }
        }
    }

    private fun cando(): Boolean {
        val infopath = path!! + "/files/info/"
        val filename = "info_android.ini"

        val f = File(infopath, filename)

        return if (f.exists()) {
            try {
                var line: String?

                val fis = FileInputStream(f)
                val isr = InputStreamReader(fis)
                val br = BufferedReader(isr)

                val lines = ArrayList<String>()

                while (true) {
                    line = br.readLine()

                    if(line == null) break

                    lines.add(line)
                }

                try {
                    val libs = TreeSet(listOf(*lines[2].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

                    for (s in LIB_REQUIRED)
                        if (!libs.contains(s))
                            return false

                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                false
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }

        } else {
            false
        }
    }

    private fun deleter(f: File) {
        if (f.isDirectory)
            for (g in f.listFiles())
                deleter(g)
        else
            f.delete()
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language", 0) ?: 0

        val config = Configuration()

        var language = StaticStore.lang[lang]

        if (language == "")
            language = Resources.getSystem().configuration.locales.get(0).toString()

        config.setLocale(Locale(language))

        applyOverrideConfiguration(config)

        super.attachBaseContext(LocaleManager.langChange(newBase, shared?.getInt("Language", 0) ?: 0))
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
        mustDie(this)
    }

    fun mustDie(`object`: Any) {
        if (MainActivity.watcher != null) {
            MainActivity.watcher!!.watch(`object`)
        }
    }
}
