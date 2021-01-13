package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.system.files.VFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import java.util.*

class FileViewer : AppCompatActivity() {
    lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

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

        setContentView(R.layout.activity_file_viewer)

        val extra = intent.extras ?: return

        path = extra.getString("path", "")

        if(path == "")
            return

        val scroll = findViewById<ScrollView>(R.id.fileviewscroll)
        val img = findViewById<ImageView>(R.id.fileviewimg)
        val text = findViewById<TextView>(R.id.fileviewtext)
        val bck = findViewById<FloatingActionButton>(R.id.fileviewbck)
        val title = findViewById<AutoMarquee>(R.id.fileviewtitle)

        val f = VFile.get(path) ?: return

        title.text = f.path

        if(path.endsWith("png")) {
            scroll.visibility = View.GONE

            val image = f.data.img.bimg() as Bitmap

            img.setImageBitmap(image)
        } else {
            img.visibility = View.GONE

            text.text = loadText(f)
            text.setTextIsSelectable(true)
        }

        bck.setOnClickListener {
            finish()
        }
    }

    private fun loadText(f: VFile) : String {
        val ins = f.data.stream ?: return ""

        val isr = InputStreamReader(ins, StandardCharsets.UTF_8)
        val b = BufferedReader(isr)

        val sb = StringBuilder()
        var t: String?

        while(true) {
            t = b.readLine()

            if(t == null)
                break

            sb.append(t.replace("\t","    ")).append("\n")
        }

        return sb.toString()
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
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
}