package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.filter
import com.mandarin.bcu.androidutil.filter.FilterStage
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.stage.MapColc
import common.util.stage.StageMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapList : AppCompatActivity() {
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            filter = FilterStage.setFilter(StaticStore.stgschname, StaticStore.stmschname, StaticStore.stgenem, StaticStore.stgenemorand, StaticStore.stgmusic, StaticStore.stgbg, StaticStore.stgstar, StaticStore.stgbh, StaticStore.bhop, StaticStore.stgcontin, StaticStore.stgboss, this)

            val f = filter ?: return@registerForActivityResult

            val keys = f.keys.toMutableList()

            keys.sort()

            val stageSet = findViewById<Spinner>(R.id.stgspin)
            val mapList = findViewById<ListView>(R.id.maplist)
            val status = findViewById<TextView>(R.id.status)

            if(f.isEmpty()) {
                stageSet.visibility = View.GONE
                mapList.visibility = View.GONE

                status.visibility = View.VISIBLE
                status.setText(R.string.filter_nores)
            } else {
                stageSet.visibility = View.VISIBLE
                mapList.visibility = View.VISIBLE
                status.visibility = View.GONE

                val mapCollectionResult = ArrayList<String>()
                val collectionName = StaticStore.collectMapCollectionNames(this@MapList)

                for (i in keys) {
                    val index = StaticStore.mapcode.indexOf(i)

                    if (index != -1) {
                        mapCollectionResult.add(collectionName[index])
                    }
                }

                var maxWidth = 0

                val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, R.layout.spinneradapter, mapCollectionResult) {
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

                val layout = stageSet.layoutParams

                layout.width = maxWidth

                stageSet.layoutParams = layout

                stageSet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

                            mapList.adapter = mapListAdapter
                        } catch (e: NullPointerException) {
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                        } catch (e: IndexOutOfBoundsException) {
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                        }
                    }
                }

                stageSet.adapter = adapter

                val index = StaticStore.mapcode.indexOf(keys[stageSet.selectedItemPosition])

                if (index == -1)
                    return@registerForActivityResult

                val resmapname = ArrayList<Identifier<StageMap>>()

                val resmaplist = f[keys[stageSet.selectedItemPosition]] ?: return@registerForActivityResult

                val mc = MapColc.get(keys[stageSet.selectedItemPosition])

                for(i in 0 until resmaplist.size()) {
                    val stm = mc.maps.list[resmaplist.keyAt(i)]

                    resmapname.add(stm.id)
                }

                val mapListAdapter = MapListAdapter(this, resmapname)

                mapList.adapter = mapListAdapter

                mapList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL)
                        return@OnItemClickListener

                    StaticStore.maplistClick = SystemClock.elapsedRealtime()

                    val stm = if(mapList.adapter is MapListAdapter) {
                        (mapList.adapter as MapListAdapter).getItem(position) ?: return@OnItemClickListener
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

        setContentView(R.layout.activity_map_list)
        
        lifecycleScope.launch {
            val maplist = findViewById<ListView>(R.id.maplist)
            val st = findViewById<TextView>(R.id.status)
            val stageset = findViewById<Spinner>(R.id.stgspin)
            val prog = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(maplist)

            st.text = getString(R.string.stg_info_stgs)

            prog.isIndeterminate = false
            prog.max = 10000

            withContext(Dispatchers.IO) {
                Definer.define(this@MapList, { p -> runOnUiThread { prog.progress = (p * 10000).toInt() }}, { t -> runOnUiThread { st.text = t }})
            }

            if(filter == null) {
                val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this@MapList, R.layout.spinneradapter, StaticStore.collectMapCollectionNames(this@MapList)) {
                    override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                        val v = super.getView(position, converView, parent)

                        (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))

                        val eight = StaticStore.dptopx(8f, this@MapList)

                        v.setPadding(eight, eight, eight, eight)

                        return v
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val v = super.getDropDownView(position, convertView, parent)

                        (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))

                        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                        return v
                    }
                }
                
                stageset.adapter = adapter

                stageset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        try {
                            val positions = ArrayList<Int>()

                            val mc = MapColc.get(StaticStore.mapcode[position])

                            try {
                                for (i in mc.maps.list.indices) {
                                    positions.add(i)
                                }
                            } catch (e : java.lang.IndexOutOfBoundsException) {
                                ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                                return
                            }

                            val names = ArrayList<Identifier<StageMap>>()

                            for(i in mc.maps.list.indices) {
                                val stm = mc.maps.list[i]

                                names.add(stm.id)
                            }

                            val mapListAdapter = MapListAdapter(this@MapList, names)
                            maplist.adapter = mapListAdapter
                        } catch (e: NullPointerException) {
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                val name = ArrayList<Identifier<StageMap>>()

                stageset.setSelection(0)

                val mc = MapColc.get(StaticStore.mapcode[stageset.selectedItemPosition]) ?: return@launch

                for(i in mc.maps.list.indices) {
                    val stm = mc.maps[i]

                    name.add(stm.id)
                }

                val mapListAdapter = MapListAdapter(this@MapList, name)

                maplist.adapter = mapListAdapter

                maplist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                    StaticStore.maplistClick = SystemClock.elapsedRealtime()

                    if (maplist.adapter !is MapListAdapter)
                        return@OnItemClickListener

                    val stm = Identifier.get((maplist.adapter as MapListAdapter).getItem(position)) ?: return@OnItemClickListener

                    val intent = Intent(this@MapList, StageList::class.java)

                    intent.putExtra("Data", JsonEncoder.encode(stm.id).toString())
                    intent.putExtra("custom", !StaticStore.BCMapCode.contains(stm.cont.sid))

                    startActivity(intent)
                }
            } else {
                val f = filter ?: return@launch

                if(f.isEmpty()) {
                    stageset.visibility = View.GONE
                    maplist.visibility = View.GONE
                } else {
                    stageset.visibility = View.VISIBLE
                    maplist.visibility = View.VISIBLE

                    val mapCollectionResult = ArrayList<String>()
                    val collectionName = StaticStore.collectMapCollectionNames(this@MapList)

                    val keys = f.keys.toMutableList()

                    keys.sort()

                    for (i in keys) {
                        val index = StaticStore.mapcode.indexOf(i)

                        if (index != -1) {
                            mapCollectionResult.add(collectionName[index])
                        }
                    }

                    val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this@MapList, R.layout.spinneradapter, mapCollectionResult) {
                        override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                            val v = super.getView(position, converView, parent)

                            (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))

                            val eight = StaticStore.dptopx(8f, this@MapList)

                            v.setPadding(eight, eight, eight, eight)

                            return v
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v = super.getDropDownView(position, convertView, parent)

                            (v as TextView).setTextColor(ContextCompat.getColor(this@MapList, R.color.TextPrimary))

                            v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                            return v
                        }
                    }

                    stageset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            try {
                                val resmapname = ArrayList<Identifier<StageMap>>()

                                val resmaplist = f[keys[position]] ?: return

                                val mc = MapColc.get(keys[position]) ?: return

                                for(i in 0 until resmaplist.size()) {
                                    val stm = mc.maps.list[resmaplist.keyAt(i)]

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

                    val mc = MapColc.get(keys[stageset.selectedItemPosition]) ?: return@launch

                    val resmapname = ArrayList<Identifier<StageMap>>()

                    val resmaplist = f[keys[stageset.selectedItemPosition]] ?: return@launch

                    for(i in 0 until resmaplist.size()) {
                        val stm = mc.maps.list[resmaplist.keyAt(i)]

                        resmapname.add(stm.id)
                    }

                    val mapListAdapter = MapListAdapter(this@MapList, resmapname)
                    maplist.adapter = mapListAdapter

                    maplist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL)
                            return@OnItemClickListener

                        StaticStore.maplistClick = SystemClock.elapsedRealtime()

                        val intent = Intent(this@MapList, StageList::class.java)

                        if(maplist.adapter !is MapListAdapter)
                            return@OnItemClickListener

                        val stm = Identifier.get((maplist.adapter as MapListAdapter).getItem(position)) ?: return@OnItemClickListener

                        intent.putExtra("Data", JsonEncoder.encode(stm.id).toString())
                        intent.putExtra("custom", !StaticStore.BCMapCode.contains(stm.cont.sid))

                        startActivity(intent)
                    }
                }
            }

            val stgfilter = findViewById<FloatingActionButton>(R.id.stgfilter)

            stgfilter.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@MapList,StageSearchFilter::class.java)

                    resultLauncher.launch(intent)
                }
            })

            val back = findViewById<FloatingActionButton>(R.id.stgbck)

            back.setOnClickListener {
                StaticStore.stgFilterReset()
                StaticStore.filterReset()
                StaticStore.entityname = ""
                finish()
            }

            onBackPressedDispatcher.addCallback(this@MapList, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val bck = findViewById<FloatingActionButton>(R.id.stgbck)

                    bck.performClick()
                }
            })

            StaticStore.setAppear(maplist)
            StaticStore.setDisappear(st, prog)
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