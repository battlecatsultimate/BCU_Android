package com.mandarin.bcu.androidutil.unit.asynchs

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.unit.adapters.DynamicExplanation
import com.mandarin.bcu.androidutil.unit.adapters.DynamicFruit
import com.mandarin.bcu.androidutil.unit.adapters.UnitinfPager
import com.mandarin.bcu.androidutil.unit.adapters.UnitinfRecycle
import com.mandarin.bcu.util.Interpret
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.Unit
import java.lang.ref.WeakReference
import java.util.*

class UInfoLoader(activity: Activity, private val data: Identifier<Unit>, private val fm: FragmentManager) : AsyncTask<Void, String, Void>() {
    private val weakActivity: WeakReference<Activity> = WeakReference(activity)
    private val names = ArrayList<String>()
    private val nformid = intArrayOf(R.string.unit_info_first, R.string.unit_info_second, R.string.unit_info_third)
    private val nform = ArrayList<String>()
    private var table: TableTab? = null
    private var explain: ExplanationTab? = null
    private var unitinfRecycle: UnitinfRecycle? = null

    private var added = false
    private var stopper = Object()

    init {
        val ac = weakActivity.get()

        if(ac != null) {
            val u = data.get()

            if(u != null) {
                val fs = u.forms

                if(fs != null) {
                    for(n in fs.indices) {
                        if(n in 0..2) {
                            nform.add(ac.getString(nformid[n]))
                        } else {
                            if(Locale.getDefault().language == "en")
                                nform.add(Interpret.numberWithExtension(n+1, Locale.getDefault().language))
                            else
                                nform.add(ac.getString(R.string.unit_info_forms).replace("_", (n+1).toString()))
                        }
                    }
                }
            }
        }
    }

    override fun onPreExecute() {
        val activity = weakActivity.get() ?: return
        val u = data.get() ?: return

        val fruittext = activity.findViewById<TextView>(R.id.cfinftext)
        val fruitpage: ViewPager = activity.findViewById(R.id.catfruitpager)
        val anim = activity.findViewById<Button>(R.id.animanim)

        if (u.info?.evo == null) {
            fruitpage.visibility = View.GONE
            fruittext.visibility = View.GONE
            anim.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakActivity.get() ?: return null

        Definer.define(activity, this::updateProg, this::updateText)

        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val u = Identifier.get(data) ?: return null

        for (i in u.forms.indices) {
            var name = MultiLangCont.get(u.forms[i]) ?: u.forms[i].name

            if (name == null)
                name = ""

            names.add(name)
        }

        publishProgress("1")

        synchronized(stopper) {
            while(!added) {
                try {
                    stopper.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    break
                }
            }
        }

        val tabs: TabLayout = activity.findViewById(R.id.unitinfexplain)

        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (shared.getBoolean("Lay_Land", false)) {
                table = TableTab(fm, tabs.tabCount, nform.toTypedArray())
                explain = ExplanationTab(fm, tabs.tabCount, nform.toTypedArray())
            } else {
                unitinfRecycle = UnitinfRecycle(activity, names, u.forms, data)
                explain = ExplanationTab(fm, tabs.tabCount, nform.toTypedArray())
            }
        } else {
            if (shared.getBoolean("Lay_Port", true)) {
                table = TableTab(fm, tabs.tabCount, nform.toTypedArray())
                explain = ExplanationTab(fm, tabs.tabCount, nform.toTypedArray())
            } else {
                unitinfRecycle = UnitinfRecycle(activity, names, u.forms, data)
                explain = ExplanationTab(fm, tabs.tabCount, nform.toTypedArray())
            }
        }

        publishProgress("0")

        val treasure: FloatingActionButton = activity.findViewById(R.id.treabutton)
        val mainLayout: ConstraintLayout = activity.findViewById(R.id.unitinfomain)
        val treasuretab: ConstraintLayout = activity.findViewById(R.id.treasurelayout)
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val set = AnimatorSet()
        treasure.setOnClickListener {
            if (!StaticStore.UisOpen) {
                val slider = ValueAnimator.ofInt(0, treasuretab.width).setDuration(300)
                slider.addUpdateListener { animation ->
                    treasuretab.translationX = -(animation.animatedValue as Int).toFloat()
                    treasuretab.requestLayout()
                }
                set.play(slider)
                set.interpolator = DecelerateInterpolator()
                set.start()
                StaticStore.UisOpen = true
            } else {
                val view = activity.currentFocus
                if (view != null) {
                    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    treasuretab.clearFocus()
                }
                val slider = ValueAnimator.ofInt(treasuretab.width, 0).setDuration(300)
                slider.addUpdateListener { animation ->
                    treasuretab.translationX = -(animation.animatedValue as Int).toFloat()
                    treasuretab.requestLayout()
                }
                set.play(slider)
                set.interpolator = AccelerateInterpolator()
                set.start()
                StaticStore.UisOpen = false
            }
        }
        treasuretab.setOnTouchListener { _, _ ->
            mainLayout.isClickable = false
            true
        }
        val fruitpage: ViewPager = activity.findViewById(R.id.catfruitpager)
        if (u.info.evo != null) {
            fruitpage.adapter = DynamicFruit(activity, data)
            fruitpage.offscreenPageLimit = 1
        }
        return null
    }

