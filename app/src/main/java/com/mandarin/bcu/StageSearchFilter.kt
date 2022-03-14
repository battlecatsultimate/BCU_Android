package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.enemy.coroutine.EAdder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Enemy
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList

class StageSearchFilter : AppCompatActivity() {
    companion object {
        val GAME_MUSICS = intArrayOf(
            3,4,5,6,30,31,32,33,34,47,48,49,58,62,66,67,68,69,75,76,77,78,79,80,81,82,87,89,97,98,99,
            100,101,102,103,104,117,118,119,120,122,123,125,126,127,130,131,132,133,140,141,142,144,145
        )
    }

    private val radioid = intArrayOf(R.id.lessthan, R.id.same, R.id.greaterthan)

    private val mdata = ArrayList<String>()
    private val bdata = ArrayList<String>()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data

        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val id = StaticStore.transformIdentifier<AbEnemy>(data.getStringExtra("Data")) ?: return@registerForActivityResult
            val e = id.get() ?: return@registerForActivityResult

            if(e !is Enemy)
                return@registerForActivityResult

            if (!StaticStore.stgenem.contains(e.id)) {
                StaticStore.stgenem.add(e.id)

                val enemygroup = findViewById<ChipGroup>(R.id.enemygroup)

                val chip = Chip(this)
                chip.id = R.id.enemychip + e.id.pack.hashCode() + e.id.id
                chip.text = getEnemyName(e)
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    StaticStore.stgenem.remove(e.id)
                    enemygroup.removeView(chip)
                }

