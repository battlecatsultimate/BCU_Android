package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.Revalidater
import com.mandarin.bcu.androidutil.StaticStore
import common.util.Data
import java.util.*

class SearchFilter : AppCompatActivity() {
    private val tgid = intArrayOf(R.id.schchrd, R.id.schchfl, R.id.schchbla, R.id.schchme, R.id.schchan, R.id.schchal, R.id.schchzo, R.id.schchre, R.id.schchwh)
    private val colors = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "0")
    private val rareid = intArrayOf(R.id.schchba, R.id.schchex, R.id.schchr, R.id.schchsr, R.id.schchur, R.id.schchlr)
    private val rarity = arrayOf("0", "1", "2", "3", "4", "5")
    private val atkid = intArrayOf(R.id.schchld, R.id.schchom, R.id.schchmu)
    private val atks = arrayOf("2", "4", "3")
    private val abid = intArrayOf(R.id.schchabwe, R.id.schchabfr, R.id.schchabsl, R.id.schchabta, R.id.schchabst, R.id.schchabre, R.id.schchabir, R.id.schchabmd, R.id.schchabid, R.id.schchabkb, R.id.schchabwp, R.id.schchabstr, R.id.schchabsu, R.id.schchabcd,
            R.id.schchabcr, R.id.schchabzk, R.id.schchabbb, R.id.schchabem, R.id.schchabme, R.id.schchabwv, R.id.schchabimwe, R.id.schchabimfr, R.id.schchabimsl, R.id.schchabimkb, R.id.schchabimwv, R.id.schchabimwp, R.id.schchabimcu, R.id.schchabws,
            R.id.schchabwik, R.id.schchabevk, R.id.schchabsb, R.id.schchabiv)
    private val abtool = intArrayOf(R.string.sch_abi_we, R.string.sch_abi_fr, R.string.sch_abi_sl, R.string.sch_abi_ao, R.string.sch_abi_st, R.string.sch_abi_re, R.string.sch_abi_it, R.string.sch_abi_md, R.string.sch_abi_id, R.string.sch_abi_kb,
            R.string.sch_abi_wa, R.string.sch_abi_str, R.string.sch_abi_su, R.string.sch_abi_bd, R.string.sch_abi_cr, R.string.sch_abi_zk, R.string.sch_abi_bb, R.string.sch_abi_em, R.string.sch_abi_me, R.string.sch_abi_wv,
            R.string.sch_abi_iw, R.string.sch_abi_if, R.string.sch_abi_is, R.string.sch_abi_ik, R.string.sch_abi_iwv, R.string.sch_abi_iwa, R.string.sch_abi_ic, R.string.sch_abi_ws, R.string.sch_abi_wk, R.string.sch_abi_eva, R.string.sch_abi_sb, R.string.sch_abi_iv)
    private val tgtool = intArrayOf(R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_re, R.string.sch_wh)
    private val abils = arrayOf(intArrayOf(1, Data.P_WEAK), intArrayOf(1, Data.P_STOP), intArrayOf(1, Data.P_SLOW), intArrayOf(0, Data.AB_ONLY), intArrayOf(0, Data.AB_GOOD), intArrayOf(0, Data.AB_RESIST), intArrayOf(0, Data.AB_RESISTS), intArrayOf(0, Data.AB_MASSIVE), intArrayOf(0, Data.AB_MASSIVES), intArrayOf(1, Data.P_KB), intArrayOf(1, Data.P_WARP), intArrayOf(1, Data.P_STRONG), intArrayOf(1, Data.P_LETHAL), intArrayOf(0, Data.AB_BASE), intArrayOf(1, Data.P_CRIT), intArrayOf(0, Data.AB_ZKILL), intArrayOf(1, Data.P_BREAK), intArrayOf(0, Data.AB_EARN), intArrayOf(0, Data.AB_METALIC), intArrayOf(1, Data.P_WAVE), intArrayOf(1, Data.P_IMUWEAK), intArrayOf(1, Data.P_IMUSTOP), intArrayOf(1, Data.P_IMUSLOW), intArrayOf(1, Data.P_IMUKB), intArrayOf(1, Data.P_IMUWAVE), intArrayOf(1, Data.P_IMUWARP), intArrayOf(1, Data.P_IMUCURSE), intArrayOf(0, Data.AB_WAVES), intArrayOf(0, Data.AB_WKILL), intArrayOf(0, Data.AB_EKILL), intArrayOf(1, Data.P_SATK), intArrayOf(1, Data.P_IMUATK))
    private val rarities = arrayOfNulls<CheckBox>(rareid.size)
    private val targets = arrayOfNulls<CheckBox>(tgid.size)
    private val attacks = arrayOfNulls<CheckBox>(atkid.size)
    private val abilities = arrayOfNulls<CheckBox>(abid.size)
    private val atkdraw = intArrayOf(212, 112)
    private val tgdraw = intArrayOf(219, 220, 221, 222, 223, 224, 225, 226, 227)
    private val abdraw = intArrayOf(195, 197, 198, 202, 203, 204, 122, 206, 114, 207, 266, 196, 199, 200, 201, 260, 264, 205, 209, 208, 213, 214, 215, 216, 210, 262, 116, 218, 258, 110, 229, 231)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (StaticStore.img15 == null) StaticStore.readImg()
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
        setContentView(R.layout.activity_search_filter)
        val tgor = findViewById<RadioButton>(R.id.schrdtgor)
        val atkmu = findViewById<RadioButton>(R.id.schrdatkmu)
        atkmu.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        val atksi = findViewById<RadioButton>(R.id.schrdatksi)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(211, 40f), null)
            atksi.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 40f), null)
        } else {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(211, 32f), null)
            atksi.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 32f), null)
        }
        atksi.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        val atkor = findViewById<RadioButton>(R.id.schrdatkor)
        val abor = findViewById<RadioButton>(R.id.schrdabor)
        for (i in tgid.indices) {
            targets[i] = findViewById(tgid[i])
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) targets[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(tgdraw[i], 40f), null) else targets[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(tgdraw[i], 32f), null)
            targets[i]?.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        }
        for (i in rareid.indices) rarities[i] = findViewById(rareid[i])
        for (i in atkid.indices) {
            attacks[i] = findViewById(atkid[i])
            if (i < atkid.size - 1) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) attacks[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(atkdraw[i], 40f), null) else attacks[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(atkdraw[i], 32f), null)
                attacks[i]?.compoundDrawablePadding = StaticStore.dptopx(8f, this)
            }
        }
        for (i in abid.indices) {
            abilities[i] = findViewById(abid[i])
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) abilities[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(abdraw[i], 40f), null) else abilities[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(abdraw[i], 32f), null)
            abilities[i]?.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        }
        tgor.isChecked = true
        atkor.isChecked = true
        abor.isChecked = true
        checker()
        listeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {
        val back = findViewById<FloatingActionButton>(R.id.schbck)
        val reset = findViewById<FloatingActionButton>(R.id.schreset)
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val tgor = findViewById<RadioButton>(R.id.schrdtgor)
        val atkor = findViewById<RadioButton>(R.id.schrdatkor)
        val abor = findViewById<RadioButton>(R.id.schrdabor)
        val chnp = findViewById<CheckBox>(R.id.schnp)
        val tggroup = findViewById<RadioGroup>(R.id.schrgtg)
        val atkgroupor = findViewById<RadioGroup>(R.id.schrgatkor)
        val abgroup = findViewById<RadioGroup>(R.id.schrgab)
        val atkmu = findViewById<RadioButton>(R.id.schrdatkmu)
        back!!.setOnClickListener { returner() }
        reset!!.setOnClickListener {
            StaticStore.filterReset()
            atkgroup!!.clearCheck()
            tgor!!.isChecked = true
            atkor!!.isChecked = true
            abor!!.isChecked = true
            chnp!!.isChecked = false
            for (rarity1 in rarities) {
                if (rarity1!!.isChecked) rarity1.isChecked = false
            }
            for (attack1 in attacks) {
                if (attack1!!.isChecked) attack1.isChecked = false
            }
            for (target in targets) {
                if (target!!.isChecked) target.isChecked = false
            }
            for (ability1 in abilities) if (ability1!!.isChecked) ability1.isChecked = false
        }
        tggroup!!.setOnCheckedChangeListener { _, checkedId -> StaticStore.tgorand = checkedId == tgor!!.id }
        atkgroup!!.setOnCheckedChangeListener { _, checkedId -> StaticStore.atksimu = checkedId == atkmu!!.id }
        atkgroupor!!.setOnCheckedChangeListener { _, checkedId -> StaticStore.atkorand = checkedId == atkor!!.id }
        abgroup!!.setOnCheckedChangeListener { _, checkedId -> StaticStore.aborand = checkedId == abor!!.id }
        for (i in targets.indices) {
            targets[i]!!.setOnCheckedChangeListener { _, isChecked -> if (isChecked) StaticStore.tg.add(colors[i]) else StaticStore.tg.remove(colors[i]) }
            targets[i]!!.setOnLongClickListener { v ->
                StaticStore.showShortMessage(v.context, tgtool[i])
                true
            }
        }
        for (i in rarities.indices) {
            rarities[i]!!.setOnCheckedChangeListener { _, isChecked -> if (isChecked) StaticStore.rare.add(rarity[i]) else StaticStore.rare.remove(rarity[i]) }
        }
        for (i in attacks.indices) {
            attacks[i]!!.setOnCheckedChangeListener { _, isChecked -> if (isChecked) StaticStore.attack.add(atks[i]) else StaticStore.attack.remove(atks[i]) }
        }
        for (i in abilities.indices) {
            abilities[i]!!.setOnLongClickListener { v ->
                StaticStore.showShortMessage(v.context, abtool[i])
                true
            }
            abilities[i]!!.setOnCheckedChangeListener { _, isChecked ->
                val abilval = ArrayList<Int>()
                for (j in abils[i]) abilval.add(j)
                if (isChecked) {
                    StaticStore.ability.add(abilval)
                } else StaticStore.ability.remove(abilval)
            }
        }
        chnp!!.setOnCheckedChangeListener { _, isChecked -> StaticStore.talents = isChecked }
    }

    private fun returner() {
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val result = Intent()
        StaticStore.empty = atkgroup!!.checkedRadioButtonId == -1
        setResult(Activity.RESULT_OK, result)
        StaticStore.updateList = true
        finish()
    }

    private fun checker() {
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val atkgroupor = findViewById<RadioGroup>(R.id.schrgatkor)
        val tggroup = findViewById<RadioGroup>(R.id.schrgtg)
        val abgroup = findViewById<RadioGroup>(R.id.schrgab)
        val chnp = findViewById<CheckBox>(R.id.schnp)

        if (!StaticStore.empty) atkgroup!!.check(R.id.schrdatkmu)
        if (!StaticStore.atksimu) if (!StaticStore.empty) atkgroup!!.check(R.id.schrdatksi)
        if (!StaticStore.atkorand) atkgroupor!!.check(R.id.schrdatkand)
        if (!StaticStore.tgorand) tggroup!!.check(R.id.schrdtgand)
        if (!StaticStore.aborand) abgroup!!.check(R.id.schrdaband)
        for (i in rarity.indices) if (StaticStore.rare != null && StaticStore.rare.contains(rarity[i])) rarities[i]!!.isChecked = true
        for (i in atks.indices) if (StaticStore.attack != null && StaticStore.attack.contains(atks[i])) attacks[i]!!.isChecked = true
        for (i in colors.indices) if (StaticStore.tg != null && StaticStore.tg.contains(colors[i])) targets[i]!!.isChecked = true
        for (i in abils.indices) {
            val checker = ArrayList<Int>()
            for (j in abils[i]) checker.add(j)
            if (StaticStore.ability.contains(checker)) abilities[i]!!.isChecked = true
        }
        if (StaticStore.talents) chnp!!.isChecked = true
    }

    private fun getResizeDraw(id: Int, dp: Float): BitmapDrawable {
        val bd = BitmapDrawable(resources, StaticStore.getResizeb(StaticStore.img15[id].bimg() as Bitmap, this, dp))
        bd.isFilterBitmap = true
        bd.setAntiAlias(true)
        return bd
    }

    override fun onBackPressed() {
        val back = findViewById<FloatingActionButton>(R.id.schbck)
        back!!.performClick()
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
}