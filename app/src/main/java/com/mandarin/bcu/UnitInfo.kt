package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.Interpret
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.TranslationAnimator
import com.mandarin.bcu.androidutil.unit.adapters.DynamicExplanation
import com.mandarin.bcu.androidutil.unit.adapters.DynamicFruit
import com.mandarin.bcu.androidutil.unit.adapters.UnitInfoPager
import com.mandarin.bcu.androidutil.unit.adapters.UnitinfRecycle
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.Unit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class UnitInfo : AppCompatActivity() {
    private val tabTitle = intArrayOf(R.string.unit_info_first, R.string.unit_info_second, R.string.unit_info_third)

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.clear()

        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: Editor

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

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (shared.getBoolean("Lay_Land", false)) {
                setContentView(R.layout.activity_unit_info)
            } else {
                setContentView(R.layout.activity_unit_infor)
            }
        } else {
            if (shared.getBoolean("Lay_Port", true)) {
                setContentView(R.layout.activity_unit_info)
            } else {
                setContentView(R.layout.activity_unit_infor)
            }
        }

        val result = intent
        val extra = result.extras ?: return

        val data = StaticStore.transformIdentifier<Unit>(extra.getString("Data")) ?: return

        lifecycleScope.launch {
            //Prepare
            val fruitText = findViewById<TextView>(R.id.cfinftext)
            val fruitPager = findViewById<ViewPager>(R.id.catfruitpager)
            val animationButton = findViewById<Button>(R.id.animanim)
            val unitStatPanel = findViewById<CoordinatorLayout>(R.id.unitcoord)
            val back = findViewById<FloatingActionButton>(R.id.unitinfback)
            val treasure = findViewById<FloatingActionButton>(R.id.treabutton)
            val progression = findViewById<ProgressBar>(R.id.prog)
            val unitTitle = findViewById<AutoMarquee>(R.id.unitinfrarname)
            val tabs = findViewById<TabLayout>(R.id.unitinfexplain)
            val treasureTab = findViewById<ConstraintLayout>(R.id.treasurelayout)
            val mainLayout = findViewById<ConstraintLayout>(R.id.unitinfomain)
            val st = findViewById<TextView>(R.id.status)
            val unitTable = findViewById<ViewPager2>(R.id.unitinftable)
            val scrollView = findViewById<NestedScrollView>(R.id.unitinfscroll)
            val separator = findViewById<View>(R.id.view2)
            val separator2 = findViewById<View>(R.id.view)
            val unitExplanationTitle = findViewById<TextView>(R.id.unitinfexp)

            progression.isIndeterminate = true

            StaticStore.setDisappear(unitStatPanel, animationButton, fruitPager, fruitText, separator, separator2, unitExplanationTitle)

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@UnitInfo, { _ -> }, { t -> runOnUiThread { st.text = t }})
            }

            val u = data.get() ?: return@launch

            //Load UI
            val tabNames = u.forms.mapIndexed { i, _ ->
                return@mapIndexed if (i in 0..2) {
                    getString(tabTitle[i])
                } else {
                    if(Locale.getDefault().language == "en")
                        Interpret.numberWithExtension(i+1, Locale.getDefault().language)
                    else
                        getString(R.string.unit_info_forms).replace("_", (i+1).toString())
                }
            }.toTypedArray()

            for (i in u.forms.indices) {
                tabs.addTab(tabs.newTab().setText(tabNames[i]))
            }

            val s = GetStrings(this@UnitInfo)

            unitTitle.text = s.getTitle(u.forms[0])

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (shared.getBoolean("Lay_Land", true)) {
                    setUnitInfoWithPager(u)
                } else {
                    setUnitInfoWithRecycler(u)
                }
            } else {
                if (shared.getBoolean("Lay_Port", true)) {
                    setUnitInfoWithPager(u)
                } else {
                    setUnitInfoWithRecycler(u)
                }
            }

            treasure.setOnClickListener {
                if (!StaticStore.UisOpen) {
                    val animator = TranslationAnimator(treasureTab, AnimatorConst.Axis.X, 300, AnimatorConst.Accelerator.DECELERATE, 0f, treasureTab.width.toFloat())

                    animator.start()

                    StaticStore.UisOpen = true
                } else {
                    val view = currentFocus

                    if (view != null) {
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                        treasureTab.clearFocus()
                    }

                    val animator = TranslationAnimator(treasureTab, AnimatorConst.Axis.X, 300, AnimatorConst.Accelerator.DECELERATE, treasureTab.width.toFloat(), 0f)

                    animator.start()

                    StaticStore.UisOpen = false
                }
            }

            treasureTab.setOnTouchListener { _, _ ->
                mainLayout.isClickable = false

                true
            }

            if (u.info.evo != null) {
                fruitPager.adapter = DynamicFruit(this@UnitInfo, data)

                fruitPager.offscreenPageLimit = 1
            }

            supportActionBar?.elevation = 0F

            if (StaticStore.unitinfreset) {
                StaticStore.unittabposition = 0
                StaticStore.unitinfreset = false
            }

            treasureTab.visibility = View.GONE

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (shared.getBoolean("Lay_Land", false)) {
                    scrollView.visibility = View.GONE

                    unitTable.isFocusable = false
                } else {
                    scrollView.visibility = View.GONE

                    val recyclerView = findViewById<RecyclerView>(R.id.unitinfrec)

                    recyclerView.requestFocusFromTouch()
                }
            } else {
                if (shared.getBoolean("Lay_Port", false)) {
                    scrollView.visibility = View.GONE

                    unitTable.isFocusable = false
                } else {
                    scrollView.visibility = View.GONE

                    val recyclerView = findViewById<RecyclerView>(R.id.unitinfrec)

                    recyclerView.requestFocusFromTouch()
                }
            }

            back.setOnClickListener {
                StaticStore.unitinfreset = true
                StaticStore.UisOpen = false
                finish()
            }

            animationButton.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@UnitInfo, ImageViewer::class.java)

                    StaticStore.formposition = StaticStore.unittabposition

                    intent.putExtra("Img", ImageViewer.ViewerType.UNIT.name)
                    intent.putExtra("Data", JsonEncoder.encode(data).toString())
                    intent.putExtra("Form", StaticStore.formposition)

                    CommonStatic.getConfig().performanceModeAnimation = shared.getBoolean("performanceAnimation", false)

                    startActivity(intent)
                }
            })

            onBackPressedDispatcher.addCallback(this@UnitInfo, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (StaticStore.UisOpen) {
                        treasure!!.performClick()
                    } else {
                        StaticStore.unitinfreset = true

                        finish()
                    }
                }
            })

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (shared.getBoolean("Lay_Land", false)) {
                    scrollView.visibility = View.VISIBLE
                } else {
                    scrollView.visibility = View.VISIBLE

                    scrollView.postDelayed({ scrollView.scrollTo(0, 0) }, 0)
                }
            } else {
                if (shared.getBoolean("Lay_Port", false)) {
                    scrollView.visibility = View.VISIBLE
                } else {
                    scrollView.visibility = View.VISIBLE

                    scrollView.postDelayed({ scrollView.scrollTo(0, 0) }, 0)
                }
            }

            if(StaticStore.UisOpen) {
                treasureTab.translationX = treasureTab.width.toFloat()
                treasureTab.requestLayout()
            }

            StaticStore.setDisappear(st, progression)
            StaticStore.setAppear(animationButton, unitStatPanel, treasureTab)

            if (u.info?.evo != null) {
                StaticStore.setAppear(fruitPager, fruitText, separator2)
            }

            val explanationExist = u.forms.any { f ->
                val arr = MultiLangCont.getStatic().FEXP.getCont(f)

                if (arr != null && arr.any { l -> l.isNotBlank() })
                    return@any true

                val description = f.description.toString()

                if (description.isBlank())
                    return@any false

                val customArr = description.split("<br>")

                customArr.isNotEmpty() && customArr.any { l -> l.isNotBlank() }
            }

            if (explanationExist) {
                StaticStore.setAppear(separator, unitExplanationTitle)
            }

            tabs.getTabAt(StaticStore.unittabposition)?.select()
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

    private fun setUnitInfoWithRecycler(u: Unit) {
        val tabs = findViewById<TabLayout>(R.id.unitinfexplain)
        val recyclerView = findViewById<RecyclerView>(R.id.unitinfrec)
        val viewPager = findViewById<ViewPager2>(R.id.unitinfpager)
        val view = findViewById<View>(R.id.view)
        val view2 = findViewById<View>(R.id.view2)
        val exp = findViewById<TextView>(R.id.unitinfexp)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = UnitinfRecycle(this, ArrayList(u.forms.map { f -> MultiLangCont.get(f) ?: f.names.toString() }), u.forms, u.id)

        val tabNames = u.forms.mapIndexed { i, _ ->
            return@mapIndexed if (i in 0..2) {
                getString(tabTitle[i])
            } else {
                if(Locale.getDefault().language == "en")
                    Interpret.numberWithExtension(i+1, Locale.getDefault().language)
                else
                    getString(R.string.unit_info_forms).replace("_", (i+1).toString())
            }
        }.toTypedArray()

        viewPager.adapter = ExplanationTab(tabs.tabCount, tabNames, u.id)
        viewPager.offscreenPageLimit = 1

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = tabNames[position]
        }.attach()

        if (u.info.evo == null)
            viewPager.setPadding(0, 0, 0, StaticStore.dptopx(24f, this))

        viewPager.isSaveEnabled = false
        viewPager.isSaveFromParentEnabled = false

        if (MultiLangCont.getStatic().FEXP.getCont(u.forms[0]) == null) {
            StaticStore.setDisappear(viewPager, view, view2, tabs, exp)
        }

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position

                StaticStore.unittabposition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setUnitInfoWithPager(u: Unit) {
        val tabs = findViewById<TabLayout>(R.id.unitinfexplain)
        val tablePager = findViewById<ViewPager2>(R.id.unitinftable)
        val viewPager = findViewById<ViewPager2>(R.id.unitinfpager)
        val view = findViewById<View>(R.id.view)
        val view2 = findViewById<View>(R.id.view2)
        val exp = findViewById<TextView>(R.id.unitinfexp)

        val tabNames = u.forms.mapIndexed { i, _ ->
            return@mapIndexed if (i in 0..2) {
                getString(tabTitle[i])
            } else {
                if(Locale.getDefault().language == "en")
                    Interpret.numberWithExtension(i+1, Locale.getDefault().language)
                else
                    getString(R.string.unit_info_forms).replace("_", (i+1).toString())
            }
        }.toTypedArray()

        tablePager.isSaveEnabled = false
        tablePager.isSaveFromParentEnabled = false

        tablePager.adapter = TableTab(tabs.tabCount, tabNames, u.id)
        tablePager.offscreenPageLimit = u.forms.size

        TabLayoutMediator(tabs, tablePager) { tab, position ->
            tab.text = tabNames[position]
        }.attach()

        viewPager.isSaveEnabled = false
        viewPager.isSaveFromParentEnabled = false

        viewPager.adapter = ExplanationTab(tabs.tabCount, tabNames, u.id)
        viewPager.offscreenPageLimit = u.forms.size

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = tabNames[position]
        }.attach()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                tablePager.currentItem = tab.position
                StaticStore.unittabposition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        if (u.info.evo == null)
            viewPager.setPadding(0, 0, 0, StaticStore.dptopx(24f, this))

        if (MultiLangCont.getStatic().FEXP.getCont(u.forms[0]) == null && (u.id.pack == Identifier.DEF || u.forms[0].description.toString().isBlank())) {
            StaticStore.setDisappear(viewPager, view, view2, exp)
        }
    }

    private inner class TableTab(private val form: Int, private val names: Array<String>, private val data: Identifier<Unit>) : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return form
        }

        override fun createFragment(position: Int): Fragment {
            return UnitInfoPager.newInstance(position, data, names)
        }
    }

    private inner class ExplanationTab(private val number: Int, private val title: Array<String>, private val data: Identifier<Unit>) : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return number
        }

        override fun createFragment(position: Int): Fragment {
            return DynamicExplanation.newInstance(position, data, title)
        }
    }
}