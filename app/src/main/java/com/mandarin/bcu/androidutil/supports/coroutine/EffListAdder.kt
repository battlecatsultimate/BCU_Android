package com.mandarin.bcu.androidutil.supports.coroutine

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.adapter.EffListPager
import common.util.pack.EffAnim
import common.util.pack.NyCastle
import common.util.pack.Soul
import java.lang.ref.WeakReference

class EffListAdder(activity: Activity, private val fm: FragmentManager, private val lc: Lifecycle) : CoroutineTask<String>() {
    private val w = WeakReference(activity)

    private val done = "done"

    override fun prepare() {
        val ac = w.get() ?: return
        val sc = ac.findViewById<ViewPager2>(R.id.effpager)

        sc.visibility = View.GONE
    }

    override fun doSomething() {
        val ac = w.get() ?: return

        Definer.define(ac, this::updateProg, this::updateText)

        publishProgress(done)
    }

    override fun progressUpdate(vararg data: String) {
        val a = w.get() ?: return
        val st = a.findViewById<TextView>(R.id.status)

        when (data[0]) {
            StaticStore.TEXT -> st.text = data[1]
            StaticStore.PROG -> {
                val prog = a.findViewById<ProgressBar>(R.id.prog)

                if (data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
            done -> {
                st.setText(R.string.load_process)

                val tab = a.findViewById<TabLayout>(R.id.efftab)
                val pager = a.findViewById<ViewPager2>(R.id.effpager)

                pager.adapter = EffListTab()
                pager.offscreenPageLimit = 3

                TabLayoutMediator(tab, pager) { t, position ->
                    val ac = w.get() ?: return@TabLayoutMediator

                    t.text = when(position) {
                        0 -> ac.getString(R.string.eff_eff)
                        1 -> ac.getString(R.string.eff_soul)
                        2 -> ac.getString(R.string.eff_cannon)
                        else -> ac.getString(R.string.eff_eff)
                    }
                }.attach()

                val bck = a.findViewById<FloatingActionButton>(R.id.effbck)

                bck.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        a.finish()
                    }

                })
            }
        }
    }

    override fun finish() {
        val activity = w.get() ?: return
        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        val st = activity.findViewById<TextView>(R.id.status)
        val pager = activity.findViewById<ViewPager2>(R.id.effpager)

        prog.visibility = View.GONE
        st.visibility = View.GONE
        pager.visibility = View.VISIBLE
    }

    private fun updateText(info: String) {
        val ac = w.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }

    inner class EffListTab : FragmentStateAdapter(fm, lc) {
        init {
            val lit = fm.fragments
            val trans = fm.beginTransaction()

            for (f in lit) {
                trans.remove(f)
            }

            trans.commitAllowingStateLoss()
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> EffListPager.newInstance<EffAnim<*>>(AnimationCView.EFFECT)
                1 -> EffListPager.newInstance<Soul>(AnimationCView.SOUL)
                2 -> EffListPager.newInstance<NyCastle>(AnimationCView.CANNON)
                else -> EffListPager.newInstance<EffAnim<*>>(AnimationCView.EFFECT)
            }
        }
    }
}