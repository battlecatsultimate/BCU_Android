package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.medal.adapters.MedalListAdapter
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.system.files.VFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Locale

class MedalList : AppCompatActivity() {
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

        setContentView(R.layout.activity_medal_list)

        lifecycleScope.launch {
            var order: ArrayList<Int>

            //Prepare
            val medalList = findViewById<ListView>(R.id.medallist)
            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(medalList)

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@MedalList, { _ -> }, { t -> runOnUiThread { st.text = t }})
            }

            st.setText(R.string.medal_reading_icon)

            withContext(Dispatchers.IO) {
                order = getMedalWithOrder()

                val path = "./org/page/medal/"

                if (StaticStore.medals.isEmpty()) {
                    if(order.isEmpty() || order.size != StaticStore.medalnumber) {
                        for (i in 0 until StaticStore.medalnumber) {
                            val name = "medal_" + number(i) + ".png"

                            val medal = VFile.get("$path$name").data.img.bimg()

                            if (medal == null) {
                                StaticStore.medals.add(StaticStore.empty(1, 1))
                            } else {
                                StaticStore.medals.add(medal as Bitmap)
                            }
                        }
                    } else {
                        for(i in 0 until StaticStore.medalnumber) {
                            val name = "medal_"+number(order[i]) +".png"

                            val medal = VFile.get("$path$name").data.img.bimg()

                            if (medal == null) {
                                StaticStore.medals.add(StaticStore.empty(1, 1))
                            } else {
                                StaticStore.medals.add(medal as Bitmap)
                            }
                        }
                    }
                }
            }

            //Load UI
            val bck = findViewById<FloatingActionButton>(R.id.medalbck)

            bck.setOnClickListener { finish() }

            prog.isIndeterminate = true

            val width = StaticStore.getScreenWidth(this@MedalList, false)
            val wh: Float = if (this@MedalList.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                72f
            else
                90f

            val num = (width - StaticStore.dptopx(16f, this@MedalList)) / StaticStore.dptopx(wh, this@MedalList)

            var line = StaticStore.medalnumber / num

            if (StaticStore.medalnumber % num != 0)
                line++

            val lines = arrayOfNulls<String>(line)

            if(order.isEmpty()) {
                for(i in 0 until StaticStore.medalnumber) {
                    order.add(i)
                }
            }

            val adapter = MedalListAdapter(this@MedalList, num, width, wh, lines, order)

            medalList.adapter = adapter
            medalList.isClickable = false

            StaticStore.setAppear(medalList)
            StaticStore.setDisappear(prog, st)
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

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }

    private fun getMedalWithOrder() : ArrayList<Int> {
        val res = ArrayList<Int>()
        val compare = ArrayList<Int>()

        val vfile = VFile.get("./org/data/medallist.json") ?: return res

        val json = String(vfile.data.bytes, StandardCharsets.UTF_8)

        val jsonObj = JSONObject(json)

        if(jsonObj.has("iconID")) {
            val array = jsonObj.getJSONArray("iconID")

            for(i in 0 until array.length()) {
                if(array.isNull(i)) {
                    res.clear()
                    return res
                }

                val arr = array.getJSONObject(i)

                if(!arr.has("line")) {
                    res.clear()
                    return res
                }

                val line = arr.getInt("line")

                inject(res, compare, line, i)
            }

            return res
        } else {
            return res
        }
    }

    private fun inject(res: ArrayList<Int>, compare: ArrayList<Int>, data: Int, index: Int) {
        for(i in res.indices) {
            if(data <= compare[i]) {
                compare.add(i, data)
                res.add(i, index)
                return
            }
        }

        compare.add(data)
        res.add(index)
    }
}