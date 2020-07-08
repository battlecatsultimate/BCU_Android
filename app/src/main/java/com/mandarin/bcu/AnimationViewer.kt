package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.unit.asynchs.Adder
import common.system.fake.ImageBuilder
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

class AnimationViewer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        when {
            shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

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

        if (!shared.getBoolean("DEV_MODE", false)) {
            AppWatcher.config = AppWatcher.config.copy(enabled = false)
            LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        } else {
            AppWatcher.config = AppWatcher.config.copy(enabled = true)
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }

        DefineItf.check(this)

        setContentView(R.layout.activity_animation_viewer)

        ImageBuilder.builder = BMBuilder()

        val back = findViewById<FloatingActionButton>(R.id.animbck)
        val search = findViewById<FloatingActionButton>(R.id.animsch)

        back.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                StaticStore.filterReset()
                finish()
            }
        })

        search.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                gotoFilter()
            }
        })

        Adder(this, supportFragmentManager).execute()
    }

    private fun gotoFilter() {
        val intent = Intent(this@AnimationViewer, SearchFilter::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        for(i in StaticStore.filterEntityList.indices) {
            StaticStore.filterEntityList[i] = true
        }

        val schname = findViewById<EditText>(R.id.animschname)

        schname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                StaticStore.entityname = s.toString()

                for(i in StaticStore.filterEntityList.indices) {
                    StaticStore.filterEntityList[i] = true
                }
            }
        })
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

    override fun onBackPressed() {
        super.onBackPressed()
        StaticStore.filterReset()
    }

    companion object {
        const val REQUEST_CODE = 1
    }
}