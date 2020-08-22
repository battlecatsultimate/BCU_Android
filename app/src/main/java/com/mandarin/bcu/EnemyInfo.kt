package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.JsonParser
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.asynchs.EInfoLoader
import com.mandarin.bcu.androidutil.io.DefineItf
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.PackData
import common.util.lang.MultiLangCont
import common.util.unit.Enemy
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

class EnemyInfo : AppCompatActivity() {
    var treasure: FloatingActionButton? = null
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

        setContentView(R.layout.activity_enemy_info)

        treasure = findViewById(R.id.enemtreasure)

        val scrollView = findViewById<ScrollView>(R.id.eneminfscroll)

        scrollView.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        scrollView.isFocusable = true
        scrollView.isFocusableInTouchMode = true
        scrollView.visibility = View.GONE

        val title = findViewById<TextView>(R.id.eneminftitle)

        val result = intent

        val extra = result.extras

        if (extra != null) {

            val data = JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), PackData.Identifier::class.java) as PackData.Identifier<Enemy>
            val pid = extra.getString("PID")
            val multi = extra.getInt("Multiply")
            val amulti = extra.getInt("AMultiply")

            title.text = MultiLangCont.get(data.get()) ?: data.get().name

            val eanim = findViewById<Button>(R.id.eanimanim)

            eanim.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@EnemyInfo, ImageViewer::class.java)
                    intent.putExtra("Img", 3)
                    intent.putExtra("PID", pid)
                    intent.putExtra("Data", JsonEncoder.encode(data).toString())
                    startActivity(intent)
                }
            })

            if (multi != 0)
                EInfoLoader(this, multi, amulti, data).execute()
            else
                EInfoLoader(this, data).execute()
        }
    }

    override fun onBackPressed() {
        if (StaticStore.EisOpen) treasure!!.performClick() else super.onBackPressed()
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