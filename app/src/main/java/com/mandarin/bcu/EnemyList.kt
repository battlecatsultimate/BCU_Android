package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.enemy.coroutine.EAdder
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.system.fake.ImageBuilder
import java.util.*

open class EnemyList : AppCompatActivity() {
    private var mode = EAdder.MODE_INFO

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val schname: TextInputEditText = findViewById(R.id.enemlistschname)

        for(i in StaticStore.filterEntityList.indices) {
            StaticStore.filterEntityList[i] = true
        }

        schname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                StaticStore.entityname = s.toString()

                for(i in StaticStore.filterEntityList.indices) {
                    StaticStore.filterEntityList[i] = true
                }
            }
        })
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

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_enemy_list)

        ImageBuilder.builder = BMBuilder()

        val extra = intent.extras

        if(extra != null) {
            mode = extra.getInt("mode")
        }

        val back = findViewById<FloatingActionButton>(R.id.enlistbck)
        val search = findViewById<FloatingActionButton>(R.id.enlistsch)

        back.setOnClickListener {
            StaticStore.filterReset()
            StaticStore.entityname = ""
            finish()
        }

        search.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                gotoFilter()
            }
        })

        EAdder(this,mode,supportFragmentManager).execute()
    }

    protected fun gotoFilter() {
        val intent = Intent(this@EnemyList, EnemySearchFilter::class.java)

        resultLauncher.launch(intent)
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
        StaticStore.filterReset()
        StaticStore.entityname = ""
        super.onBackPressed()
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