package com.mandarin.bcu

import android.annotation.SuppressLint
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
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.mandarin.bcu.androidutil.FilterEntity
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyListAdapter
import com.mandarin.bcu.androidutil.enemy.asynchs.EAdder
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import common.system.fake.ImageBuilder
import java.util.*

open class EnemyList : AppCompatActivity() {
    private var list: ListView? = null
    private var numbers = ArrayList<Int>()
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
        if (shared.getInt("Orientation", 0) == 1) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else if (shared.getInt("Orientation", 0) == 2) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else if (shared.getInt("Orientation", 0) == 0) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        setContentView(R.layout.activity_enemy_list)
        ImageBuilder.builder = BMBuilder()
        val back = findViewById<FloatingActionButton>(R.id.enlistbck)
        list = findViewById(R.id.enlist)
        val search = findViewById<FloatingActionButton>(R.id.enlistsch)
        back.setOnClickListener {
            StaticStore.filterReset()
            finish()
        }
        search.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                gotoFilter()
            }
        })
        StaticStore.getEnemynumber()
        EAdder(this, StaticStore.emnumber).execute()
    }

    protected fun gotoFilter() {
        val intent = Intent(this@EnemyList, EnemySearchFilter::class.java)
        startActivityForResult(intent, 1)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val schname = findViewById<TextInputEditText>(R.id.enemlistschname)
            val filterEntity: FilterEntity
            filterEntity = if (Objects.requireNonNull(schname.text).toString().isNotEmpty()) FilterEntity(StaticStore.emnumber, schname.text.toString()) else FilterEntity(StaticStore.emnumber)
            numbers = filterEntity.eSetFilter()
            val newName = ArrayList<String>()
            for (i in numbers) newName.add(StaticStore.enames[i])
            val enemyListAdapter = EnemyListAdapter(this, newName.toTypedArray(), numbers)
            list!!.adapter = enemyListAdapter
            list!!.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                val result = Intent(this@EnemyList, EnemyInfo::class.java)
                result.putExtra("ID", numbers[position])
                startActivity(result)
                StaticStore.unitinflistClick = SystemClock.elapsedRealtime()
            }
            schname.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    val filterEntity1 = FilterEntity(StaticStore.emnumber, s.toString())
                    numbers = filterEntity1.eSetFilter()
                    val names = ArrayList<String>()
                    for (i in numbers) {
                        names.add(StaticStore.enames[i])
                    }
                    val adap = EnemyListAdapter(this@EnemyList, names.toTypedArray(), numbers)
                    list!!.adapter = adap
                    if (s.toString().isEmpty()) {
                        schname.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.search), null)
                    } else {
                        schname.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_close_black_24dp), null)
                    }
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

    override fun onBackPressed() {
        StaticStore.filterReset()
        super.onBackPressed()
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
}