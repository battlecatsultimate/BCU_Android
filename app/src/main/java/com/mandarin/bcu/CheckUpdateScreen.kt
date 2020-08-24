package com.mandarin.bcu

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.asynchs.CheckApk
import common.CommonStatic
import common.system.fake.ImageBuilder
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.io.*
import java.util.*

open class CheckUpdateScreen : AppCompatActivity() {
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

        if (!shared.contains("Announce_0.13.0")) {
            ed.putBoolean("Announce_0.13.0", false)
            ed.apply()
        }

        if (!shared.contains("PackReset0137")) {
            ed.putBoolean("PackReset0137", false)
            ed.apply()
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

        if(!shared.getBoolean("PackReset0137", false)) {
            ed.putBoolean("PackReset0137", true)
            ed.apply()

            deleter(File(StaticStore.getExternalRes(this)))
        }

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter(StaticStore.getExternalLog(this), shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)))

        setContentView(R.layout.activity_check_update_screen)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (MainActivity.isRunning)
            finish()

        deleter(File(StaticStore.getExternalPath(this)+"apk/"))

        val result = intent

        if (result.extras != null) {
            val extra = result.extras

            config = extra!!.getBoolean("Config")
        }

        val mainprog = findViewById<ProgressBar>(R.id.mainprogup)
        val retry = findViewById<Button>(R.id.checkupretry)

        retry.visibility = View.GONE

        path = StaticStore.getExternalPath(this)

        ImageBuilder.builder = BMBuilder()

        StaticStore.checkFolders(StaticStore.getExternalPath(this), StaticStore.getExternalLog(this), StaticStore.getExternalPack(this), StaticStore.getExternalRes(this))

        retry.setOnClickListener {
            if (connectivityManager.activeNetwork != null) {
                retry.visibility = View.GONE
                mainprog.visibility = View.VISIBLE
                val lang = false
                val checkApk = CheckApk(path ?: StaticStore.getExternalPath(this), lang, this@CheckUpdateScreen, cando())
                checkApk.execute()
            } else {
                StaticStore.showShortMessage(this@CheckUpdateScreen, R.string.needconnect)
            }
        }

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if(checkOldFiles() && !shared.getBoolean("Announce_0.13.0",false)) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

                val builder = AlertDialog.Builder(this)
                val inflater = LayoutInflater.from(this)
                val v = inflater.inflate(R.layout.announce_dialog, null)

                builder.setView(v)

                val confirm = v.findViewById<Button>(R.id.anncomfirm)

                val dialog = builder.create()

                dialog.setCancelable(false)

                dialog.show()

                v.post {
                    val w = (resources.displayMetrics.widthPixels*0.75).toInt()

                    dialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
                }

                dialog.setOnDismissListener {
                    when {
                        shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                }

                confirm.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        ed.putBoolean("Announce_0.13.0",true)
                        ed.apply()

                        startCheckUpdates()

                        dialog.dismiss()
                    }

                })
            } else {
                startCheckUpdates()
            }
        } else {
            startCheckUpdates()
        }
    }

    private fun startCheckUpdates() {
        val checkstate = findViewById<TextView>(R.id.mainstup)
        val mainprog = findViewById<ProgressBar>(R.id.mainprogup)
        val retry = findViewById<Button>(R.id.checkupretry)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager.activeNetwork != null) {
            if (!config) {
                val lang = false
                val checkApk = CheckApk(path ?: StaticStore.getExternalPath(this), lang, this, cando())
                checkApk.execute()
            } else {
                val lang = false
                val checkApk = CheckApk(path ?: StaticStore.getExternalPath(this), lang, this, cando(), config)
                checkApk.execute()
            }
        } else {
            if (cando()) {
                val lang = false
                CheckApk(path ?: StaticStore.getExternalPath(this), lang, this, cando(), config).execute()

            } else {
                mainprog.visibility = View.GONE
                retry.visibility = View.VISIBLE
                checkstate.setText(R.string.main_internet_no)
                StaticStore.showShortMessage(this, R.string.needconnect)
            }
        }
    }

    private fun cando(): Boolean {
        val infopath = path!! + "info/"
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

                    for (s in StaticStore.LIBREQ)
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
        if (f.isDirectory) {
            val lit = f.listFiles() ?: return

            for (g in lit)
                deleter(g)

            f.delete()
        } else
            f.delete()
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
        (CommonStatic.ctx as AContext).releaseActivity()
    }

    private fun checkOldFiles() : Boolean {
        val names:List<String> = listOf("apk","lang","music")

        var result = false

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val datapath = StaticStore.getExternalPath(this).replace("files/","")

            val f = File(datapath)

            val lit = f.listFiles() ?: return result

            for(fs in lit) {
                if(fs.name != "files" && names.contains(fs.name)) {
                    result = true

                    deleter(fs)
                }
            }
        }

        return result
    }
}
