package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.Revalidater
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import common.CommonStatic
import java.util.*

open class ConfigScreen : AppCompatActivity() {
    private val langId = intArrayOf(R.string.lang_auto, R.string.def_lang_en, R.string.def_lang_zh, R.string.def_lang_ko, R.string.def_lang_ja)
    private val locales = StaticStore.lang
    private var started = false
    private var changed = false
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

        setContentView(R.layout.activity_config_screen)

        val back = findViewById<ImageButton>(R.id.configback)
        back.setOnClickListener {
            val intent = Intent(this@ConfigScreen, MainActivity::class.java)
            intent.putExtra("Config", true)
            startActivity(intent)
            finish()
        }

        val day = findViewById<RadioButton>(R.id.themeday)
        val night = findViewById<RadioButton>(R.id.themenight)
        val frames = findViewById<RadioButton>(R.id.configframe)
        val seconds = findViewById<RadioButton>(R.id.configsecond)
        if (shared.contains("initial")) {
            if (!shared.getBoolean("theme", false)) night.isChecked = true else day.isChecked = true
        }
        if (shared.getBoolean("frame", true)) {
            frames.isChecked = true
        } else {
            seconds.isChecked = true
        }
        val theme = findViewById<RadioGroup>(R.id.configrgtheme)
        theme.setOnCheckedChangeListener { _, checkedId ->
            if(started) {
                if (checkedId == day.id) {
                    val ed1 = shared.edit()
                    ed1.putBoolean("theme", true)
                    ed1.apply()
                    restart()
                } else {
                    val ed1 = shared.edit()
                    ed1.putBoolean("theme", false)
                    ed1.apply()
                    restart()
                }
            }
        }
        val frse = findViewById<RadioGroup>(R.id.configfrse)
        frse.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == frames.id) {
                val ed1 = shared.edit()
                ed1.putBoolean("frame", true)
                ed1.apply()
            } else {
                val ed1 = shared.edit()
                ed1.putBoolean("frame", false)
                ed1.apply()
            }
        }
        val levels: MutableList<Int> = ArrayList()
        for (j in 1..50) levels.add(j)
        val deflev = findViewById<Spinner>(R.id.configdeflevsp)
        val arrayAdapter = ArrayAdapter(this, R.layout.spinneradapter, levels)
        deflev.adapter = arrayAdapter
        deflev.setSelection(getIndex(deflev, shared.getInt("default_level", 50)),false)

        println(CommonStatic.Lang.lang)
        val apktest = findViewById<Switch>(R.id.apktest)
        apktest.isChecked = shared.getBoolean("apktest", false)
        val senderr = findViewById<Switch>(R.id.senderror)
        senderr.isChecked = shared.getBoolean("upload", false)
        senderr.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val ed1 = shared.edit()
                ed1.putBoolean("upload", true)
                ed1.apply()
            } else {
                val ed1 = shared.edit()
                ed1.putBoolean("upload", false)
                ed1.apply()
            }

            StaticStore.upload = shared.getBoolean("upload",false) || shared.getBoolean("ask_upload",true)
        }
        apktest.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val ed1 = shared.edit()
                ed1.putBoolean("apktest", true)
                ed1.apply()
            } else {
                val ed1 = shared.edit()
                ed1.putBoolean("apktest", false)
                ed1.apply()
            }
        }
        val language = findViewById<Spinner>(R.id.configlangsp)
        val lang: MutableList<String> = ArrayList()
        for (i1 in langId) {
            lang.add(getString(i1))
        }
        val adapter = ArrayAdapter(this, R.layout.spinneradapter, lang)
        language.adapter = adapter
        language.setSelection(shared.getInt("Language", 0), false)

        val orientation = findViewById<RadioGroup>(R.id.configorirg)
        val oris = arrayOf(findViewById(R.id.configoriauto), findViewById(R.id.configoriland), findViewById<RadioButton>(R.id.configoriport))
        orientation.setOnCheckedChangeListener { _, checkedId ->
            if (started) for (i in 0..2) if (i != shared.getInt("Orientation", 0) && checkedId == oris[i].id) {
                val ed1 = shared.edit()
                ed1.putInt("Orientation", i)
                ed1.apply()
                restart()
            }
        }
        oris[shared.getInt("Orientation", 0)].isChecked = true
        val unitinfland = findViewById<RadioGroup>(R.id.configinfland)
        val unitinflandlist = findViewById<RadioButton>(R.id.configlaylandlist)
        val unitinflandslide = findViewById<RadioButton>(R.id.configlaylandslide)
        if (shared.getBoolean("Lay_Land", true)) unitinflandslide.isChecked = true else unitinflandlist.isChecked = true
        unitinfland.setOnCheckedChangeListener { _, checkedId ->
            val ed1 = shared.edit()
            ed1.putBoolean("Lay_Land", checkedId == unitinflandslide.id)
            ed1.apply()
        }
        val unitinfport = findViewById<RadioGroup>(R.id.configinfport)
        val unitinfportlist = findViewById<RadioButton>(R.id.configlayportlist)
        val unitinfportslide = findViewById<RadioButton>(R.id.configlayportslide)
        if (shared.getBoolean("Lay_Port", true)) unitinfportslide.isChecked = true else unitinfportlist.isChecked = true
        unitinfport.setOnCheckedChangeListener { _, checkedId ->
            val ed1 = shared.edit()
            ed1.putBoolean("Lay_Port", checkedId == unitinfportslide.id)
            ed1.apply()
        }
        val skiptext = findViewById<Switch>(R.id.configskiptext)
        skiptext.isChecked = shared.getBoolean("Skip_Text", false)
        skiptext.setOnCheckedChangeListener { _, isChecked ->
            val ed1 = shared.edit()
            ed1.putBoolean("Skip_Text", isChecked)
            ed1.apply()
        }
        val checkupdate = findViewById<Button>(R.id.configcheckup)
        checkupdate.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@ConfigScreen, CheckUpdateScreen::class.java)
                intent.putExtra("Config", true)
                startActivity(intent)
                finish()
            }
        })
        val axis = findViewById<Switch>(R.id.configaxis)
        axis.isChecked = shared.getBoolean("Axis", true)
        axis.setOnCheckedChangeListener { _, isChecked ->
            val ed1 = shared.edit()
            ed1.putBoolean("Axis", isChecked)
            ed1.apply()
        }
        val fps = findViewById<Switch>(R.id.configfps)
        fps.isChecked = shared.getBoolean("FPS", true)
        fps.setOnCheckedChangeListener { _, isChecked ->
            val ed1 = shared.edit()
            ed1.putBoolean("FPS", isChecked)
            ed1.apply()
        }
        val mus = findViewById<Switch>(R.id.configmus)
        val musvol = findViewById<SeekBar>(R.id.configmusvol)
        mus.isChecked = shared.getBoolean("music", true)
        musvol.isEnabled = shared.getBoolean("music", true)
        musvol.max = 99
        musvol.progress = shared.getInt("mus_vol", 99)
        mus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val editor = shared.edit()
                editor.putBoolean("music", true)
                editor.apply()
                SoundHandler.musicPlay = true
                musvol.isEnabled = true
            } else {
                val editor = shared.edit()
                editor.putBoolean("music", false)
                editor.apply()
                SoundHandler.musicPlay = false
                musvol.isEnabled = false
            }
        }
        musvol.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (progress >= 100 || progress < 0) return
                    val editor = shared.edit()
                    editor.putInt("mus_vol", progress)
                    editor.apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        val soundeff = findViewById<Switch>(R.id.configse)
        val sevol = findViewById<SeekBar>(R.id.configsevol)
        soundeff.isChecked = shared.getBoolean("SE", true)
        sevol.isEnabled = shared.getBoolean("SE", true)
        sevol.max = 99
        sevol.progress = shared.getInt("se_vol", 99)
        soundeff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val editor = shared.edit()
                editor.putBoolean("SE", true)
                editor.apply()
                SoundHandler.se_vol = StaticStore.getVolumScaler((shared.getInt("se_vol", 99) * 0.85).toInt())
                sevol.isEnabled = true
            } else {
                val editor = shared.edit()
                editor.putBoolean("SE", false)
                editor.apply()
                SoundHandler.se_vol = 0f
                sevol.isEnabled = false
            }
        }
        sevol.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (progress >= 100 || progress < 0) return
                    val editor = shared.edit()
                    editor.putInt("se_vol", progress)
                    editor.apply()
                    SoundHandler.se_vol = StaticStore.getVolumScaler((progress * 0.85).toInt())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        val build = findViewById<TextView>(R.id.configbuildver)
        val text = getString(R.string.config_build_ver).replace("-", if (shared.getBoolean("DEV_MODE", false)) BuildConfig.VERSION_NAME + "_DEV_MODE" else BuildConfig.VERSION_NAME)
        build.text = text
        build.setOnLongClickListener(OnLongClickListener {
            if (!shared.getBoolean("DEV_MODE", false)) {
                val builder = AlertDialog.Builder(this@ConfigScreen)
                val inflater = LayoutInflater.from(this@ConfigScreen)
                val view = inflater.inflate(R.layout.dev_mode_password, null)
                builder.setView(view)
                val active = view.findViewById<Button>(R.id.devpassactive)
                val password = view.findViewById<EditText>(R.id.devpassedit)
                val dialog = builder.create()
                dialog.setCancelable(true)
                dialog.show()
                active.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val pass = password.text.toString()
                        if (pass.isNotEmpty()) {
                            if (pass == BuildConfig.YOU_CANT_FIND_PASSWORD) {
                                val editor = shared.edit()
                                editor.putBoolean("DEV_MODE", true)
                                editor.apply()
                                val text1 = getString(R.string.config_build_ver).replace("-", BuildConfig.VERSION_NAME + "_DEV_MODE")
                                build.text = text1
                                StaticStore.showShortMessage(this@ConfigScreen, R.string.dev_pass_activated)
                            } else {
                                StaticStore.showShortMessage(this@ConfigScreen, R.string.dev_pass_wrong)
                            }
                        } else {
                            StaticStore.showShortMessage(this@ConfigScreen, R.string.dev_pass_wrong)
                        }
                        dialog.dismiss()
                    }
                })
                return@OnLongClickListener true
            }
            false
        })
    }

    override fun onStart() {
        val deflev: Spinner = findViewById(R.id.configdeflevsp)
        val language: Spinner = findViewById(R.id.configlangsp)
        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        language.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                println("Started : $started")

                if (started) {
                    changed = true
                    val ed1 = shared.edit()
                    ed1.putInt("Language", position)
                    ed1.apply()
                    var lang1 = locales[position]
                    if (lang1 == "") lang1 = Resources.getSystem().configuration.locales[0].language
                    if (StaticStore.units != null || StaticStore.enemies != null) Revalidater.validate(lang1, this@ConfigScreen) else {
                        StaticStore.getLang(position)
                    }

                    restart()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        deflev.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val ed1 = shared.edit()
                ed1.putInt("default_level", deflev.selectedItem as Int)
                ed1.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        started = true

        super.onStart()
    }

    private fun getIndex(spinner: Spinner, lev: Int): Int {
        var index = 0
        for (i in 0 until spinner.count) if (lev == spinner.getItemAtPosition(i) as Int) index = i
        return index
    }

    private fun restart() {
        println(Arrays.toString(Throwable().stackTrace))
        if(!started) return

        val intent = Intent(this@ConfigScreen, ConfigScreen::class.java)
        startActivity(intent)
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language", 0) ?: 0

        println("Lang : $lang")

        val config = Configuration()

        var language = StaticStore.lang[lang]

        if (language == "")
            language = Resources.getSystem().configuration.locales.get(0).language

        config.setLocale(Locale(language))

        applyOverrideConfiguration(config)

        super.attachBaseContext(LocaleManager.langChange(newBase, shared?.getInt("Language", 0) ?: 0))
    }

    override fun onBackPressed() {
        val back = findViewById<FloatingActionButton>(R.id.configback)
        back!!.performClick()
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