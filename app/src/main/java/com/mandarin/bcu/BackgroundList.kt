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
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.adapter.BGListPager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_background_list)

        lifecycleScope.launch {
            val tab = findViewById<TabLayout>(R.id.bglisttab)
            val pager = findViewById<ViewPager2>(R.id.bglistpager)
            val bck = findViewById<FloatingActionButton>(R.id.bgbck)
            val progression = findViewById<ProgressBar>(R.id.prog)
            val status = findViewById<TextView>(R.id.status)

            StaticStore.setDisappear(tab, pager, bck)

            withContext(Dispatchers.IO) {
                Definer.define(this@BackgroundList, { _ -> }, { t -> runOnUiThread { status.text = t }})
            }

            pager.adapter = BGListTab()
            pager.offscreenPageLimit = getExistingBGPack()

            val keys = getExistingPack()

            TabLayoutMediator(tab, pager) { t, position ->
                t.text = if (position == 0) {
                    getString(R.string.pack_default)
                } else {
                    val pack = UserProfile.getUserPack(keys[position])

                    if (pack == null) {
                        keys[position]
                    }

                    val name = pack?.desc?.names.toString()

                    name.ifEmpty {
                        keys[position]
                    }
                }
            }.attach()

            if(getExistingBGPack() == 1) {
                tab.visibility = View.GONE

                val collapse = findViewById<CollapsingToolbarLayout>(R.id.bgcollapse)

                val param = collapse.layoutParams as AppBarLayout.LayoutParams

                param.scrollFlags = 0

                collapse.layoutParams = param
            }

            bck.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    finish()
                }
            })

            StaticStore.setAppear(tab, pager, bck)
            StaticStore.setDisappear(progression, status)
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

    private fun getExistingBGPack() : Int {
        var res = 0

        for(p in UserProfile.getAllPacks()) {
            if(p.bgs.list.isNotEmpty())
                res++
        }

        return max(1, res)
    }

    private fun getExistingPack(): ArrayList<String> {
        val list = UserProfile.getAllPacks()

        val res = ArrayList<String>()

        for(k in list) {
            if(k is PackData.DefPack) {
                res.add(Identifier.DEF)
            } else if(k is PackData.UserPack && k.bgs.size() != 0) {
                res.add(k.desc.id)
            }
        }

        return res
    }

    inner class BGListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return BGListPager.newInstance(keys[position])
        }
    }
}