    override fun onProgressUpdate(vararg results: String) {
        val activity = weakActivity.get() ?: return

        when(results[0]) {
            StaticStore.TEXT -> {
                val st = activity.findViewById<TextView>(R.id.status)

                st.text = results[1]
            }
            StaticStore.PROG -> {
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                if(results[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = results[1].toInt()
            }
            "0" -> {
                val tabs: TabLayout = activity.findViewById(R.id.unitinfexplain)
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

                prog.isIndeterminate = true

                if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (shared.getBoolean("Lay_Land", false)) {
                        setUinfo(activity, tabs)
                    } else {
                        setUinfoR(activity, tabs)
                    }
                } else {
                    if (shared.getBoolean("Lay_Port", true)) {
                        setUinfo(activity, tabs)
                    } else {
                        setUinfoR(activity, tabs)
                    }
                }
            }

            "1" -> {
                val tabs: TabLayout = activity.findViewById(R.id.unitinfexplain)
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                prog.isIndeterminate = true

                val u = data.get() ?: return

                for (i in u.forms.indices) {
                    tabs.addTab(tabs.newTab().setText(nform[i]))
                }

                synchronized(stopper) {
                    added = true
                    stopper.notifyAll()
                }
            }
        }

    }

    override fun onPostExecute(result: Void?) {
        val activity = weakActivity.get() ?: return
        val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (preferences.getBoolean("Lay_Land", false)) {
                val scrollView: NestedScrollView = activity.findViewById(R.id.unitinfscroll)
                scrollView.visibility = View.VISIBLE
            } else {
                val scrollView: NestedScrollView = activity.findViewById(R.id.unitinfscroll)
                scrollView.visibility = View.VISIBLE
                scrollView.postDelayed({ scrollView.scrollTo(0, 0) }, 0)
            }
        } else {
            if (preferences.getBoolean("Lay_Port", false)) {
                val scrollView: NestedScrollView = activity.findViewById(R.id.unitinfscroll)
                scrollView.visibility = View.VISIBLE
            } else {
                val scrollView: NestedScrollView = activity.findViewById(R.id.unitinfscroll)
                scrollView.visibility = View.VISIBLE
                scrollView.postDelayed({ scrollView.scrollTo(0, 0) }, 0)
            }
        }

        val treasuretab: ConstraintLayout = activity.findViewById(R.id.treasurelayout)
        treasuretab.visibility = View.VISIBLE

        if(StaticStore.UisOpen) {
            treasuretab.translationX = treasuretab.width.toFloat()
            treasuretab.requestLayout()
        }

        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        prog.visibility = View.GONE
        val st = activity.findViewById<TextView>(R.id.status)
        st.visibility = View.GONE
        val anim = activity.findViewById<Button>(R.id.animanim)
        anim.visibility = View.VISIBLE
        val tabs: TabLayout = activity.findViewById(R.id.unitinfexplain)
        tabs.getTabAt(StaticStore.unittabposition)?.select()
    }

    private fun updateText(info: String) {
        val ac = weakActivity.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUinfo(activity: Activity, tabs: TabLayout) {
        val u = data.get() ?: return

        val tablePager: ViewPager = activity.findViewById(R.id.unitinftable)

        tablePager.adapter = table
        tablePager.offscreenPageLimit = u.forms.size
        tablePager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))
        tabs.setupWithViewPager(tablePager)

        val viewPager: ViewPager = activity.findViewById(R.id.unitinfpager)
        viewPager.adapter = explain
        viewPager.offscreenPageLimit = u.forms.size
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                tablePager.currentItem = tab.position
                StaticStore.unittabposition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        if (u.info.evo == null)
            viewPager.setPadding(0, 0, 0, StaticStore.dptopx(24f, activity))

        val view = activity.findViewById<View>(R.id.view)
        val view2 = activity.findViewById<View>(R.id.view2)
        val exp = activity.findViewById<TextView>(R.id.unitinfexp)
        if (MultiLangCont.getStatic().FEXP.getCont(u.forms[0]) == null) {
            viewPager.visibility = View.GONE
            view.visibility = View.GONE
            view2.visibility = View.GONE
            exp.visibility = View.GONE
        }
    }

    private inner class TableTab(fm: FragmentManager?, var form: Int, var names: Array<String?>) : FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(i: Int): Fragment {
            return UnitinfPager.newInstance(i, data, names)
        }

        override fun getCount(): Int {
            return form
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return names[position]
        }

    }

    private inner class ExplanationTab(fm: FragmentManager?, var number: Int, var title: Array<String?>) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(i: Int): Fragment {
            return DynamicExplanation.newInstance(i, data, title)
        }

        override fun getCount(): Int {
            return number
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }

    }

    private fun setUinfoR(activity: Activity, tabs: TabLayout) {
        val u = data.get() ?: return

        val recyclerView: RecyclerView = activity.findViewById(R.id.unitinfrec)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = unitinfRecycle
        val viewPager: ViewPager = activity.findViewById(R.id.unitinfpager)
        viewPager.adapter = explain
        viewPager.offscreenPageLimit = 1
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))
        tabs.setupWithViewPager(viewPager)

        if (u.info.evo == null)
            viewPager.setPadding(0, 0, 0, StaticStore.dptopx(24f, activity))

        val view = activity.findViewById<View>(R.id.view)
        val view2 = activity.findViewById<View>(R.id.view2)
        val exp = activity.findViewById<TextView>(R.id.unitinfexp)
        if (MultiLangCont.getStatic().FEXP.getCont(u.forms[0]) == null) {
            viewPager.visibility = View.GONE
            view.visibility = View.GONE
            view2.visibility = View.GONE
            tabs.visibility = View.GONE
            exp.visibility = View.GONE
        }
        tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                StaticStore.unittabposition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}