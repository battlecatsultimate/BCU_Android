package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.drive.DriveUtil
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private var sendcheck = false
    private var notshowcheck = false
    private var send = false
    private var show = false

    companion object {
        @JvmField
        var isRunning = false

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed = shared.edit()

        if (!shared.contains("initial")) {
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.putBoolean("frame", true)
            ed.putBoolean("apktest", false)
            ed.putInt("default_level", 50)
            ed.putInt("Language", 0)
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

        deleter(File(Environment.getDataDirectory().absolutePath+"/data/com.mandarin.bcu/temp/"))
        deleter(File(StaticStore.getExternalTemp(this)))

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter(StaticStore.getExternalLog(this), shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)))

        setContentView(R.layout.activity_main)

        SoundHandler.musicPlay = shared.getBoolean("music", true)
        SoundHandler.mu_vol = if(shared.getBoolean("music", true)) {
            StaticStore.getVolumScaler(shared.getInt("mus_vol", 99))
        } else {
            0f
        }
        SoundHandler.sePlay = shared.getBoolean("SE", true)
        SoundHandler.se_vol = if(shared.getBoolean("SE", true)) {
            StaticStore.getVolumScaler((shared.getInt("se_vol", 99) * 0.85).toInt())
        } else {
            0f
        }
        SoundHandler.uiPlay = shared.getBoolean("UI", true)
        SoundHandler.ui_vol = if(SoundHandler.uiPlay)
            StaticStore.getVolumScaler((shared.getInt("ui_vol", 99) * 0.85).toInt())
        else
            0f
        StaticStore.upload = shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)
        CommonStatic.getConfig().twoRow = shared.getBoolean("rowlayout", true)
        CommonStatic.getConfig().levelLimit = shared.getInt("levelLimit", 0)
        CommonStatic.getConfig().plus = shared.getBoolean("unlockPlus", true)
        CommonStatic.getConfig().drawBGEffect = shared.getBoolean("bgeff", true)
        CommonStatic.getConfig().buttonDelay = shared.getBoolean("unitDelay", true)
        CommonStatic.getConfig().viewerColor = shared.getInt("viewerColor", -1)
        CommonStatic.getConfig().exContinuation = shared.getBoolean("exContinue", true)
        CommonStatic.getConfig().realEx = shared.getBoolean("realEx", false)
        CommonStatic.getConfig().shake = shared.getBoolean("shake", true)
        CommonStatic.getConfig().stageName = shared.getBoolean("showst", true)
        StaticStore.showResult = shared.getBoolean("showres", true)
        CommonStatic.getConfig().realLevel = shared.getBoolean("reallv", false)
        CommonStatic.getConfig().deadOpa = 0
        CommonStatic.getConfig().fullOpa = 100

        val result = intent
        var conf = false
        val bundle = result.extras

        if (bundle != null)
            conf = bundle.getBoolean("Config")

        val upath = Environment.getDataDirectory().absolutePath + "/data/com.mandarin.bcu/upload/"
        val upload = File(upath)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (upload.exists() && upload.listFiles()?.isNotEmpty() == true && connectivityManager.activeNetwork != null) {
            if (shared.getBoolean("ask_upload", true) && !StaticStore.dialogisShowed && !conf) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

                val builder = AlertDialog.Builder(this)
                val inflater = LayoutInflater.from(this)
                val v = inflater.inflate(R.layout.error_dialog, null)

                builder.setView(v)

                val yes = v.findViewById<Button>(R.id.errorupload)
                val no = v.findViewById<Button>(R.id.errorno)
                val group = v.findViewById<RadioGroup>(R.id.radio)
                val donotshow = v.findViewById<RadioButton>(R.id.radionotshow)
                val always = v.findViewById<RadioButton>(R.id.radiosend)

                always.setOnClickListener {
                    if (sendcheck && send) {
                        group.clearCheck()

                        val editor = shared.edit()

                        editor.putBoolean("upload", false)
                        editor.putBoolean("ask_upload", true)
                        editor.apply()

                        sendcheck = false
                        notshowcheck = false
                        send = false
                        show = false
                    } else if (sendcheck || send) {
                        send = true
                    }
                }
                donotshow.setOnClickListener {
                    if (notshowcheck && show) {
                        group.clearCheck()

                        val editor = shared.edit()

                        editor.putBoolean("upload", false)
                        editor.putBoolean("ask_upload", true)
                        editor.apply()

                        sendcheck = false
                        notshowcheck = false
                        send = false
                        show = false
                    } else if (notshowcheck || show) {
                        show = true
                    }
                }
                group.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId == donotshow.id) {
                        val editor = shared.edit()

                        editor.putBoolean("upload", false)
                        editor.putBoolean("ask_upload", false)
                        editor.apply()

                        notshowcheck = true
                        sendcheck = false
                        send = false
                        show = false
                    } else {
                        val editor = shared.edit()

                        editor.putBoolean("upload", true)
                        editor.putBoolean("ask_upload", false)
                        editor.apply()

                        notshowcheck = false
                        sendcheck = true
                        send = false
                        show = false
                    }
                }
                val dialog = builder.create()

                dialog.setCancelable(true)
                dialog.show()

                no.setOnClickListener {
                    deleter(upload)
                    dialog.dismiss()
                }

                yes.setOnClickListener {
                    StaticStore.showShortMessage(this@MainActivity, R.string.main_err_start)
                    dialog.dismiss()

                    uploadLog()
                }

                dialog.setOnDismissListener {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

                    StaticStore.dialogisShowed = true
                }
            } else if (shared.getBoolean("upload", false)) {
                StaticStore.showShortMessage(this, R.string.main_err_upload)

                uploadLog()
            }
        }

        isRunning = true

        val grid = findViewById<GridLayout>(R.id.maingrid)

        val drawables = intArrayOf(R.drawable.ic_kasa_jizo, R.drawable.ic_enemy, R.drawable.ic_castle,
                R.drawable.ic_medal, R.drawable.ic_basis, R.drawable.ic_bg, R.drawable.ic_castles,
                R.drawable.ic_music, R.drawable.ic_effect, R.drawable.ic_pack, R.drawable.ic_baseline_folder_24)

        val classes = arrayOf(AnimationViewer::class.java, EnemyList::class.java, MapList::class.java,
                MedalList::class.java, LineUpScreen::class.java, BackgroundList::class.java, CastleList::class.java,
                MusicList::class.java, EffectList::class.java, PackManagement::class.java, AssetBrowser::class.java)

        val texts = intArrayOf(R.string.main_unitinfo,R.string.main_enemy_info, R.string.stg_inf,
                R.string.main_medal, R.string.main_equip, R.string.main_bg, R.string.main_castle,
                R.string.main_music, R.string.main_effect, R.string.main_packs, R.string.main_asset)

        val row = 7
        val col = 2 // unit/enem | stage,medal | basis | bg,castles | music,effect | pack | asset

        val gap = StaticStore.dptopx(4f, this)

        val w = StaticStore.getScreenWidth(this, false) - StaticStore.dptopx(32f, this) - gap * 4

        grid.rowCount = row
        grid.columnCount = col

        for(i in 0 until row) {
            if(i == 2 || i == 5 || i == 6) {
                var index = i * 2

                if(i > 2)
                    index--

                if(i > 5)
                    index--

                val card = CardView(this)

                val r = GridLayout.spec(i, 1)
                val c = GridLayout.spec(0 ,2)

                val gParam = GridLayout.LayoutParams(r, c)

                gParam.setMargins(gap, gap, gap, gap)

                card.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val v = LayoutInflater.from(this).inflate(R.layout.main_card_layout, card, false)

                val layout = v.findViewById<ConstraintLayout>(R.id.cardlayout)

                val lParam = layout.layoutParams

                lParam.width = w + gap * 2
                lParam.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                layout.layoutParams = lParam

                val text = v.findViewById<TextView>(R.id.cardname)
                val img = v.findViewById<ImageView>(R.id.cardimg)

                text.setText(texts[index])
                img.setImageDrawable(ContextCompat.getDrawable(this, drawables[index]))

                card.addView(v)

                card.isClickable = true

                card.radius = StaticStore.dptopx(8f, this).toFloat()

                card.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(this@MainActivity, classes[index])

                        startActivity(intent)

                        if(classes[index] == PackManagement::class.java) {
                            finish()
                        }
                    }
                })

                grid.addView(card, gParam)
            } else {
                for(j in 0 until col) {
                    var index = i * 2 + j

                    if(i > 2)
                        index --

                    val card = CardView(this)

                    val r = GridLayout.spec(i, 1)
                    val c = GridLayout.spec(j ,1)

                    val gParam = GridLayout.LayoutParams(r, c)

                    gParam.setMargins(gap, gap, gap, gap)

                    card.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val v = LayoutInflater.from(this).inflate(R.layout.main_card_layout, card, false)

                    val layout = v.findViewById<ConstraintLayout>(R.id.cardlayout)

                    val lParam = layout.layoutParams

                    lParam.width = w / 2
                    lParam.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

                    layout.layoutParams = lParam

                    val text = v.findViewById<TextView>(R.id.cardname)
                    val img = v.findViewById<ImageView>(R.id.cardimg)

                    text.setText(texts[index])
                    img.setImageDrawable(ContextCompat.getDrawable(this, drawables[index]))

                    card.addView(v)

                    card.isClickable = true

                    card.radius = StaticStore.dptopx(8f, this).toFloat()

                    card.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            val intent = Intent(this@MainActivity, classes[index])

                            startActivity(intent)

                            if(classes[index] == PackManagement::class.java) {
                                finish()
                            }
                        }
                    })

                    grid.addView(card, gParam)
                }
            }
        }

        val config = findViewById<FloatingActionButton>(R.id.mainconfig)

        config.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@MainActivity, ConfigScreen::class.java)

                startActivity(intent)
                finish()
            }
        })

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isRunning = false

                StaticStore.dialogisShowed = false

                StaticStore.clear()

                finish()
            }
        })
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
        isRunning = false

        StaticStore.dialogisShowed = false
        StaticStore.toast = null

        super.onDestroy()
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    private fun deleter(f: File) {
        if (f.isDirectory) {
            val lit = f.listFiles() ?: return

            for (g in lit)
                deleter(g)
        } else
            f.delete()
    }

    private fun uploadLog() {
        lifecycleScope.launch {
            val path = Environment.getDataDirectory().absolutePath + "/data/com.mandarin.bcu/upload/"

            val upload = File(path)

            val files = upload.listFiles()

            val total = upload.listFiles()?.size ?: 0

            var succeed = 0
            var failed = 0

            for (i in 0 until total) {
                val f = files?.get(i) ?: return@launch

                val str = getString(R.string.err_send_log).replace("-", (i + 1).toString()).replace("_", total.toString())
                StaticStore.showShortMessage(this@MainActivity, str)

                try {
                    if (safeCheck(f)) {
                        withContext(Dispatchers.IO) {
                            val inputStream = resources.openRawResource(R.raw.service_key)
                            val good = DriveUtil.upload(f, inputStream)

                            if (good) {
                                f.delete()
                                succeed++
                            } else {
                                Log.e("uploadFailed", "Uploading " + f.name + " to server failed")
                                failed++
                            }
                        }
                    } else {
                        f.delete()
                        failed++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    failed++
                }
            }

            val str = getString(R.string.err_send_result).replace("-", succeed.toString()).replace("_", failed.toString())
            StaticStore.showShortMessage(this@MainActivity, str)
        }
    }

    private fun safeCheck(f: File): Boolean {
        val name = f.name

        if (!name.endsWith("txt"))
            return false

        val size = f.length()

        val mb = size / 1024 / 1024

        return mb <= 10
    }
}