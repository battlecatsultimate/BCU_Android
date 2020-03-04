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
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.asynchs.EInfoLoader
import common.system.MultiLangCont
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

        if (shared.getInt("Orientation", 0) == 1)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else if (shared.getInt("Orientation", 0) == 2)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        else if (shared.getInt("Orientation", 0) == 0)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

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

            val id = extra.getInt("ID")
            val multi = extra.getInt("Multiply")

            title.text = MultiLangCont.ENAME.getCont(StaticStore.enemies[id])

            val eanim = findViewById<Button>(R.id.eanimanim)

            eanim.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@EnemyInfo, ImageViewer::class.java)
                    intent.putExtra("Img", 3)
                    intent.putExtra("ID", id)
                    startActivity(intent)
                }
            })

            if (multi != 0)
                EInfoLoader(this, id, multi).execute()
            else
                EInfoLoader(this, id).execute()
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

        if(language == "")
            language = Resources.getSystem().configuration.locales.get(0).language

        config.setLocale(Locale(language))
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
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