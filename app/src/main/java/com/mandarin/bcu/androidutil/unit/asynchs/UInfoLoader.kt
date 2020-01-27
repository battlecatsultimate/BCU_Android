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
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.unit.adapters.DynamicExplanation
import com.mandarin.bcu.androidutil.unit.adapters.DynamicFruit
import com.mandarin.bcu.androidutil.unit.adapters.UnitinfPager
import com.mandarin.bcu.androidutil.unit.adapters.UnitinfRecycle
import common.system.MultiLangCont
import java.lang.ref.WeakReference
import java.util.*

class UInfoLoader(private val id: Int, activity: Activity, private val fm: FragmentManager) : AsyncTask<Void?, Int?, Void?>() {
    private val weakActivity: WeakReference<Activity> = WeakReference(activity)
    private val names = ArrayList<String>()
    private val nformid = intArrayOf(R.string.unit_info_first, R.string.unit_info_second, R.string.unit_info_third)
    private val nform = arrayOfNulls<String>(nformid.size)
    private var table: TableTab? = null
    private var explain: ExplanationTab? = null
    private var unitinfRecycle: UnitinfRecycle? = null
    override fun onPreExecute() {
        val activity = weakActivity.get() ?: return
        val fruittext = activity.findViewById<TextView>(R.id.cfinftext)
        val fruitpage: ViewPager = activity.findViewById(R.id.catfruitpager)
        val anim = activity.findViewById<Button>(R.id.animanim)
        if (StaticStore.units[id].info.evo == null) {
            fruitpage.visibility = View.GONE
            fruittext.visibility = View.GONE
            anim.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakActivity.get() ?: return null
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        for (i in StaticStore.units[id].forms.indices) {
            var name = MultiLangCont.FNAME.getCont(StaticStore.units[id].forms[i])
            if (name == null) name = ""
            names.add(name)
        }
        val tabs: TabLayout = activity.findViewById(R.id.unitinfexplain)
        for (i in StaticStore.units[id].forms.indices) {
            tabs.addTab(tabs.newTab().setText(nform[i]))
        }
        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (shared.getBoolean("Lay_Land", false)) {
                table = TableTab(fm, tabs.tabCount, id, nform)
                explain = ExplanationTab(fm, tabs.tabCount, id, nform)
            } else {
                unitinfRecycle = UnitinfRecycle(activity, names, StaticStore.units[id].forms, id)
                explain = ExplanationTab(fm, tabs.tabCount, id, nform)
            }
        } else {
            if (shared.getBoolean("Lay_Port", true)) {
                table = TableTab(fm, tabs.tabCount, id, nform)
                explain = ExplanationTab(fm, tabs.tabCount, id, nform)
            } else {
                unitinfRecycle = UnitinfRecycle(activity, names, StaticStore.units[id].forms, id)
                explain = ExplanationTab(fm, tabs.tabCount, id, nform)
            }
        }
        publishProgress(0)
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
        if (StaticStore.units[id].info.evo != null) {
            fruitpage.adapter = DynamicFruit(activity, id)
            fruitpage.offscreenPageLimit = 1
        }
        return null
    }

    override fun onProgressUpdate(vararg results: Int?) {
        val activity = weakActivity.get() ?: return
        val tabs: TabLayout = activity.findViewById(R.id.unitinfexplain)
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
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
        val prog = activity.findViewById<ProgressBar>(R.id.unitinfprog)
        prog.visibility = View.GONE
        val anim = activity.findViewById<Button>(R.id.animanim)
        anim.visibility = View.VISIBLE
        val tabs: TabLayout = activity.findViewById(R.id.unitinfexplain)
        Objects.requireNonNull(tabs.getTabAt(StaticStore.unittabposition))!!.select()
    }

    private inner class ExplanationTab internal constructor(fm: FragmentManager?, var number: Int, var id: Int, var title: Array<String?>) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(i: Int): Fragment {
            return DynamicExplanation.newInstance(i, id, title)
        }

        override fun getCount(): Int {
            return number
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }

    }

    private inner class TableTab internal constructor(fm: FragmentManager?, var form: Int, var id: Int, var names: Array<String?>) : FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(i: Int): Fragment {
            return UnitinfPager.newInstance(i, id, names)
        }

        override fun getCount(): Int {
            return form
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return names[position]
        }

    }

    private fun setUinfo(activity: Activity, tabs: TabLayout) {
        val tablePager: ViewPager = activity.findViewById(R.id.unitinftable)
        tablePager.adapter = table
        tablePager.offscreenPageLimit = 2
        tablePager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))
        tabs.setupWithViewPager(tablePager)
        val viewPager: ViewPager = activity.findViewById(R.id.unitinfpager)
        viewPager.adapter = explain
        viewPager.offscreenPageLimit = 2
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
        if (StaticStore.units[id].info.evo == null) viewPager.setPadding(0, 0, 0, StaticStore.dptopx(24f, activity))
        val view = activity.findViewById<View>(R.id.view)
        val view2 = activity.findViewById<View>(R.id.view2)
        val exp = activity.findViewById<TextView>(R.id.unitinfexp)
        if (MultiLangCont.FEXP.getCont(StaticStore.units[id].forms[0]) == null) {
            viewPager.visibility = View.GONE
            view.visibility = View.GONE
            view2.visibility = View.GONE
            exp.visibility = View.GONE
        }
    }

    private fun setUinfoR(activity: Activity, tabs: TabLayout) {
        val recyclerView: RecyclerView = activity.findViewById(R.id.unitinfrec)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = unitinfRecycle
        val viewPager: ViewPager = activity.findViewById(R.id.unitinfpager)
        viewPager.adapter = explain
        viewPager.offscreenPageLimit = 1
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))
        tabs.setupWithViewPager(viewPager)
        if (StaticStore.units[id].info.evo == null) viewPager.setPadding(0, 0, 0, StaticStore.dptopx(24f, activity))
        val view = activity.findViewById<View>(R.id.view)
        val view2 = activity.findViewById<View>(R.id.view2)
        val exp = activity.findViewById<TextView>(R.id.unitinfexp)
        if (MultiLangCont.FEXP.getCont(StaticStore.units[id].forms[0]) == null) {
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

    init {
        for (i in nformid.indices) nform[i] = weakActivity.get()!!.getString(nformid[i])
    }
}