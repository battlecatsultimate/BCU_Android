package com.mandarin.bcu.androidutil.music.coroutine

import android.app.Activity
import android.util.SparseArray
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.music.adapters.MusicListPager
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import java.lang.ref.WeakReference
import kotlin.math.round

class MusicAdder(activity: Activity, private val fm: FragmentManager, private val lc: Lifecycle) : CoroutineTask<String>() {
    private val weak = WeakReference(activity)

    private val done = "done"

    override fun prepare() {
        val ac = weak.get() ?: return

        val tab: TabLayout = ac.findViewById(R.id.mulisttab)
        val pager: ViewPager2 = ac.findViewById(R.id.mulistpager)

        setDisappear(tab, pager)
    }

    override fun doSomething() {
        val ac = weak.get() ?: return

        Definer.define(ac, this::updateProg, this::updateText)

        publishProgress(StaticStore.TEXT, ac.getString(R.string.load_music_duratoin))

        if(StaticStore.musicnames.size != UserProfile.getAllPacks().size || StaticStore.musicData.isEmpty()) {
            StaticStore.musicnames.clear()
            StaticStore.musicData.clear()
            StaticStore.durations.clear()

            for (p in UserProfile.getAllPacks()) {
                val names = SparseArray<String>()

                for (j in p.musics.list.indices) {
                    val m = p.musics.list[j]

                    val f = StaticStore.getMusicDataSource(m) ?: continue

                    val sp = SoundPlayer()

                    sp.setDataSource(f.absolutePath)

                    sp.prepare()

                    StaticStore.durations.add(sp.duration)

                    var time = sp.duration.toFloat() / 1000f

                    sp.release()

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
                }

                if(p is PackData.DefPack) {
                    StaticStore.musicnames[Identifier.DEF] = names
                } else if(p is PackData.UserPack) {
                    StaticStore.musicnames[p.desc.id] = names
                }
            }
        }

        publishProgress(done)
    }

    override fun progressUpdate(vararg data: String) {
        val ac = weak.get() ?: return

        val st = ac.findViewById<TextView>(R.id.status)

        when(data[0]) {
            StaticStore.TEXT -> st.text = data[1]
            StaticStore.PROG -> {
                val prog = ac.findViewById<ProgressBar>(R.id.prog)

                if(data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
            done -> {
                val prog = ac.findViewById<ProgressBar>(R.id.prog)
                val tab: TabLayout = ac.findViewById(R.id.mulisttab)
                val pager: ViewPager2 = ac.findViewById(R.id.mulistpager)

                st.text = ac.getString(R.string.medal_loading_data)

                prog.isIndeterminate = true

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
                                weak.get()?.getString(R.string.pack_default) ?: "Default"
                            }
                            is PackData.UserPack -> {
                                StaticStore.getPackName(pack.sid)
                            }
                            else -> {
                                ""
                            }
                        }

                        if(name.isEmpty()) {
                            keys[position]
                        } else {
                            name
                        }
                    }
                }.attach()
            }
        }
    }

    override fun finish() {
        val ac = weak.get() ?: return

        val loadt: TextView = ac.findViewById(R.id.status)
        val prog: ProgressBar = ac.findViewById(R.id.prog)
        val tab: TabLayout = ac.findViewById(R.id.mulisttab)
        val pager: ViewPager2 = ac.findViewById(R.id.mulistpager)

        setAppear(pager)
        setDisappear(loadt, prog)

        if(existingPackNumber() > 1) {
            setAppear(tab)
        } else {
            val collapse = ac.findViewById<CollapsingToolbarLayout>(R.id.muscollapse)

            val param = collapse.layoutParams as AppBarLayout.LayoutParams

            param.scrollFlags = 0

            collapse.layoutParams = param
        }
    }

    private fun setAppear(vararg view: View) {
        for (v in view) {
            v.visibility = View.VISIBLE
        }
    }

    private fun setDisappear(vararg view: View) {
        for (v in view) {
            v.visibility = View.GONE
        }
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

    private fun updateText(info: String) {
        val ac = weak.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
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

    inner class MusicListTab : FragmentStateAdapter(fm, lc) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return MusicListPager.newIntance(keys[position])
        }
    }
}