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
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.DefineItf
import common.util.Data
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*

open class EnemySearchFilter : AppCompatActivity() {
    private val attacks = arrayOfNulls<CheckBox>(3)
    private val trid = intArrayOf(R.id.eschchrd, R.id.eschchfl, R.id.eschchbla, R.id.eschchme, R.id.eschchan, R.id.eschchal, R.id.eschchzo, R.id.eschchre, R.id.eschchwh, R.id.eschwit, R.id.escheva, R.id.eschnone)
    private val traits = arrayOfNulls<CheckBox>(trid.size)
    private val colors = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "0", "10", "9", "")
    private val atkid = intArrayOf(R.id.eschchld, R.id.eschchom, R.id.eschchmu)
    private val atks = arrayOf("2", "4", "3")
    private val abid = intArrayOf(R.id.eschchabwe, R.id.eschchabfr, R.id.eschchabsl, R.id.eschchabkb, R.id.eschchabwp, R.id.eschchabcu, R.id.eschchabiv, R.id.eschchabstr, R.id.eschchabsu, R.id.eschchabcd, R.id.eschchabcr,
            R.id.eschchabwv, R.id.eschchabsur, R.id.eschchabimwe, R.id.eschchabimfr, R.id.eschchabimsl, R.id.eschchabimkb, R.id.eschchabimwv, R.id.eschchabimsu, R.id.eschchabbu, R.id.eschchabrev, R.id.eschchabsb, R.id.eschchabpo)
    private val abilities = arrayOfNulls<CheckBox>(abid.size)
    private val abtool = intArrayOf(R.string.sch_abi_we, R.string.sch_abi_fr, R.string.sch_abi_sl, R.string.sch_abi_kb, R.string.sch_abi_wa, R.string.sch_abi_cu, R.string.sch_abi_iv, R.string.sch_abi_str, R.string.sch_abi_su, R.string.sch_abi_bd, R.string.sch_abi_cr,
            R.string.sch_abi_wv, R.string.sch_abi_surge, R.string.sch_abi_iw, R.string.sch_abi_if, R.string.sch_abi_is, R.string.sch_abi_ik, R.string.sch_abi_iwv, R.string.sch_abi_imsu, R.string.abi_bu, R.string.abi_rev, R.string.sch_abi_sb, R.string.sch_abi_poi)
    private val trtool = intArrayOf(R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_re, R.string.sch_wh)
    private val abils = arrayOf(intArrayOf(1, Data.P_WEAK), intArrayOf(1, Data.P_STOP), intArrayOf(1, Data.P_SLOW), intArrayOf(1, Data.P_KB), intArrayOf(1, Data.P_WARP), intArrayOf(1, Data.P_CURSE), intArrayOf(1, Data.P_IMUATK), intArrayOf(1, Data.P_STRONG), intArrayOf(1, Data.P_LETHAL), intArrayOf(0, Data.AB_BASE), intArrayOf(1, Data.P_CRIT), intArrayOf(1, Data.P_WAVE), intArrayOf(1, Data.P_VOLC), intArrayOf(1, Data.P_IMUWEAK), intArrayOf(1, Data.P_IMUSTOP), intArrayOf(1, Data.P_IMUSLOW), intArrayOf(1, Data.P_IMUKB), intArrayOf(1, Data.P_IMUWAVE), intArrayOf(1, Data.P_IMUVOLC), intArrayOf(1, Data.P_BURROW), intArrayOf(1, Data.P_REVIVE), intArrayOf(1, Data.P_SATK), intArrayOf(1, Data.P_POIATK))
    private val atkdraw = intArrayOf(212, 112)
    private val trdraw = intArrayOf(219, 220, 221, 222, 223, 224, 225, 226, 227, -1, -1, -1)
    private val abdraw = intArrayOf(195, 197, 198, 207, 266, 289, 231, 196, 199, 200, 201, 208, 239, 213, 214, 215, 216, 210, 243, -1, -1, 229, -1)
    private val abfiles = arrayOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Burrow.png", "Revive.png", "", "BCPoison.png")

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println(abdraw.size)
        println(abfiles.size)
        println(abid.size)

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

        if (!shared.getBoolean("DEV_MODE", false)) {
            AppWatcher.config = AppWatcher.config.copy(enabled = false)
            LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        } else {
            AppWatcher.config = AppWatcher.config.copy(enabled = true)
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }

        DefineItf.check(this)

        setContentView(R.layout.activity_enemy_search_filter)

        if (StaticStore.img15 == null) {
            StaticStore.readImg(this)
        }

        val tgor = findViewById<RadioButton>(R.id.eschrdtgor)
        val atkmu = findViewById<RadioButton>(R.id.eschrdatkmu)

        atkmu.compoundDrawablePadding = StaticStore.dptopx(16f, this)

        val atksi = findViewById<RadioButton>(R.id.eschrdatksi)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(211, 40f), null)
            atksi.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 40f), null)
        } else {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(211, 32f), null)
            atksi.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 32f), null)
        }

        atksi.compoundDrawablePadding = StaticStore.dptopx(16f, this)

        val atkor = findViewById<RadioButton>(R.id.eschrdatkor)
        val abor = findViewById<RadioButton>(R.id.eschrdabor)

        for (i in trid.indices) {
            traits[i] = findViewById(trid[i])
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && trdraw[i] != -1)
                traits[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(trdraw[i], 40f), null)
            else if (trdraw[i] != -1)
                traits[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(trdraw[i], 32f), null)

            if (trdraw[i] != -1)
                traits[i]?.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        }

        for (i in atkid.indices) {
            attacks[i] = findViewById(atkid[i])

            if (i < atkid.size - 1) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    attacks[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(atkdraw[i], 40f), null)
                else
                    attacks[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(atkdraw[i], 32f), null)

                attacks[i]?.compoundDrawablePadding = StaticStore.dptopx(8f, this)
            }
        }

        for (i in abid.indices) {
            abilities[i] = findViewById(abid[i])

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (abdraw[i] != -1) {
                    if (abdraw[i] == -100) {
                        abilities[i]?.setText(abtool[i])
                        continue
                    }

                    abilities[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(abdraw[i], 40f), null)
                } else {
                    val b = StaticStore.getResizeb(BitmapFactory.decodeFile(StaticStore.getExternalPath(this) + "org/page/icons/" + abfiles[i]), this, 40f)

                    abilities[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, BitmapDrawable(resources, b), null)
                }
            } else {
                if (abdraw[i] != -1) {
                    if (abdraw[i] == -100) {
                        abilities[i]?.setText(abtool[i])

                        continue
                    }

                    abilities[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(abdraw[i], 32f), null)
                } else {
                    val b = StaticStore.getResizeb(BitmapFactory.decodeFile(StaticStore.getExternalPath(this)+"org/page/icons/" + abfiles[i]), this, 32f)

                    abilities[i]?.setCompoundDrawablesWithIntrinsicBounds(null, null, BitmapDrawable(resources, b), null)
                }
            }

            abilities[i]?.compoundDrawablePadding = StaticStore.dptopx(16f, this)
        }

        tgor.isChecked = true
        atkor.isChecked = true
        abor.isChecked = true

        checker()

        listeners()
    }

    private fun getResizeDraw(id: Int, dp: Float): BitmapDrawable {
        val bd = BitmapDrawable(resources, StaticStore.getResizeb(StaticStore.img15[id].bimg() as Bitmap, this, dp))

        bd.isFilterBitmap = true
        bd.setAntiAlias(true)

        return bd
    }

    @SuppressLint("ClickableViewAccessibility")
    protected fun listeners() {
        val back = findViewById<FloatingActionButton>(R.id.eschbck)
        val reset = findViewById<FloatingActionButton>(R.id.schreset)
        val atkgroup = findViewById<RadioGroup>(R.id.eschrgatk)
        val tgor = findViewById<RadioButton>(R.id.eschrdtgor)
        val atkor = findViewById<RadioButton>(R.id.eschrdatkor)
        val abor = findViewById<RadioButton>(R.id.eschrdabor)
        val star = findViewById<CheckBox>(R.id.eschstar)

        back.setOnClickListener { returner() }

        reset.setOnClickListener {
            StaticStore.tg = ArrayList()
            StaticStore.ability = ArrayList()
            StaticStore.attack = ArrayList()
            StaticStore.tgorand = true
            StaticStore.atksimu = true
            StaticStore.aborand = true
            StaticStore.atkorand = true
            StaticStore.starred = false

            atkgroup.clearCheck()

            tgor.isChecked = true
            atkor.isChecked = true
            abor.isChecked = true
            star.isChecked = false

            for (attack1 in attacks) {
                if (attack1!!.isChecked)
                    attack1.isChecked = false
            }
            for (trait in traits) {
                if (trait!!.isChecked)
                    trait.isChecked = false
            }
            for (ability in abilities)
                if (ability!!.isChecked)
                    ability.isChecked = false
        }

        val tggroup = findViewById<RadioGroup>(R.id.eschrgtg)
        val atkgroupor = findViewById<RadioGroup>(R.id.eschrgatkor)
        val abgroup = findViewById<RadioGroup>(R.id.eschrgab)
        val atkmu = findViewById<RadioButton>(R.id.eschrdatkmu)

        star.setOnCheckedChangeListener { _, isChecked -> StaticStore.starred = isChecked }
        tggroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.tgorand = checkedId == tgor.id }
        atkgroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.atksimu = checkedId == atkmu.id }
        atkgroupor.setOnCheckedChangeListener { _, checkedId -> StaticStore.atkorand = checkedId == atkor.id }
        abgroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.aborand = checkedId == abor.id }

        for (i in traits.indices) {
            traits[i]?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    StaticStore.tg.add(colors[i])
                else
                    StaticStore.tg.remove(colors[i])
            }

            if (i < 9) traits[i]?.setOnLongClickListener { v ->
                StaticStore.showShortMessage(v.context, trtool[i])

                true
            }
        }

        for (i in attacks.indices) {
            attacks[i]?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    StaticStore.attack.add(atks[i])
                else
                    StaticStore.attack.remove(atks[i])
            }
        }

        for (i in abilities.indices) {
            abilities[i]?.setOnLongClickListener { v ->
                StaticStore.showShortMessage(v.context, abtool[i])

                true
            }

            abilities[i]?.setOnCheckedChangeListener { _, isChecked ->
                val abilval = ArrayList<Int>()

                for (j in abils[i])
                    abilval.add(j)

                if (isChecked)
                    StaticStore.ability.add(abilval)
                else
                    StaticStore.ability.remove(abilval)
            }

            star.setOnCheckedChangeListener { _, isChecked ->
                StaticStore.starred = isChecked
            }
        }
    }

    private fun returner() {
        val atkgroup = findViewById<RadioGroup>(R.id.eschrgatk)
        val result = Intent()

        StaticStore.empty = atkgroup.checkedRadioButtonId == -1

        setResult(Activity.RESULT_OK, result)

        finish()
    }

    private fun checker() {
        val star = findViewById<CheckBox>(R.id.eschstar)
        val atkgroup = findViewById<RadioGroup>(R.id.eschrgatk)
        val atkgroupor = findViewById<RadioGroup>(R.id.eschrgatkor)
        val tggroup = findViewById<RadioGroup>(R.id.eschrgtg)
        val abgroup = findViewById<RadioGroup>(R.id.eschrgab)

        star.isChecked = StaticStore.starred

        if (!StaticStore.empty)
            atkgroup.check(R.id.schrdatkmu)

        if (!StaticStore.atksimu)
            if (!StaticStore.empty)
                atkgroup.check(R.id.eschrdatksi)

        if (!StaticStore.atkorand)
            atkgroupor.check(R.id.eschrdatkand)

        if (!StaticStore.tgorand)
            tggroup.check(R.id.eschrdtgand)

        if (!StaticStore.aborand)
            abgroup.check(R.id.eschrdaband)

        for (i in atks.indices) {
            if (StaticStore.attack != null && StaticStore.attack.contains(atks[i]))
                attacks[i]?.isChecked = true
        }

        for (i in colors.indices) {
            if (StaticStore.tg != null && StaticStore.tg.contains(colors[i]))
                traits[i]?.isChecked = true
        }

        for (i in abils.indices) {
            val checker = ArrayList<Int>()

            for (k in abils[i])
                checker.add(k)

            if (StaticStore.ability != null && StaticStore.ability.contains(checker))
                abilities[i]?.isChecked = true
        }
    }

    override fun onBackPressed() {
        val back = findViewById<FloatingActionButton>(R.id.eschbck)

        back.performClick()
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
    }
}