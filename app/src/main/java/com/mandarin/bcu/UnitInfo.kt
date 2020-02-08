package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.unit.asynchs.UInfoLoader
import java.util.*

class UnitInfo : AppCompatActivity() {
    private var treasure: FloatingActionButton? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
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
        if (shared.getInt("Orientation", 0) == 1) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else if (shared.getInt("Orientation", 0) == 2) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else if (shared.getInt("Orientation", 0) == 0) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
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
        if (StaticStore.unitinfreset) {
            StaticStore.unittabposition = 0
            StaticStore.unitinfreset = false
        }
        val treasuretab = findViewById<ConstraintLayout>(R.id.treasurelayout)
        treasuretab.visibility = View.GONE
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (shared.getBoolean("Lay_Land", false)) {
                val scrollView = findViewById<NestedScrollView>(R.id.unitinfscroll)
                scrollView.visibility = View.GONE
                val unittable = findViewById<ViewPager>(R.id.unitinftable)
                unittable.isFocusable = false
                unittable.requestFocusFromTouch()
            } else {
                val scrollView = findViewById<NestedScrollView>(R.id.unitinfscroll)
                scrollView.visibility = View.GONE
                val recyclerView = findViewById<RecyclerView>(R.id.unitinfrec)
                recyclerView.requestFocusFromTouch()
            }
        } else {
            if (shared.getBoolean("Lay_Port", false)) {
                val scrollView = findViewById<NestedScrollView>(R.id.unitinfscroll)
                scrollView.visibility = View.GONE
                val unittable = findViewById<ViewPager>(R.id.unitinftable)
                unittable.isFocusable = false
                unittable.requestFocusFromTouch()
            } else {
                val scrollView = findViewById<NestedScrollView>(R.id.unitinfscroll)
                scrollView.visibility = View.GONE
                val recyclerView = findViewById<RecyclerView>(R.id.unitinfrec)
                recyclerView.requestFocusFromTouch()
            }
        }
        val unittitle = findViewById<TextView>(R.id.unitinfrarname)
        val back = findViewById<FloatingActionButton>(R.id.unitinfback)
        treasure = findViewById(R.id.treabutton)
        back.setOnClickListener {
            StaticStore.unitinfreset = true
            StaticStore.UisOpen = false
            finish()
        }
        val result = intent
        val extra = result.extras
        if (extra != null) {
            val id = extra.getInt("ID")
            val s = GetStrings(this)
            unittitle.text = s.getTitle(StaticStore.units[id].forms[0])
            val anim = findViewById<Button>(R.id.animanim)
            anim.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@UnitInfo, ImageViewer::class.java)
                    StaticStore.formposition = StaticStore.unittabposition
                    intent.putExtra("Img", 2)
                    intent.putExtra("ID", id)
                    intent.putExtra("Form", StaticStore.formposition)
                    startActivity(intent)
                }
            })
            UInfoLoader(id, this, supportFragmentManager).execute()
        }
    }

    override fun onBackPressed() {
        if (StaticStore.UisOpen) {
            treasure!!.performClick()
        } else {
            super.onBackPressed()
            StaticStore.unitinfreset = true
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