                enemygroup.addView(chip)
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
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
                setTheme(R.style.AppTheme_designNight)
            } else {
                setTheme(R.style.AppTheme_designDay)
            }
        }

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_stage_search_filter)

        val bck = findViewById<FloatingActionButton>(R.id.statschbck)
        val addenemy = findViewById<FloatingActionButton>(R.id.addenemy)
        val orand = arrayOf<RadioButton>(findViewById(R.id.stgenor), findViewById(R.id.stgenand))
        val bosses = arrayOf<RadioButton>(findViewById(R.id.bossall), findViewById(R.id.hasboss), findViewById(R.id.noboss))
        val enemygroup = findViewById<ChipGroup>(R.id.enemygroup)
        val musicspin = findViewById<Spinner>(R.id.musicspinner)
        val bgspin = findViewById<Spinner>(R.id.bgspinner)
        val starspin = findViewById<Spinner>(R.id.starspinner)
        val bhopgroup = findViewById<RadioGroup>(R.id.bhopgroup)
        val radios = arrayOf<RadioButton>(findViewById(radioid[0]), findViewById(radioid[1]), findViewById(radioid[2]))
        val bhedit = findViewById<EditText>(R.id.bhedit)
        val continspin = findViewById<Spinner>(R.id.continspinner)
        val reset = findViewById<FloatingActionButton>(R.id.stgfilterreset)
        val scroll = findViewById<HorizontalScrollView>(R.id.schenemscroll)
        val name = findViewById<EditText>(R.id.stgnameedit)
        val stmname = findViewById<EditText>(R.id.stmnameedit)

        scroll.isHorizontalScrollBarEnabled = false

        for (id in StaticStore.stgenem) {
            val e = Identifier.get(id) ?: continue

            if(e !is Enemy)
                continue

            val chip = Chip(this)
            chip.id = R.id.enemychip + id.pack.hashCode() + id.id
            chip.text = getEnemyName(e)
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                StaticStore.stgenem.remove(id)
                enemygroup.removeView(chip)
            }

            enemygroup.addView(chip)
        }

        if (StaticStore.stgboss in -1..1) {
            bosses[StaticStore.stgboss + 1].isChecked = true
        }

        if (StaticStore.stgenemorand) {
            orand[0].isChecked = true
        } else {
            orand[1].isChecked = true
        }

        for (i in bosses.indices) {
            bosses[i].setOnClickListener {
                StaticStore.stgboss = i - 1
            }
        }

        addenemy.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@StageSearchFilter, EnemyList::class.java)
                intent.putExtra("mode", EAdder.MODE_SELECTION)

                resultLauncher.launch(intent)
            }
        })

        orand[0].setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
            StaticStore.stgenemorand = checked
        }

        if(!StaticStore.stgenemorand)
            orand[1].isChecked = true

        if (StaticStore.stgbh > 0) {
            bhedit.setText(StaticStore.stgbh.toString())
        }

        val musics = ArrayList<String>()
        musics.add(getString(R.string.combo_all))

        for (i in GAME_MUSICS) {
            musics.add(getString(R.string.pack_default) + " - "+number(i))
            mdata.add(Identifier.DEF+" - "+number(i))
        }

        for(i in UserProfile.getAllPacks()) {
            if(i is PackData.DefPack)
                continue

            val ms = i.musics

            for(j in ms.list) {
                val p = StaticStore.getPackName(j.id.pack)

                val musicName = "$p - ${Data.trio(j.id.id)}"

                musics.add(musicName)
                mdata.add(musicName)
            }
        }

        val musicadapter = ArrayAdapter(this, R.layout.spinneradapter, musics)

        musicspin.adapter = musicadapter

        musicspin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                StaticStore.stgmusic = if(position == 0)
                    ""
                else
                    mdata[position-1]
            }
        }

        if (StaticStore.stgmusic.isNotEmpty()) {
            musicspin.setSelection(mdata.indexOf(StaticStore.stgmusic)+1, false)
        }

        val backgrounds = ArrayList<String>()
        backgrounds.add(getString(R.string.combo_all))

        for(i in UserProfile.getBCData().bgs.list) {
            backgrounds.add(getString(R.string.pack_default)+ " - "+number(i.id.id))

            bdata.add(Identifier.DEF+" - "+number(i.id.id))
        }

        for(i in UserProfile.getAllPacks()) {
            if(i is PackData.DefPack)
                continue

            if(i !is PackData.UserPack)
                continue

            val bg = i.bgs.list

            for(b in bg) {
                backgrounds.add(i.desc.names.toString() + " - "+ number(b.id.id))
                bdata.add(StaticStore.getPackName(b.id.pack) + " - "+number(b.id.id))
            }
        }

        val bgadpater = ArrayAdapter(this, R.layout.spinneradapter, backgrounds)

        bgspin.adapter = bgadpater

        bgspin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                StaticStore.stgbg = if(position == 0)
                    ""
                else
                    bdata[position-1]
            }

        }

        if(StaticStore.stgbg.isNotEmpty()) {
            val index = bdata.indexOf(StaticStore.stgbg)

            bgspin.setSelection(index+1, false)
        }

        val stars = arrayOf(getString(R.string.stg_sch_star1),getString(R.string.stg_sch_star2),getString(R.string.stg_sch_star3),getString(R.string.stg_sch_star4))

        val staradapter = ArrayAdapter(this, R.layout.spinneradapter, stars)

        starspin.adapter = staradapter

        starspin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                StaticStore.stgstar = position
            }

        }

        if(StaticStore.stgstar in stars.indices) {
            starspin.setSelection(StaticStore.stgstar, false)
        }

        val contins = arrayOf(getString(R.string.combo_all), getString(R.string.stg_info_poss), getString(R.string.stg_info_impo))

        val continadapter = ArrayAdapter(this, R.layout.spinneradapter, contins)

        continspin.adapter = continadapter

        continspin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                StaticStore.stgcontin = position-1
            }

        }

        if(StaticStore.stgcontin in -1..contins.size-2) {
            continspin.setSelection(StaticStore.stgcontin+1, false)
        }

        if (bhedit.text.toString().isEmpty()) {
            StaticStore.bhop = -1
            for (radio in radios) {
                radio.isEnabled = false
                radio.setTextColor(StaticStore.getAttributeColor(this, R.attr.HintPrimary))
            }
        }

        if (StaticStore.bhop != -1) {
            radios[StaticStore.bhop].isChecked = true
        }

        bhedit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    StaticStore.bhop = -1
                    StaticStore.stgbh = -1
                    for (radio in radios) {
                        radio.isEnabled = false
                        radio.setTextColor(StaticStore.getAttributeColor(this@StageSearchFilter, R.attr.HintPrimary))
                    }
                    bhopgroup.clearCheck()
                } else {
                    if (radios[0].currentTextColor == StaticStore.getAttributeColor(this@StageSearchFilter, R.attr.HintPrimary)) {
                        for (radio in radios) {
                            radio.isEnabled = true
                            radio.setTextColor(StaticStore.getAttributeColor(this@StageSearchFilter, R.attr.TextPrimary))
                        }
                    }

                    if(bhopgroup.checkedRadioButtonId == -1) {
                        radios[1].isChecked = true
                        StaticStore.bhop = 1
                    }

                    try {
                        StaticStore.stgbh = s.toString().toInt()
                    } catch (e : NumberFormatException) {
                        StaticStore.stgbh = Int.MAX_VALUE
                        bhedit.setText(Int.MAX_VALUE.toString())
                        bhedit.setSelection(bhedit.text.toString().length)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        bhedit.setOnEditorActionListener { _: TextView?, id: Int, _: KeyEvent? ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(bhedit.windowToken, 0)
                bhedit.clearFocus()

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        for (i in radios.indices) {
            radios[i].setOnClickListener {
                StaticStore.bhop = i
            }
        }

        bck.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        reset.setOnClickListener {
            name.setText("")
            orand[0].isChecked = true
            bhopgroup.clearCheck()
            bosses[0].isChecked = true
            enemygroup.removeAllViews()
            musicspin.setSelection(0)
            bgspin.setSelection(0)
            starspin.setSelection(0)
            bhedit.setText("")
            continspin.setSelection(0)
            stmname.setText("")

            StaticStore.stgFilterReset()
        }

        if(StaticStore.stgschname != "") {
            name.setText(StaticStore.stgschname)
        }

        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s != null) {
                    StaticStore.stgschname = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        name.setOnEditorActionListener { _: TextView?, id: Int, e: KeyEvent? ->
            if(id == EditorInfo.IME_ACTION_DONE || e?.action == KeyEvent.KEYCODE_BACK) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(bhedit.windowToken, 0)
                name.post {
                    name.clearFocus()
                }

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        if(StaticStore.stmschname != "") {
            stmname.setText(StaticStore.stmschname)
        }

        stmname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s != null) {
                    StaticStore.stmschname = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        stmname.setOnEditorActionListener { _: TextView?, id: Int, e: KeyEvent? ->
            if(id == EditorInfo.IME_ACTION_DONE || e?.action == KeyEvent.KEYCODE_BACK) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(bhedit.windowToken, 0)
                name.post {
                    name.clearFocus()
                }

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
    }

    override fun onBackPressed() {
        val bck = findViewById<FloatingActionButton>(R.id.statschbck)
        bck.performClick()
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

    private fun getEnemyName(id: Enemy): String {
        try {
            val name = MultiLangCont.get(id) ?: id.names.toString()

            val pack = StaticStore.getPackName(id.id.pack)

            if (name == "") {
                return getString(R.string.stg_sch_enemy) + " $pack - ${number(id.id.id)}"
            }

            return name
        } catch (e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, this)
            val pack = StaticStore.getPackName(id.id.pack)

            return getString(R.string.stg_sch_enemy) + " $pack - ${number(id.id.id)}"
        }
    }

    private fun number(id: Int): String {
        return when (id) {
            in 0..9 -> {
                "00$id"
            }
            in 10..99 -> {
                "0$id"
            }
            else -> {
                id.toString()
            }
        }
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }
}
