package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.enemy.adapters.DynamicEmExplanation
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyRecycle
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.TranslationAnimator
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Enemy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class EnemyInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        setContentView(R.layout.activity_enemy_info)

        val result = intent

        val extra = result.extras

        if (extra != null) {
            val data = StaticStore.transformIdentifier<AbEnemy>(extra.getString("Data")) ?: return
            val multi = extra.getInt("Multiply", 100)
            val amulti = extra.getInt("AMultiply", 100)

            val e = Identifier.get(data)

            if (e !is Enemy)
                return

            lifecycleScope.launch {
                val treasure = findViewById<FloatingActionButton>(R.id.enemtreasure)
                val scrollView = findViewById<ScrollView>(R.id.eneminfscroll)
                val title = findViewById<TextView>(R.id.eneminftitle)
                val enemyAnimation = findViewById<Button>(R.id.eanimanim)
                val view1 = findViewById<View>(R.id.enemviewtop)
                val view2 = findViewById<View>(R.id.enemviewbot)
                val viewPager = findViewById<ViewPager>(R.id.eneminfexp)
                val explanationTitle = findViewById<TextView>(R.id.eneminfexptx)
                val st = findViewById<TextView>(R.id.status)
                val progression = findViewById<ProgressBar>(R.id.prog)
                val recyclerView = findViewById<RecyclerView>(R.id.eneminftable)
                val main = findViewById<ConstraintLayout>(R.id.enemmainlayout)
                val treasureLayout: ConstraintLayout = findViewById(R.id.enemtreasuretab)
                val back: FloatingActionButton = findViewById(R.id.eneminfbck)

                StaticStore.setDisappear(view1, view2, explanationTitle, viewPager, enemyAnimation, treasure, back, scrollView)

                progression.isIndeterminate = true

                //Load Data
                withContext(Dispatchers.IO) {
                    Definer.define(this@EnemyInfo, { _ -> }, { t -> runOnUiThread { st.text = t }})
                }

                scrollView.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
                scrollView.isFocusable = true
                scrollView.isFocusableInTouchMode = true
                scrollView.visibility = View.GONE

                title.text = MultiLangCont.get(e) ?: e.names.toString()

                enemyAnimation.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(this@EnemyInfo, ImageViewer::class.java)

                        intent.putExtra("Img", ImageViewer.ViewerType.ENEMY.name)
                        intent.putExtra("Data", JsonEncoder.encode(data).toString())

                        CommonStatic.getConfig().performanceModeAnimation = shared.getBoolean("performanceAnimation", false)

                        startActivity(intent)
                    }
                })

                val enemyRecycle: EnemyRecycle = if (multi != -1 && amulti != -1)
                    EnemyRecycle(this@EnemyInfo, multi, amulti, data)
                else
                    EnemyRecycle(this@EnemyInfo, data)

                recyclerView.layoutManager = LinearLayoutManager(this@EnemyInfo)
                recyclerView.adapter = enemyRecycle

                ViewCompat.setNestedScrollingEnabled(recyclerView, false)

                val explain = DynamicEmExplanation(this@EnemyInfo, data)

                viewPager.adapter = explain
                viewPager.offscreenPageLimit = 1

                treasure.setOnClickListener {
                    if (!StaticStore.EisOpen) {
                        TranslationAnimator(treasureLayout, AnimatorConst.Axis.X, 300, AnimatorConst.Accelerator.DECELERATE, 0f, treasureLayout.width.toFloat()).start()

                        StaticStore.EisOpen = true
                    } else {
                        val view = currentFocus

                        if (view != null) {
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, 0)
                            treasureLayout.clearFocus()
                        }

                        TranslationAnimator(treasureLayout, AnimatorConst.Axis.X, 300, AnimatorConst.Accelerator.DECELERATE, treasureLayout.width.toFloat(), 0f).start()

                        StaticStore.EisOpen = false
                    }
                }

                treasureLayout.setOnTouchListener { _, _ ->
                    main.isClickable = false
                    true
                }

                back.setOnClickListener {
                    StaticStore.EisOpen = false
                    finish()
                }

                onBackPressedDispatcher.addCallback(
                    this@EnemyInfo,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (StaticStore.EisOpen)
                                treasure.performClick()
                            else
                                finish()
                        }
                    }
                )

                if(StaticStore.EisOpen) {
                    treasureLayout.translationX = -treasureLayout.width.toFloat()
                    treasureLayout.requestLayout()
                }

                if (MultiLangCont.getStatic().EEXP.getCont(e) == null && (e.id.pack == Identifier.DEF || e.description.toString().isBlank())) {
                    StaticStore.setAppear(view1, view2, explanationTitle, viewPager)
                }

                StaticStore.setAppear(scrollView, enemyAnimation, treasure, back)
                StaticStore.setDisappear(progression, st)
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
}