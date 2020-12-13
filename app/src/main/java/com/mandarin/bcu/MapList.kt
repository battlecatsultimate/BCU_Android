package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.filter
import com.mandarin.bcu.androidutil.filter.FilterStage
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import com.mandarin.bcu.androidutil.stage.coroutine.MapAdder
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.stage.MapColc
import common.util.stage.StageMap
import java.util.*
import kotlin.collections.ArrayList

class MapList : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 100
    }

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

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_map_list)

        val back = findViewById<FloatingActionButton>(R.id.stgbck)

        back.setOnClickListener {
            StaticStore.stgFilterReset()
            StaticStore.filterReset()
            StaticStore.entityname = ""
            finish()
        }

        val mapAdder = MapAdder(this)

        mapAdder.execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            filter = FilterStage.setFilter(StaticStore.stgschname, StaticStore.stmschname, StaticStore.stgenem, StaticStore.stgenemorand, StaticStore.stgmusic, StaticStore.stgbg, StaticStore.stgstar, StaticStore.stgbh, StaticStore.bhop, StaticStore.stgcontin, StaticStore.stgboss, this)

            val f = filter ?: return

            val keys = f.keys.toMutableList()

            keys.sort()

            val stageset = findViewById<Spinner>(R.id.stgspin)
            val maplist = findViewById<ListView>(R.id.maplist)
            val loadt = findViewById<TextView>(R.id.status)

            if(f.isEmpty()) {
                stageset.visibility = View.GONE
                maplist.visibility = View.GONE

                loadt.visibility = View.VISIBLE
                loadt.setText(R.string.filter_nores)
            } else {
                stageset.visibility = View.VISIBLE
                maplist.visibility = View.VISIBLE
                loadt.visibility = View.GONE

                val resmc = ArrayList<String>()

                for (i in keys) {
                    val index = StaticStore.mapcode.indexOf(i)

                    if (index != -1) {
                        resmc.add(StaticStore.mapcolcname[index])
                    }
                }

                var maxWidth = 0

                val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, R.layout.spinneradapter, resmc) {
                    override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                        val v = super.getView(position, converView, parent)

                        (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))

                        val eight = StaticStore.dptopx(8f, this@MapList)

                        v.setPadding(eight, eight, eight, eight)

                        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                        if(maxWidth < v.measuredWidth) {
                            maxWidth = v.measuredWidth
                        }

                        return v
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val v = super.getDropDownView(position, convertView, parent)

                        (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))

                        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                        return v
                    }
                }

                val layout = stageset.layoutParams

                layout.width = maxWidth

                stageset.layoutParams = layout

                stageset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        try {
                            var index = StaticStore.mapcode.indexOf(keys[position])

                            if (index == -1)
                                index = 0

                            val resmapname = ArrayList<Identifier<StageMap>>()

                            val resmaplist = f[keys[position]] ?: return

                            val mc = MapColc.get(StaticStore.mapcode[index]) ?: return

                            for(i in 0 until resmaplist.size()) {
                                val stm = mc.maps[resmaplist.keyAt(i)]

                                resmapname.add(stm.id)
                            }

                            val mapListAdapter = MapListAdapter(this@MapList, resmapname)

                            maplist.adapter = mapListAdapter
                        } catch (e: NullPointerException) {
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                        } catch (e: IndexOutOfBoundsException) {
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                        }
                    }
                }

                stageset.adapter = adapter

                val index = StaticStore.mapcode.indexOf(keys[stageset.selectedItemPosition])

                if (index == -1)
                    return

                val resmapname = ArrayList<Identifier<StageMap>>()

                val resmaplist = f[keys[stageset.selectedItemPosition]] ?: return

                val mc = MapColc.get(keys[stageset.selectedItemPosition])

                for(i in 0 until resmaplist.size()) {
                    val stm = mc.maps.list[resmaplist.keyAt(i)]

                    resmapname.add(stm.id)
                }

                val mapListAdapter = MapListAdapter(this, resmapname)

                maplist.adapter = mapListAdapter

                maplist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL)
                        return@OnItemClickListener

                    StaticStore.maplistClick = SystemClock.elapsedRealtime()

                    val stm = if(maplist.adapter is MapListAdapter) {
                        (maplist.adapter as MapListAdapter).getItem(position) ?: return@OnItemClickListener
                    } else {
                        return@OnItemClickListener
                    }

                    val intent = Intent(this@MapList, StageList::class.java)

                    intent.putExtra("Data", JsonEncoder.encode(stm).toString())
                    intent.putExtra("custom", !StaticStore.BCMapCode.contains(stm.cont.sid))

                    startActivity(intent)
                }
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

    override fun onBackPressed() {
        val bck = findViewById<FloatingActionButton>(R.id.stgbck)
        bck.performClick()
    }

    public override fun onDestroy() {
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