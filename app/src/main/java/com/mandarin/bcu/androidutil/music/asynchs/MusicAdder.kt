package com.mandarin.bcu.androidutil.music.asynchs

import android.app.Activity
import android.os.AsyncTask
import android.os.Parcelable
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.music.adapters.MusicListPager
import common.util.pack.Pack
import java.lang.ref.WeakReference
import kotlin.math.round

class MusicAdder(activity: Activity, private val fm: FragmentManager?) : AsyncTask<Void, Int, Void>() {
    private val weak = WeakReference(activity)

    override fun onPreExecute() {
        val ac = weak.get() ?: return

        val tab: TabLayout = ac.findViewById(R.id.mulisttab)
        val pager: MeasureViewPager = ac.findViewById(R.id.mulistpager)

        setDisappear(tab, pager)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val ac = weak.get() ?: return null

        if(!StaticStore.musicread) {
            SoundHandler.read(ac)
            StaticStore.musicread = true
        }

        if(StaticStore.musicnames.size != Pack.map.size || StaticStore.musicData.isEmpty()) {
            StaticStore.musicnames.clear()
            StaticStore.musicData.clear()

            for (i in Pack.map) {
                val names = ArrayList<String>()

                for (j in i.value.ms.list.indices) {
                    val f = i.value.ms.list[j]

                    val sp = SoundPlayer()
                    sp.setDataSource(f.toString())
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

                    names.add("$mins:$secs")

                    StaticStore.musicData.add(i.key.toString() + "\\" + j)
                }

                StaticStore.musicnames[i.key] = names
            }
        }

        publishProgress(0)

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val ac = weak.get() ?: return

        val loadt: TextView = ac.findViewById(R.id.mulistloadt)
        val tab: TabLayout = ac.findViewById(R.id.mulisttab)
        val pager: MeasureViewPager = ac.findViewById(R.id.mulistpager)

        loadt.text = ac.getString(R.string.medal_loading_data)

        fm ?: return

        pager.removeAllViewsInLayout()
        pager.adapter = MusicListTab(fm)
        pager.offscreenPageLimit = Pack.map.keys.size
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

        tab.setupWithViewPager(pager)
    }

    override fun onPostExecute(result: Void?) {
        val ac = weak.get() ?: return

        val loadt: TextView = ac.findViewById(R.id.mulistloadt)
        val prog: ProgressBar = ac.findViewById(R.id.mulistprog)
        val tab: TabLayout = ac.findViewById(R.id.mulisttab)
        val pager: MeasureViewPager = ac.findViewById(R.id.mulistpager)

        setAppear(pager)
        setDisappear(loadt, prog)

        if(Pack.map.size > 1) {
            setAppear(tab)
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

    inner class MusicListTab internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val keys: ArrayList<Int>

        init {
            val lit = fm.fragments
            val trans = fm.beginTransaction()

            for(f in lit) {
                trans.remove(f)
            }

            trans.commitAllowingStateLoss()

            keys = getExistingPack()
        }

        override fun getItem(position: Int): Fragment {
            return MusicListPager.newIntance(keys[position])
        }

        override fun getCount(): Int {
            return keys.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val keys = Pack.map.keys.toMutableList()

            return if(position == 0) {
                "Default"
            } else {
                val pack = Pack.map[keys[position]]

                if(pack == null) {
                    keys[position].toString()
                }

                val name = pack?.name ?: ""

                if(name.isEmpty()) {
                    keys[position].toString()
                } else {
                    name
                }
            }
        }

        override fun saveState(): Parcelable? {
            return null
        }

        private fun getExistingPack() : ArrayList<Int> {
            val keys = Pack.map.keys.toMutableList()
            val res = ArrayList<Int>()

            for(k in keys) {
                val p = Pack.map[k] ?: continue

                if(p.ms.list.isNotEmpty()) {
                    res.add(k)
                }
            }

            return res
        }
    }
}