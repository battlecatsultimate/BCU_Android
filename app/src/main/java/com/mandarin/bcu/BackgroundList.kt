package com.mandarin.bcu

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.mandarin.bcu.androidutil.BGListPager
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.io.DefineItf
import common.util.pack.Pack
import leakcanary.AppWatcher
import leakcanary.LeakCanary
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

        setContentView(R.layout.activity_background_list)

        val tab = findViewById<TabLayout>(R.id.bglisttab)
        val pager = findViewById<MeasureViewPager>(R.id.bglistpager)

        pager.removeAllViewsInLayout()
        pager.adapter = BGListTab(supportFragmentManager)
        pager.offscreenPageLimit = Pack.map.keys.size
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

        tab.setupWithViewPager(pager)

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
            language = Resources.getSystem().configuration.locales.get(0).language

        config.setLocale(Locale(language))
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    inner class BGListTab(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        init {
            val lit = fm.fragments
            val trans = fm.beginTransaction()

            for (f in lit) {
                trans.remove(f)
            }

            trans.commitAllowingStateLoss()
        }

        private val keys = Pack.map.keys.toMutableList()

        override fun getItem(position: Int): Fragment {
            return BGListPager.newInstance(keys[position])
        }

        override fun getCount(): Int {
            return Pack.map.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val keys = Pack.map.keys.toMutableList()

            return if (position == 0) {
                "Default"
            } else {
                val pack = Pack.map[keys[position]]

                if (pack == null) {
                    keys[position].toString()
                }

                val name = pack?.name ?: ""

                if (name.isEmpty()) {
                    keys[position].toString()
                } else {
                    name
                }
            }
        }

        override fun saveState(): Parcelable? {
            return null
        }
    }
}