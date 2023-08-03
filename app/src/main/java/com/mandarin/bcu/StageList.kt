package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.stage.adapters.CStageListAdapter
import com.mandarin.bcu.androidutil.stage.adapters.StageListAdapter
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import common.util.stage.StageMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class StageList : AppCompatActivity() {
    private var custom = false

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

        setContentView(R.layout.activity_stage_list)

        val result = intent
        val extra = result.extras

        if (extra != null) {
            val data = StaticStore.transformIdentifier<StageMap>(extra.getString("Data")) ?: return

            custom = extra.getBoolean("custom")
            
            lifecycleScope.launch {
                val bck: FloatingActionButton = findViewById<FloatingActionButton>(R.id.stglistbck)
                val st = findViewById<TextView>(R.id.status)
                val stglist = findViewById<ListView>(R.id.stglist)
                val prog = findViewById<ProgressBar>(R.id.prog)

                StaticStore.setDisappear(stglist)

                prog.isIndeterminate = true
                prog.max = 10000

                withContext(Dispatchers.IO) {
                    Definer.define(this@StageList, { p -> runOnUiThread { prog.progress = (p * 10000).toInt() }}, { t -> runOnUiThread { st.text = t }})
                }

                bck.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        finish()
                    }
                })

                val stm = Identifier.get(data) ?: return@launch

                val name = findViewById<TextView>(R.id.stglistname)

                var stname = MultiLangCont.get(stm) ?: stm.names.toString()

                if (stname.isBlank())
                    stname = Data.trio(data.id)

                name.text = stname

                val stageListAdapter: Any

                val stages = if(StaticStore.filter != null) {
                    val f = StaticStore.filter ?: return@launch

                    val stmList = f[stm.cont.sid] ?: return@launch

                    val stList = stmList[stm.cont.maps.list.indexOf(stm)] ?: return@launch

                    Array<Identifier<Stage>>(stList.size) {
                        stm.list.list[stList[it]].id
                    }
                } else {
                    Array<Identifier<Stage>>(stm.list.list.size) { i ->
                        stm.list.list[i].id
                    }
                }

                stageListAdapter = if(custom) {
                    CStageListAdapter(this@StageList, stages)
                } else {
                    StageListAdapter(this@StageList, stages)
                }

                stglist.adapter = stageListAdapter

                stglist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.stglistClick < StaticStore.INTERVAL)
                        return@OnItemClickListener

                    StaticStore.stglistClick = SystemClock.elapsedRealtime()

                    val d: Identifier<Stage> = when (stglist.adapter) {
                        is CStageListAdapter -> {
                            (stglist.adapter as CStageListAdapter).getItem(position) ?: return@OnItemClickListener
                        }

                        is StageListAdapter -> {
                            (stglist.adapter as StageListAdapter).getItem(position) ?: return@OnItemClickListener
                        }

                        else -> {
                            return@OnItemClickListener
                        }
                    }

                    val intent = Intent(this@StageList, StageInfo::class.java)

                    intent.putExtra("Data", JsonEncoder.encode(d).toString())
                    intent.putExtra("custom", custom)

                    startActivity(intent)
                }

                StaticStore.setAppear(stglist)
                StaticStore.setDisappear(st, prog)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bck = findViewById<FloatingActionButton>(R.id.stglistbck)

                bck.performClick()
            }
        })
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