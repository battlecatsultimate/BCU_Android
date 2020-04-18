package com.mandarin.bcu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.util.isEmpty
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.FilterStage
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.filter
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import com.mandarin.bcu.androidutil.stage.asynchs.MapAdder
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*
import kotlin.collections.ArrayList

class MapList : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 100
    }

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

        when {
            shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        if (!shared.getBoolean("DEV_MODE", false)) {
            AppWatcher.config = AppWatcher.config.copy(enabled = false)
            LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        } else {
            AppWatcher.config = AppWatcher.config.copy(enabled = true)
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }

        DefineItf.check(this)

        setContentView(R.layout.activity_map_list)

        val back = findViewById<FloatingActionButton>(R.id.stgbck)

        back.setOnClickListener {
            StaticStore.stgFilterReset()
            finish()
        }

        val stageset = findViewById<Spinner>(R.id.stgspin)
        val setstg = resources.getStringArray(R.array.set_stg)

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, R.layout.spinneradapter, setstg) {
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
                return v
            }
        }

        stageset.adapter = adapter

        val mapAdder = MapAdder(this)

        mapAdder.execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            filter = FilterStage.setFilter(StaticStore.stgschname, StaticStore.stmschname, StaticStore.stgenem, StaticStore.stgenemorand, StaticStore.stgmusic, StaticStore.stgbg, StaticStore.stgstar, StaticStore.stgbh, StaticStore.bhop, StaticStore.stgcontin, StaticStore.stgboss, this)

            val stageset = findViewById<Spinner>(R.id.stgspin)
            val maplist = findViewById<ListView>(R.id.maplist)
            val loadt = findViewById<TextView>(R.id.mapst)

            if(filter.isEmpty()) {
                stageset.visibility = View.GONE
                maplist.visibility = View.GONE

                loadt.visibility = View.VISIBLE
                loadt.setText(R.string.filter_nores)
            } else {
                stageset.visibility = View.VISIBLE
                maplist.visibility = View.VISIBLE
                loadt.visibility = View.GONE

                val mapcolcarray = resources.getStringArray(R.array.set_stg)

                val resmc = ArrayList<String>()
                val resposition = ArrayList<Int>()

                for (i in 0 until filter.size()) {
                    val index = StaticStore.MAPCODE.indexOf(filter.keyAt(i))

                    if (index != -1) {
                        resmc.add(mapcolcarray[index])
                    }
                }

                val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(this, R.layout.spinneradapter, resmc) {
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

                        return v
                    }
                }

                stageset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        try {
                            var index = StaticStore.MAPCODE.indexOf(filter.keyAt(position))

                            if (index == -1)
                                index = 0

                            val resmapname = ArrayList<String>()
                            resposition.clear()

                            val resmaplist = filter[filter.keyAt(position)]

                            for(i in 0 until resmaplist.size()) {
                                resmapname.add(StaticStore.mapnames[index][resmaplist.keyAt(i)])
                                resposition.add(resmaplist.keyAt(i))
                            }

                            val mapListAdapter = MapListAdapter(this@MapList, resmapname.toTypedArray(), filter.keyAt(position), resposition)

                            maplist.adapter = mapListAdapter
                        } catch (e: NullPointerException) {
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                        } catch (e: IndexOutOfBoundsException) {
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@MapList)
                        }
                    }

                }

                stageset.adapter = adapter

                var index = StaticStore.MAPCODE.indexOf(filter.keyAt(stageset.selectedItemPosition))

                if (index == -1)
                    index = 0

                val resmapname = ArrayList<String>()

                val resmaplist = filter[filter.keyAt(stageset.selectedItemPosition)]

                for(i in 0 until resmaplist.size()) {
                    resmapname.add(StaticStore.mapnames[index][resmaplist.keyAt(i)])
                    resposition.add(resmaplist.keyAt(i))
                }

                val mapListAdapter = MapListAdapter(this, resmapname.toTypedArray(), filter.keyAt(stageset.selectedItemPosition),resposition)

                maplist.adapter = mapListAdapter

                maplist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL)
                        return@OnItemClickListener

                    StaticStore.maplistClick = SystemClock.elapsedRealtime()

                    val intent = Intent(this@MapList, StageList::class.java)

                    intent.putExtra("mapcode", filter.keyAt(stageset.selectedItemPosition))
                    intent.putExtra("stid", resposition[position])

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

        if(language == "")
            language = Resources.getSystem().configuration.locales.get(0).language

        config.setLocale(Locale(language))
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
}