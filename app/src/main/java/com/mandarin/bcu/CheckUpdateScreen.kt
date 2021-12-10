package com.mandarin.bcu

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.coroutine.UpdateCheckDownload
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.system.fake.ImageBuilder
import java.io.File
import java.util.*

open class CheckUpdateScreen : AppCompatActivity() {
    companion object {
        var mustShow = false
    }

    private var config = false
    private lateinit var checker: CoroutineTask<*>

    private lateinit var notifyManager: NotificationManager
    private lateinit var notifyBuilder: NotificationCompat.Builder

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed = shared.edit()

        if (!shared.contains("initial")) {
            initializeAsset()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", false)
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

        if (shared.contains("Orientation")) {
            ed.remove("Orientation")
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

        if (shared.contains("Skip_Text")) {
            ed.remove("Skip_Text")
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

        if(!shared.contains("Reformat0150")) {
            ed.putBoolean("Reformat0150", false)
            ed.apply()
        }

        if(!shared.contains("UI")) {
            ed.putBoolean("UI", true)
            ed.apply()
        }

        if(!shared.contains("ui_vol")) {
            ed.putInt("ui_vol", 99)
            ed.apply()
        }

        if(!shared.contains("gif")) {
            ed.putInt("gif", 100)
            ed.apply()
        }

        if(!shared.contains("rowlayout")) {
            ed.putBoolean("rowlayout", true)
            ed.apply()
        }

        if(!shared.contains("levelLimit")) {
            ed.putInt("levelLimit", 0)
            ed.apply()
        }

        if(!shared.contains("unlockPlus")) {
            ed.putBoolean("unlockPlus", true)
            ed.apply()
        }

        if(!shared.getBoolean("PackReset0137", false)) {
            ed.putBoolean("PackReset0137", true)
            ed.apply()

            deleter(File(StaticStore.getExternalRes(this)))
        }

        if(!shared.contains("bgeff")) {
            ed.putBoolean("bgeff", true)
            ed.apply()
        }

        if(!shared.contains("unitDelay")) {
            ed.putBoolean("unitDelay", true)
            ed.apply()
        }

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter(StaticStore.getExternalLog(this), shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)))

        setContentView(R.layout.activity_check_update_screen)

        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 786)

        if (MainActivity.isRunning)
            finish()

        deleter(File(StaticStore.getExternalPath(this)+"apk/"))

        val result = intent

        if (result.extras != null) {
            val extra = result.extras

            config = extra!!.getBoolean("Config")
        }

        val retry = findViewById<Button>(R.id.retry)
        val prog = findViewById<ProgressBar>(R.id.prog)

        retry.visibility = View.GONE
        prog.isIndeterminate = true

        ImageBuilder.builder = BMBuilder()

        StaticStore.checkFolders(StaticStore.getExternalPath(this), StaticStore.getExternalLog(this), StaticStore.getExternalPack(this))

        notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyBuilder = NotificationCompat.Builder(this, UpdateCheckDownload.NOTIF)

        notifyBuilder.setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
        notifyBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notifyBuilder.setOnlyAlertOnce(true)
        notifyBuilder.setOngoing(true)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val desc = getString(R.string.main_notif_down)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(UpdateCheckDownload.NOTIF, name, importance).apply {
                description = desc
            }

            notifyManager.createNotificationChannel(channel)
        }

        val i = Intent(applicationContext, CheckUpdateScreen::class.java)

        val p = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        notifyBuilder.setContentIntent(p)

        checker = UpdateCheckDownload(this, config, false, notifyManager, notifyBuilder, shared.getBoolean("Reformat0150", true))

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && checkOldFiles() && !shared.getBoolean("Announce_0.13.0",false)) {
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
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }

            confirm.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    ed.putBoolean("Announce_0.13.0",true)
                    ed.apply()

                    checker.execute()

                    dialog.dismiss()
                }

            })
        } else {
            checker.execute()
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
        if(checker.getStatus() != CoroutineTask.Status.DONE)
            checker.cancel()
        notifyBuilder.setOngoing(false)
        if(mustShow)
            notifyManager.notify(UpdateCheckDownload.NOTIF, R.id.downloadnotification, notifyBuilder.build())
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
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

    private fun initializeAsset() {
        var f = File(StaticStore.getExternalAsset(this))

        if(!f.exists())
            f.mkdirs()

        f = File(StaticStore.getExternalAsset(this)+"assets/")

        if(!f.exists())
            f.mkdirs()

        f = File(StaticStore.getExternalAsset(this)+"lang/")

        if(!f.exists())
            f.mkdirs()

        f = File(StaticStore.getExternalAsset(this)+"music/")

        if(!f.exists())
            f.mkdirs()
    }
}


