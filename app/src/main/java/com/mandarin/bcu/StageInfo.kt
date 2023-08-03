package com.mandarin.bcu

import android.annotation.SuppressLint
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
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.stage.adapters.EnemyListRecycle
import com.mandarin.bcu.androidutil.stage.adapters.StageRecycle
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.TranslationAnimator
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class StageInfo : AppCompatActivity() {
    private var custom = false

    @SuppressLint("SourceLockedOrientationActivity")
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

        setContentView(R.layout.activity_stage_info)

        val bck = findViewById<FloatingActionButton>(R.id.stginfobck)

        bck.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                StaticStore.infoOpened = null
                StaticStore.stageSpinner = -1
                finish()
            }
        })

        val result = intent
        val extra = result.extras

        if (extra != null) {
            val data = StaticStore.transformIdentifier<Stage>(extra.getString("Data")) ?: return

            custom = extra.getBoolean("custom")
            
            lifecycleScope.launch {
                val st = findViewById<TextView>(R.id.status)
                val title = findViewById<TextView>(R.id.stginfoname)
                val battle = findViewById<Button>(R.id.battlebtn)
                val stageInfoPanel = findViewById<RecyclerView>(R.id.stginforec)
                val progress = findViewById<ProgressBar>(R.id.prog)
                val treasure = findViewById<FloatingActionButton>(R.id.stginfotrea)
                val treasureLayout = findViewById<ConstraintLayout>(R.id.treasurelayout)
                val mainLayout = findViewById<ConstraintLayout>(R.id.stginfolayout)
                val stageScrollPanel = findViewById<ScrollView>(R.id.stginfoscroll)
                val stageEnemyPanel = findViewById<RecyclerView>(R.id.stginfoenrec)

                StaticStore.setDisappear(stageScrollPanel)

                //Load Data
                withContext(Dispatchers.IO) {
                    Definer.define(this@StageInfo, { p -> runOnUiThread { progress.progress = (p * 10000).toInt() }}, { t -> runOnUiThread { st.text = t }})
                }

                st.setText(R.string.stg_info_loadfilt)

                val stage = Identifier.get(data) ?: return@launch

                progress.isIndeterminate = true

                stageInfoPanel.layoutManager = LinearLayoutManager(this@StageInfo)

                ViewCompat.setNestedScrollingEnabled(stageInfoPanel, false)

                val stageRecycle = StageRecycle(this@StageInfo, data)

                stageInfoPanel.adapter = stageRecycle

                battle.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(this@StageInfo, BattlePrepare::class.java)

                        intent.putExtra("Data", JsonEncoder.encode(data).toString())

                        val manager = stageInfoPanel.layoutManager

                        if (manager != null) {
                            val row = manager.findViewByPosition(0)

                            if (row != null) {
                                val star = row.findViewById<Spinner>(R.id.stginfostarr)

                                if (star != null)
                                    intent.putExtra("selection", star.selectedItemPosition)
                            }
                        }

                        startActivity(intent)
                    }
                })

                title.text = MultiLangCont.get(stage) ?: stage.names.toString()

                if(title.text.isBlank())
                    title.text = getStageName(stage.id.id)

                stageScrollPanel.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
                stageScrollPanel.isFocusable = false
                stageScrollPanel.isFocusableInTouchMode = true

                stageEnemyPanel.layoutManager = LinearLayoutManager(this@StageInfo)

                ViewCompat.setNestedScrollingEnabled(stageEnemyPanel, false)

                val enemyListRecycle = EnemyListRecycle(this@StageInfo, stage)

                stageEnemyPanel.adapter = enemyListRecycle

                if(stage.data.allEnemy.isEmpty()) {
                    stageEnemyPanel.visibility = View.GONE
                }

                treasure.setOnClickListener {
                    if(!StaticStore.SisOpen) {
                        TranslationAnimator(treasureLayout, AnimatorConst.Axis.X, 300, AnimatorConst.Accelerator.DECELERATE, 0f, treasureLayout.width.toFloat()).start()

                        StaticStore.SisOpen = true
                    } else {
                        val view = currentFocus

                        if(view != null) {
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, 0)
                            treasureLayout.clearFocus()
                        }

                        TranslationAnimator(treasureLayout, AnimatorConst.Axis.X, 300, AnimatorConst.Accelerator.DECELERATE, treasureLayout.width.toFloat(), 0f).start()


                        StaticStore.SisOpen = false
                    }

                    treasureLayout.setOnTouchListener { _, _ ->
                        mainLayout.isClickable = false
                        true
                    }
                }

                onBackPressedDispatcher.addCallback(this@StageInfo, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if(StaticStore.SisOpen) {
                            treasure.performClick()
                        } else {
                            bck.performClick()
                        }
                    }
                })

                StaticStore.setAppear(stageScrollPanel)
                StaticStore.setDisappear(st, progress)
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

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

        StaticStore.toast = null
    }

    private fun getStageName(posit: Int) : String {
        return "Stage"+number(posit)
    }

    private fun number(n: Int) : String {
        return when (n) {
            in 0..9 -> {
                "00$n"
            }
            in 10..99 -> {
                "0$n"
            }
            else -> {
                "$n"
            }
        }
    }
}