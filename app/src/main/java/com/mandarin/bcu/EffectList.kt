package com.mandarin.bcu

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.adapter.EffListPager
import common.CommonStatic
import common.util.anim.AnimU
import common.util.pack.EffAnim
import common.util.pack.NyCastle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class EffectList : AppCompatActivity() {
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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_effect_list)

        lifecycleScope.launch {
            val prog = findViewById<ProgressBar>(R.id.prog)
            val st = findViewById<TextView>(R.id.status)
            val pager = findViewById<ViewPager2>(R.id.effpager)
            val tab = findViewById<TabLayout>(R.id.efftab)

            StaticStore.setDisappear(pager)

            prog.isIndeterminate = false
            prog.max = 10000

            withContext(Dispatchers.IO) {
                Definer.define(this@EffectList, { p -> runOnUiThread { prog.progress = (p * 10000).toInt() }}, { t -> runOnUiThread { st.text = t }})
            }

            st.setText(R.string.load_process)

            pager.adapter = EffListTab()
            pager.offscreenPageLimit = 3

            pager.isSaveEnabled = false
            pager.isSaveFromParentEnabled = false

            TabLayoutMediator(tab, pager) { t, position ->
                t.text = when(position) {
                    0 -> getString(R.string.eff_eff)
                    1 -> getString(R.string.eff_soul)
                    2 -> getString(R.string.eff_akusoul)
                    3 -> getString(R.string.eff_cannon)
                    else -> getString(R.string.eff_eff)
                }
            }.attach()

            val bck = findViewById<FloatingActionButton>(R.id.effbck)

            bck.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    finish()
                }
            })

            StaticStore.setAppear(pager)
            StaticStore.setDisappear(st, prog)
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

    override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    inner class EffListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {

        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> EffListPager.newInstance<EffAnim<*>>(AnimationCView.AnimationType.EFFECT)
                1 -> EffListPager.newInstance<AnimU<*>>(AnimationCView.AnimationType.SOUL)
                2 -> EffListPager.newInstance<AnimU<*>>(AnimationCView.AnimationType.DEMON_SOUL)
                3 -> EffListPager.newInstance<NyCastle>(AnimationCView.AnimationType.CANNON)
                else -> EffListPager.newInstance<EffAnim<*>>(AnimationCView.AnimationType.EFFECT)
            }
        }
    }
}