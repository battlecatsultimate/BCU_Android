package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.Revalidater
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.ColorPickerView
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

open class ConfigScreen : AppCompatActivity() {
    companion object {
        var revalidate: Boolean = false
    }

    private val langId = intArrayOf(R.string.lang_auto, R.string.def_lang_en, R.string.def_lang_zh, R.string.def_lang_ko, R.string.def_lang_ja, R.string.def_lang_ru, R.string.def_lang_fr, R.string.def_lang_it, R.string.def_lang_es, R.string.def_lang_de)
    private val langCode = arrayOf("","en","zh","ko","ja","ru","fr","it","es","de")

    private val df: DecimalFormat

    private var started = false
    private var changed = false

    init {
        val nf = NumberFormat.getInstance(Locale.US)
        df = nf as DecimalFormat

        df.applyPattern("#.##")
    }

    @SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
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

        setContentView(R.layout.activity_config_screen)

        if(revalidate) {
            val l = Locale.getDefault().language
            Revalidater.validate(l, this)
        }

        val back = findViewById<ImageButton>(R.id.configback)

        back.setOnClickListener (object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@ConfigScreen, MainActivity::class.java)
                intent.putExtra("Config", true)
                startActivity(intent)
                finish()
            }

        })

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

        deflev.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val ed1 = shared.edit()
                ed1.putInt("default_level", deflev.selectedItem as Int)
                ed1.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val apktest = findViewById<SwitchCompat>(R.id.apktest)

        apktest.isChecked = shared.getBoolean("apktest", false)

        val senderr = findViewById<SwitchCompat>(R.id.senderror)

        senderr.isChecked = shared.getBoolean("upload", false)

        val lazylineup = findViewById<SwitchCompat>(R.id.linesave)

        lazylineup.isChecked = shared.getBoolean("lazylineup", false)

        senderr.setOnCheckedChangeListener { _, isChecked ->
            val ed1 = shared.edit()
            ed1.putBoolean("upload", isChecked)
            ed1.apply()

            StaticStore.upload = shared.getBoolean("upload",false) || shared.getBoolean("ask_upload",true)
        }

        apktest.setOnCheckedChangeListener { _, isChecked ->
            val ed1 = shared.edit()

            ed1.putBoolean("apktest", isChecked)
            ed1.apply()
        }


        lazylineup.setOnCheckedChangeListener { _, isChecked ->
            val editor = shared.edit()

            editor.putBoolean("lazylineup", isChecked)
            editor.apply()
        }

        val language = findViewById<Spinner>(R.id.configlangsp)

        val lang: MutableList<String> = ArrayList()

        for (i1 in langId) {
            lang.add(getString(i1))
        }

        val adapter = ArrayAdapter(this, R.layout.spinneradapter, lang)

        var realSelection = langCode.asList().indexOf(StaticStore.lang[shared.getInt("Language", 0)])

        if(realSelection == -1) {
            realSelection = 0
        }

        language.adapter = adapter
        language.setSelection(realSelection, false)

        language.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (started) {
                    changed = true

                    val ed1 = shared.edit()

                    var l = StaticStore.lang.asList().indexOf(langCode[position])

                    if(l == -1) {
                        l = 0
                    }

                    ed1.putInt("Language", l)
                    ed1.apply()

                    revalidate = true

                    StaticStore.getLang(l)

                    restart()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        language.post {
            started = true
        }

        val unitinfland = findViewById<RadioGroup>(R.id.configinfland)
        val unitinflandlist = findViewById<RadioButton>(R.id.configlaylandlist)
        val unitinflandslide = findViewById<RadioButton>(R.id.configlaylandslide)

        if (shared.getBoolean("Lay_Land", true))
            unitinflandslide.isChecked = true
        else
            unitinflandlist.isChecked = true

        unitinfland.setOnCheckedChangeListener { _, checkedId ->
            val ed1 = shared.edit()
            ed1.putBoolean("Lay_Land", checkedId == unitinflandslide.id)
            ed1.apply()
        }

        val unitinfport = findViewById<RadioGroup>(R.id.configinfport)
        val unitinfportlist = findViewById<RadioButton>(R.id.configlayportlist)
        val unitinfportslide = findViewById<RadioButton>(R.id.configlayportslide)

        if (shared.getBoolean("Lay_Port", true))
            unitinfportslide.isChecked = true
        else
            unitinfportlist.isChecked = true

        unitinfport.setOnCheckedChangeListener { _, checkedId ->
            val ed1 = shared.edit()
            ed1.putBoolean("Lay_Port", checkedId == unitinfportslide.id)
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

        val axis = findViewById<SwitchCompat>(R.id.configaxis)

        axis.isChecked = shared.getBoolean("Axis", true)

        axis.setOnCheckedChangeListener { _, isChecked ->
            val ed1 = shared.edit()
            ed1.putBoolean("Axis", isChecked)
            ed1.apply()
        }

        val fps = findViewById<SwitchCompat>(R.id.configfps)

        fps.isChecked = shared.getBoolean("FPS", true)

        fps.setOnCheckedChangeListener { _, isChecked ->
            val ed1 = shared.edit()
            ed1.putBoolean("FPS", isChecked)
            ed1.apply()
        }

        val gifseek = findViewById<SeekBar>(R.id.configgifseek)
        val gif = findViewById<TextView>(R.id.configgifsize)

        gif.text = getString(R.string.config_gifsize).replace("_", shared.getInt("gif", 100).toString())
        gifseek.max = 80

        gifseek.progress = shared.getInt("gif", 100) - 20

        gifseek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2) {
                    val prog = p1 + 20

                    val editor = shared.edit()
                    editor.putInt("gif", prog)
                    editor.apply()

                    gif.text = getString(R.string.config_gifsize).replace("_", prog.toString())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        val mus = findViewById<SwitchCompat>(R.id.configmus)
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
                SoundHandler.mu_vol = StaticStore.getVolumScaler(shared.getInt("mus_vol", 99))
                musvol.isEnabled = true
            } else {
                val editor = shared.edit()
                editor.putBoolean("music", false)
                editor.apply()
                SoundHandler.musicPlay = false
                SoundHandler.mu_vol = 0f
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

        val soundeff = findViewById<SwitchCompat>(R.id.configse)
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
                SoundHandler.sePlay = true
                SoundHandler.se_vol = StaticStore.getVolumScaler((shared.getInt("se_vol", 99) * 0.85).toInt())
                sevol.isEnabled = true
            } else {
                val editor = shared.edit()
                editor.putBoolean("SE", false)
                editor.apply()
                SoundHandler.sePlay = false
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

        val ui = findViewById<SwitchCompat>(R.id.configui)
        val uivol = findViewById<SeekBar>(R.id.configuivol)

        ui.setOnCheckedChangeListener { _, c ->
            val editor = shared.edit()

            editor.putBoolean("UI", c)
            editor.apply()

            SoundHandler.uiPlay = c
            SoundHandler.ui_vol = if(c) {
                StaticStore.getVolumScaler((shared.getInt("ui_vol", 99) * 0.85).toInt())
            } else {
                0f
            }

            uivol.isEnabled = c
        }

        uivol.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (progress >= 100 || progress < 0) return
                    val editor = shared.edit()
                    editor.putInt("ui_vol", progress)
                    editor.apply()
                    SoundHandler.ui_vol = StaticStore.getVolumScaler((progress * 0.85).toInt())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        ui.isChecked = shared.getBoolean("UI", true)
        uivol.isEnabled = shared.getBoolean("UI", true)
        uivol.max = 99
        uivol.progress = shared.getInt("ui_vol", 99)

        val row = findViewById<SwitchCompat>(R.id.configrow)

        row.isChecked = shared.getBoolean("rowlayout", true)
        row.text = if(CommonStatic.getConfig().twoRow) {
            getString(R.string.battle_tworow)
        } else {
            getString(R.string.battle_onerow)
        }

        row.setOnCheckedChangeListener {_, isChecked ->
            val editor = shared.edit()
            editor.putBoolean("rowlayout", isChecked)
            CommonStatic.getConfig().twoRow = isChecked
            editor.apply()
        }

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

        val reset = findViewById<MaterialButton>(R.id.configreset)

        reset.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@ConfigScreen, DataResetManager::class.java)

                startActivity(intent)
                finish()
            }
        })

        val bgEff = findViewById<SwitchCompat>(R.id.configbgeff)

        bgEff.setOnCheckedChangeListener { _, c ->
            CommonStatic.getConfig().drawBGEffect = c

            val editor = shared.edit()

            editor.putBoolean("bgeff", c)
            editor.apply()
        }

        bgEff.isChecked = shared.getBoolean("bgeff", true)

        val unitDelay = findViewById<SwitchCompat>(R.id.configdelay)

        unitDelay.setOnCheckedChangeListener { _, c ->
            CommonStatic.getConfig().buttonDelay = c

            val editor = shared.edit()

            editor.putBoolean("unitDelay", c)
            editor.apply()
        }

        unitDelay.isChecked = shared.getBoolean("unitDelay", true)

        val currentColorText = findViewById<TextView>(R.id.viewcurrentcol)

        val viewerColor = shared.getInt("viewerColor", -1)

        currentColorText.text = if(viewerColor == -1)
            getString(R.string.unit_info_t_none)
        else
            toSixHexString(Integer.toHexString(viewerColor and 0xFFFFFF))

        if(viewerColor != -1) {
            currentColorText.setTextColor(viewerColor)
        }

        val viewColor = findViewById<MaterialButton>(R.id.viewcolpick)

        viewColor.setOnClickListener(object : SingleClick() {
            @SuppressLint("SetTextI18n")
            override fun onSingleClick(v: View?) {
                var currentColor = shared.getInt("viewerColor", Color.RED)

                if(currentColor == -1)
                    currentColor = Color.RED

                StaticStore.fixOrientation(this@ConfigScreen)

                val dialog = Dialog(this@ConfigScreen)

                dialog.setContentView(R.layout.color_picker_popup)

                val picker = dialog.findViewById<ColorPickerView>(R.id.picker)

                val confirm = dialog.findViewById<Button>(R.id.colorconf)
                val cancel = dialog.findViewById<Button>(R.id.colorcanc)

                val modeIDs = intArrayOf(
                    R.id.colorhue, R.id.colorsatu, R.id.colorbrigh,
                    R.id.colorred, R.id.colorgre, R.id.colorblu
                )

                val textIDs = intArrayOf(
                    R.id.colorhuet, R.id.colorsatut, R.id.colorbright,
                    R.id.colorredt, R.id.colorgret, R.id.colorblut
                )

                val modes = Array<RadioButton>(modeIDs.size) {
                    dialog.findViewById(modeIDs[it])
                }

                val texts = Array<TextInputEditText>(textIDs.size) {
                    dialog.findViewById(textIDs[it])
                }

                val hex = dialog.findViewById<TextInputEditText>(R.id.colorhext)

                texts[0].setText(picker.hsb[0].roundToInt().toString())
                texts[1].setText((picker.hsb[1] * 100).roundToInt().toString())
                texts[2].setText((picker.hsb[2] * 100).roundToInt().toString())
                texts[3].setText(picker.rgb[0].toString())
                texts[4].setText(picker.rgb[1].toString())
                texts[5].setText(picker.rgb[2].toString())

                hex.setText(toSixHexString(Integer.toHexString(picker.getColor() and 0xFFFFFF)))

                val modeData = arrayOf(
                    ColorPickerView.MODE.HUE, ColorPickerView.MODE.SATURATION, ColorPickerView.MODE.BRIGHTNESS,
                    ColorPickerView.MODE.RED, ColorPickerView.MODE.GREEN, ColorPickerView.MODE.BLUE
                )

                var editing = true

                for(m in modes.indices) {
                    modes[m].setOnClickListener {
                        for(n in modes.indices) {
                            if(n != m) {
                                modes[n].isChecked = false
                            }
                        }

                        picker.changeMode(modeData[m])
                    }

                    texts[m].addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                        override fun afterTextChanged(p0: Editable?) {
                            if(!editing)
                                return

                            val maxValue = when(m) {
                                0 -> 360
                                1 -> 100
                                2 -> 100
                                else -> 255
                            }

                            var number = p0?.toString() ?: "0"

                            if(number.isBlank())
                                number = "0"

                            var value = CommonStatic.safeParseInt(number)

                            if(value > maxValue) {
                                value = maxValue
                            }

                            when(m) {
                                0, 1, 2 -> {
                                    val fa = when(m) {
                                        0 -> floatArrayOf(value.toFloat(), picker.hsb[1], picker.hsb[2])
                                        1 -> floatArrayOf(picker.hsb[0], value / 100f, picker.hsb[2])
                                        else -> floatArrayOf(picker.hsb[0], picker.hsb[1], value / 100f)
                                    }

                                    picker.setHex(Color.HSVToColor(fa) and 0xFFFFFF)
                                }
                                else -> {
                                    val c = when(m) {
                                        3 -> Color.rgb(value, picker.rgb[1], picker.rgb[2])
                                        4 -> Color.rgb(picker.rgb[0], value, picker.rgb[2])
                                        else -> Color.rgb(picker.rgb[0], picker.rgb[1], value)
                                    }

                                    picker.setHex(c and 0xFFFFFF)
                                }
                            }
                        }
                    })

                    texts[m].setOnEditorActionListener { _, action, _ ->
                        if(action == EditorInfo.IME_ACTION_DONE) {
                            val view = dialog.currentFocus

                            if(view != null) {
                                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                            }

                            texts[m].postDelayed( {
                                texts[m].clearFocus()
                            }, 10)

                            return@setOnEditorActionListener true
                        }

                        return@setOnEditorActionListener false
                    }
                }

                hex.setOnEditorActionListener { _, action, _ ->
                    if(action == EditorInfo.IME_ACTION_DONE) {
                        val s = hex.text?.toString() ?: "0"

                        if(s.matches(Regex("-?[\\da-fA-F]+"))) {
                            var value = Integer.parseInt(s, 16)

                            if(value > 0xFFFFFF) {
                                value = 0xFFFFFF
                            }

                            picker.setHex(value)
                        } else {
                            hex.setText(toSixHexString(Integer.toHexString(picker.getColor() and 0xFFFFFF)))
                        }

                        val view = dialog.currentFocus

                        if(view != null) {
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                            imm.hideSoftInputFromWindow(view.windowToken, 0)
                        }

                        if(hex.hasFocus())
                            hex.clearFocus()

                        return@setOnEditorActionListener true
                    }

                    return@setOnEditorActionListener false
                }

                confirm.setOnClickListener {
                    val editor = shared.edit()

                    editor.putInt("viewerColor", picker.getColor())
                    editor.apply()

                    CommonStatic.getConfig().viewerColor = picker.getColor()

                    currentColorText.text = toSixHexString(Integer.toHexString(picker.getColor() and 0xFFFFFF))
                    currentColorText.setTextColor(picker.getColor())

                    dialog.dismiss()
                }

                cancel.setOnClickListener {
                    dialog.dismiss()
                }

                val callback = Runnable {
                    runOnUiThread {
                        editing = false

                        texts[0].setText(picker.hsb[0].roundToInt().toString())
                        texts[1].setText((picker.hsb[1] * 100).roundToInt().toString())
                        texts[2].setText((picker.hsb[2] * 100).roundToInt().toString())
                        texts[3].setText(picker.rgb[0].toString())
                        texts[4].setText(picker.rgb[1].toString())
                        texts[5].setText(picker.rgb[2].toString())

                        for(m in 0..5) {
                            if(texts[m].hasFocus())
                                texts[m].setSelection(texts[m].text?.toString()?.length ?: 0)
                        }

                        hex.setText(toSixHexString(Integer.toHexString(picker.getColor() and 0xFFFFFF)))

                        editing = true
                    }
                }

                picker.callBack = callback
                picker.setHex(currentColor)

                modes[0].isChecked = true

                dialog.show()

                dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

                dialog.setOnDismissListener {
                    StaticStore.unfixOrientation(this@ConfigScreen)
                }
            }
        })

        val viewColorReset = findViewById<Button>(R.id.viewcolreset)

        viewColorReset.setOnClickListener {
            val editor = shared.edit()

            editor.putInt("viewerColor", -1)
            editor.apply()

            CommonStatic.getConfig().viewerColor = -1

            currentColorText.text = getString(R.string.unit_info_t_none)
            currentColorText.setTextColor(StaticStore.getAttributeColor(this@ConfigScreen, R.attr.TextPrimary))
        }

        val exContinue = findViewById<SwitchCompat>(R.id.configex)

        exContinue.isChecked = shared.getBoolean("exContinue", true)

        exContinue.setOnCheckedChangeListener { _, c ->
            CommonStatic.getConfig().exContinuation = c

            val editor = shared.edit()

            editor.putBoolean("exContinue", c)
            editor.apply()
        }

        val realEx = findViewById<SwitchCompat>(R.id.configrealex)

        realEx.isChecked = shared.getBoolean("realEx", false)

        realEx.setOnCheckedChangeListener {_, c ->
            CommonStatic.getConfig().realEx = c

            val editor = shared.edit()

            editor.putBoolean("realEx", c)
            editor.apply()
        }

        val shake = findViewById<SwitchCompat>(R.id.configshake)

        shake.isChecked = shared.getBoolean("shake", true)

        shake.setOnCheckedChangeListener {_, c ->
            CommonStatic.getConfig().shake = c

            val editor = shared.edit()

            editor.putBoolean("shake", c)
            editor.apply()
        }

        val showst = findViewById<SwitchCompat>(R.id.configshowst)

        showst.isChecked = shared.getBoolean("showst", true)

        showst.setOnCheckedChangeListener { _, c ->
            CommonStatic.getConfig().stageName = c

            val editor = shared.edit()

            editor.putBoolean("showst", c)
            editor.apply()
        }

        val showres = findViewById<SwitchCompat>(R.id.configshowres)

        showres.isChecked = shared.getBoolean("showres", true)

        showres.setOnCheckedChangeListener { _, c ->
            StaticStore.showResult = c

            val editor = shared.edit()

            editor.putBoolean("showres", c)
            editor.apply()
        }

        val reallv = findViewById<SwitchCompat>(R.id.configreallv)

        reallv.isChecked = shared.getBoolean("reallv", false)

        reallv.setOnCheckedChangeListener { _, c ->
            CommonStatic.getConfig().realLevel = c

            val editor = shared.edit()

            editor.putBoolean("reallv", c)
            editor.apply()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                back.performClick()
            }
        })
    }

    private fun getIndex(spinner: Spinner, lev: Int): Int {
        var index = 0
        for (i in 0 until spinner.count) if (lev == spinner.getItemAtPosition(i) as Int) index = i
        return index
    }

    private fun restart() {
        if(!started) return

        val intent = Intent(this@ConfigScreen, ConfigScreen::class.java)
        startActivity(intent)
        finish()
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

    private fun toSixHexString(hex: String) : String {
        val builder = StringBuilder()

        val len = 6 - hex.length

        for(n in 0 until len)
            builder.append("0")

        builder.append(hex)

        return builder.toString().uppercase()
    }
}