package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.SparseArray
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
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.music.adapters.MusicListPager
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.round

class MusicList : AppCompatActivity() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE)
        val ed: SharedPreferences.Editor

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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_music_list)

        lifecycleScope.launch {
            //Prepare
            val bck = findViewById<FloatingActionButton>(R.id.musicbck)

            bck.setOnClickListener {
                finish()
            }

            val tab = findViewById<TabLayout>(R.id.mulisttab)
            val pager = findViewById<ViewPager2>(R.id.mulistpager)
            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(tab, pager)

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@MusicList, { _ -> }, { t -> runOnUiThread { st.text = t }})
            }

            st.setText(R.string.load_music_duratoin)

            SoundHandler.initializePlayer(this@MusicList, directPlay = false, repeat = false)

            withContext(Dispatchers.IO) {
                if(StaticStore.musicnames.size != UserProfile.getAllPacks().size || StaticStore.musicData.isEmpty()) {
                    StaticStore.musicnames.clear()
                    StaticStore.musicData.clear()
                    StaticStore.durations.clear()

                    for (p in UserProfile.getAllPacks()) {
                        val names = SparseArray<String>()

                        for (j in p.musics.list.indices) {
                            val m = p.musics.list[j]

                            while(true) {
                                val isLoading = withContext(Dispatchers.Main) {
                                    SoundHandler.MUSIC.isLoading
                                }

                                if(!isLoading) {
                                    break
                                }
                            }

                            withContext(Dispatchers.Main) {
                                suspendCancellableCoroutine {
                                    SoundHandler.setBGM(m.id, onReady = {
                                        StaticStore.durations.add(SoundHandler.MUSIC.duration)

                                        var time = SoundHandler.MUSIC.duration.toFloat() / 1000f

                                        var min = (time / 60f).toInt()

                                        time -= min.toFloat() * 60f

                                        var sec = round(time).toInt()

                                        if (sec == 60) {
                                            min += 1
                                            sec = 0
                                        }

                                        val mins = min.toString()

                                        val secs = if (sec < 10) "0$sec"
                                        else sec.toString()

                                        names.append(m.id.id, "$mins:$secs")

                                        StaticStore.musicData.add(m.id)

                                        it.resume(0) { }
                                    })
                                }
                            }
                        }

                        if(p is PackData.DefPack) {
                            StaticStore.musicnames[Identifier.DEF] = names
                        } else if(p is PackData.UserPack) {
                            StaticStore.musicnames[p.desc.id] = names
                        }
                    }
                }
            }

            SoundHandler.MUSIC.release()

            //Load UI
            st.text = getString(R.string.medal_loading_data)

            prog.isIndeterminate = true

            pager.isSaveEnabled = false
            pager.isSaveFromParentEnabled = false

            pager.adapter = MusicListTab()
            pager.offscreenPageLimit = existingPackNumber()

            val keys = getExistingPack()

            TabLayoutMediator(tab, pager) { t, position ->
                t.text = if(position == 0) {
                    "Default"
                } else {
                    val pack = UserProfile.getPack(keys[position])

                    if(pack == null) {
                        keys[position]
                    }

                    val name = when (pack) {
                        is PackData.DefPack -> {
                            getString(R.string.pack_default)
                        }
                        is PackData.UserPack -> {
                            StaticStore.getPackName(pack.sid)
                        }
                        else -> {
                            ""
                        }
                    }

                    name.ifEmpty {
                        keys[position]
                    }
                }
            }.attach()

            StaticStore.setAppear(pager)
            StaticStore.setDisappear(st, prog)

            if(existingPackNumber() > 1) {
                StaticStore.setAppear(tab)
            } else {
                val collapse = findViewById<CollapsingToolbarLayout>(R.id.muscollapse)

                val param = collapse.layoutParams as AppBarLayout.LayoutParams

                param.scrollFlags = 0

                collapse.layoutParams = param
            }
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

    private fun existingPackNumber() : Int {
        val packs = UserProfile.getAllPacks()

        var res = 0

        for(p in packs) {
            if(p.musics.list.isNotEmpty())
                res++
        }

        return res
    }

    private fun getExistingPack() : ArrayList<String> {
        val packs = UserProfile.getAllPacks()
        val res = ArrayList<String>()

        for(p in packs) {
            if(p.musics.list.isNotEmpty()) {
                if(p is PackData.DefPack) {
                    res.add(Identifier.DEF)
                } else if(p is PackData.UserPack) {
                    res.add(p.sid)
                }
            }
        }

        return res
    }

    inner class MusicListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return MusicListPager.newIntance(keys[position])
        }
    }
}
