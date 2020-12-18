package com.mandarin.bcu.androidutil.castle.coroutine

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.castle.CsListPager
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import com.mandarin.bcu.androidutil.supports.MeasureViewPager
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import java.lang.ref.WeakReference

class CsLoader(ac: Activity, private val fm: FragmentManager) : CoroutineTask<String>() {
    private val done = "done"
    private val w = WeakReference(ac)

    override fun prepare() {
        val ac = w.get() ?: return

        val sc = ac.findViewById<NestedScrollView>(R.id.cslistscroll)

        sc.visibility = View.GONE
    }

    override fun doSomething() {
        val ac = w.get() ?: return

        Definer.define(ac, this::updateProg, this::updateText)

        publishProgress(done)
    }

    override fun progressUpdate(vararg data: String) {
        val activity = w.get() ?: return
        val cslistst = activity.findViewById<TextView>(R.id.status)
        when (data[0]) {
            StaticStore.TEXT -> cslistst.text = data[1]
            StaticStore.PROG -> {
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                if (data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
            done -> {
                val tab = activity.findViewById<TabLayout>(R.id.cslisttab)
                val pager = activity.findViewById<MeasureViewPager>(R.id.cslistpager)

                pager.removeAllViewsInLayout()
                pager.adapter = CsListTab(fm)
                pager.offscreenPageLimit = getExistingCastle()
                pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

                tab.setupWithViewPager(pager)

                if(getExistingCastle() == 1) {
                    tab.visibility = View.GONE

                    val collapse = activity.findViewById<CollapsingToolbarLayout>(R.id.cscollapse)

                    val param = collapse.layoutParams as AppBarLayout.LayoutParams

                    param.scrollFlags = 0

                    collapse.layoutParams = param
                }

                val bck = activity.findViewById<FloatingActionButton>(R.id.csbck)

                bck.setOnClickListener {
                    activity.finish()
                }
            }
        }
    }

    override fun finish() {
        val ac = w.get() ?: return

        val sc = ac.findViewById<NestedScrollView>(R.id.cslistscroll)

        sc.visibility = View.VISIBLE

        val prog = ac.findViewById<ProgressBar>(R.id.prog)
        val st = ac.findViewById<TextView>(R.id.status)

        prog.visibility = View.GONE
        st.visibility = View.GONE
    }

    private fun updateText(info: String) {
        val ac = w.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }

    inner class CsListTab(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val keys: ArrayList<String>

        init {
            val lit = fm.fragments
            val trans = fm.beginTransaction()

            for(f in lit) {
                trans.remove(f)
            }

            trans.commitAllowingStateLoss()

            keys = getExistingPack()
        }

        override fun getCount(): Int {
            return keys.size
        }

        override fun getItem(position: Int): Fragment {
            return CsListPager.newInstance(keys[position])
        }

        override fun getPageTitle(position: Int): CharSequence {
            val def = w.get()?.getString(R.string.pack_default) ?: "Default"

            return when(position) {
                0 -> "$def - RC"
                1 -> "$def - EC"
                2 -> "$def - WC"
                3 -> "$def - SC"
                else -> StaticStore.getPackName(keys[position])
            }
        }

        private fun getExistingPack() : ArrayList<String> {
            val list = UserProfile.getAllPacks()

            val res = ArrayList<String>()

            for(k in list) {
                if(k is PackData.DefPack) {
                    res.add(Identifier.DEF+"-0")
                    res.add(Identifier.DEF+"-1")
                    res.add(Identifier.DEF+"-2")
                    res.add(Identifier.DEF+"-3")
                } else if(k is PackData.UserPack) {
                    if(k.castles.list.isNotEmpty())
                        res.add(k.desc.id)
                }
            }

            return res
        }
    }

    private fun getExistingCastle() : Int {
        val list = UserProfile.getAllPacks()

        var res = 0

        for(k in list) {
            if(k is PackData.DefPack) {
                res += 4
            } else if(k is PackData.UserPack) {
                if(k.castles.list.isNotEmpty())
                    res++
            }
        }

        return res
    }
}