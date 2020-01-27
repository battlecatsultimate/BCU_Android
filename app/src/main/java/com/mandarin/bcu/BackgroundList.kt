package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import java.io.File
import java.util.*

class BackgroundList : AppCompatActivity() {
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
        if (shared.getInt("Orientation", 0) == 1) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else if (shared.getInt("Orientation", 0) == 2) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else if (shared.getInt("Orientation", 0) == 0) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        setContentView(R.layout.activity_background_list)
        val listView = findViewById<ListView>(R.id.bglist)
        if (StaticStore.bgnumber == 0) {
            val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/files/org/img/bg/"
            val f = File(path)
            StaticStore.bgnumber = f.list().size - 1
        }
        val names = arrayOfNulls<String>(StaticStore.bgnumber)
        for (i in names.indices) {
            names[i] = getString(R.string.bg_names).replace("_", number(i))
        }
        val adapter = ArrayAdapter(this, R.layout.list_layout_text, names)
        listView.adapter = adapter
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (SystemClock.elapsedRealtime() - StaticStore.bglistClick < StaticStore.INTERVAL) return@OnItemClickListener
            StaticStore.bglistClick = SystemClock.elapsedRealtime()
            val intent = Intent(this@BackgroundList, ImageViewer::class.java)
            intent.putExtra("Path", Environment.getExternalStorageDirectory().absolutePath + "/Android/data/com.mandarin.BCU/files/org/img/bg/bg" + number(position) + ".png")
            intent.putExtra("Img", 0)
            intent.putExtra("BGNum", position)
            startActivity(intent)
        }
        val bck = findViewById<FloatingActionButton>(R.id.bgbck)
        bck.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                finish()
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0
        val config = Configuration()
        var language = StaticStore.lang[lang]

        if(language == "")
            language = Resources.getSystem().configuration.locales.get(0).toString()

        config.setLocale(Locale(language))
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    private fun number(n: Int): String {
        return when (n) {
            in 0..9 -> {
                "00$n"
            }
            in 10..99 -> {
                "0$n"
            }
            else -> {
                n.toString()
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
        mustDie(this)
    }

    fun mustDie(`object`: Any?) {
        if (MainActivity.watcher != null) {
            MainActivity.watcher!!.watch(`object`)
        }
    }
}