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
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.mandarin.bcu.androidutil.FilterEntity
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.unit.adapters.UnitListAdapter
import com.mandarin.bcu.androidutil.unit.asynchs.Adder
import common.system.MultiLangCont
import common.system.fake.ImageBuilder
import java.util.*

class AnimationViewer : AppCompatActivity() {
    private var numbers = ArrayList<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        if (shared.getInt("Orientation", 0) == 1) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else if (shared.getInt("Orientation", 0) == 2) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else if (shared.getInt("Orientation", 0) == 0) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
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
        if (shared.getInt("Orientation", 0) == 1) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else if (shared.getInt("Orientation", 0) == 2) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        setContentView(R.layout.activity_animation_viewer)
        ImageBuilder.builder = BMBuilder()
        val back = findViewById<FloatingActionButton>(R.id.animbck)
        val search = findViewById<FloatingActionButton>(R.id.animsch)
        back.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                StaticStore.filterReset()
                finish()
            }
        })
        search.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                gotoFilter()
            }
        })
        Adder(this).execute()
    }

    private fun showName(location: Int): String {
        val names = ArrayList<String>()
        for (f in StaticStore.units[location].forms) {
            var name = MultiLangCont.FNAME.getCont(f)
            if (name == null) name = ""
            names.add(name)
        }
        val result = StringBuilder(withID(location, names[0]))
        for (i in 1 until names.size) {
            result.append(" - ").append(names[i])
        }
        return result.toString()
    }

    private fun withID(id: Int, name: String): String {
        return if (name == "") {
            number(id)
        } else {
            number(id) + " - " + name
        }
    }

    private fun gotoFilter() {
        val intent = Intent(this@AnimationViewer, SearchFilter::class.java)
        startActivityForResult(intent, REQUEST_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val list = findViewById<ListView>(R.id.unitinflist)

        if (resultCode == Activity.RESULT_OK) {
            val schname = findViewById<TextInputEditText>(R.id.animschname)
            val filterEntity: FilterEntity
            filterEntity = if (Objects.requireNonNull(schname.text).toString().isEmpty()) FilterEntity(StaticStore.unitnumber) else FilterEntity(StaticStore.unitnumber, schname.text.toString())
            numbers = filterEntity.setFilter()
            val newName = ArrayList<String>()
            for (i in numbers) {
                newName.add(StaticStore.names[i])
            }
            val unitListAdapter = UnitListAdapter(this, newName.toTypedArray(), numbers)
            list!!.adapter = unitListAdapter
            list.onItemLongClickListener = OnItemLongClickListener { _, _, position, _ ->
                StaticStore.showShortMessage(this@AnimationViewer, showName(numbers[position]))
                list.isClickable = false
                true
            }
            list.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                if (SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                val result = Intent(this@AnimationViewer, UnitInfo::class.java)
                result.putExtra("ID", numbers[position])
                startActivity(result)
                StaticStore.unitinflistClick = SystemClock.elapsedRealtime()
            }
            schname.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    val filterEntity1 = FilterEntity(StaticStore.unitnumber, s.toString())
                    numbers = filterEntity1.setFilter()
                    val names = ArrayList<String>()
                    for (i in numbers) {
                        names.add(StaticStore.names[i])
                    }
                    val adap = UnitListAdapter(this@AnimationViewer, names.toTypedArray(), numbers)
                    list.adapter = adap
                }
            })
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]

        if(language == "")
            language = Resources.getSystem().configuration.locales.get(0).toString()

        config.setLocale(Locale(language))
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
        mustDie(this)
    }

    fun mustDie(`object`: Any?) {
        if (MainActivity.watcher != null) {
            MainActivity.watcher!!.watch(`object`)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        StaticStore.filterReset()
    }

    companion object {
        const val REQUEST_CODE = 1
    }
}