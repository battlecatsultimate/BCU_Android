package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.adapter.SearchAbilityAdapter
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.adapter.SearchTraitAdapter
import common.CommonStatic
import common.pack.Identifier
import common.pack.UserProfile
import common.util.Data
import common.util.unit.Trait
import java.util.*
import kotlin.collections.ArrayList

open class EnemySearchFilter : AppCompatActivity() {
    private val attacks = arrayOfNulls<CheckBox>(3)
    private val atkid = intArrayOf(R.id.eschchld, R.id.eschchom, R.id.eschchmu)
    private val atks = arrayOf("2", "4", "3")
    private val abtool = intArrayOf(R.string.sch_abi_we, R.string.sch_abi_fr, R.string.sch_abi_sl, R.string.sch_abi_kb, R.string.sch_abi_wa, R.string.sch_abi_cu, R.string.sch_abi_iv, R.string.sch_abi_str, R.string.sch_abi_su, R.string.sch_abi_bd, R.string.sch_abi_cr, R.string.sch_abi_ck, R.string.sch_abi_bb, R.string.sch_abi_shb,
            R.string.sch_abi_mw, R.string.sch_abi_wv, R.string.sch_abi_ms, R.string.sch_abi_surge, R.string.sch_abi_iw, R.string.sch_abi_if, R.string.sch_abi_is, R.string.sch_abi_ik, R.string.sch_abi_iwv, R.string.sch_abi_imsu, R.string.sch_abi_impoi, R.string.abi_bu, R.string.abi_rev, R.string.sch_abi_sb, R.string.sch_abi_poi, R.string.enem_info_barrier, R.string.sch_abi_ds, R.string.sch_abi_cs, R.string.sch_abi_sd, R.string.abi_sui,
            R.string.abi_gh, R.string.abi_snk, R.string.abi_seal, R.string.abi_stt, R.string.abi_sum, R.string.abi_mvatk, R.string.abi_thch, R.string.abi_poi, R.string.abi_boswv, R.string.abi_armbr, R.string.abi_hast, R.string.sch_abi_cou, R.string.sch_abi_cap, R.string.sch_abi_cut,
            R.string.abi_imvatk, R.string.abi_isnk, R.string.abi_istt, R.string.abi_ipoi, R.string.abi_ithch, R.string.abi_iseal, R.string.abi_iboswv, R.string.abi_imcri, R.string.sch_abi_imusm, R.string.sch_abi_imar, R.string.sch_abi_imsp, R.string.sch_abi_imcn)
    private val trToolID = intArrayOf(R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_de, R.string.sch_re, R.string.sch_wh, R.string.esch_eva, R.string.esch_witch, R.string.sch_bar, R.string.sch_bst, R.string.sch_ba)

    private val abils = arrayOf(intArrayOf(1, Data.P_WEAK), intArrayOf(1, Data.P_STOP),
        intArrayOf(1, Data.P_SLOW), intArrayOf(1, Data.P_KB), intArrayOf(1, Data.P_WARP),
        intArrayOf(1, Data.P_CURSE), intArrayOf(1, Data.P_IMUATK), intArrayOf(1, Data.P_STRONG),
        intArrayOf(1, Data.P_LETHAL), intArrayOf(1, Data.P_ATKBASE), intArrayOf(1, Data.P_CRIT),
        intArrayOf(0, Data.AB_CKILL), intArrayOf(1, Data.P_BREAK), intArrayOf(1, Data.P_SHIELDBREAK),
        intArrayOf(1, Data.P_MINIWAVE), intArrayOf(1, Data.P_WAVE), intArrayOf(1, Data.P_MINIVOLC),
        intArrayOf(1, Data.P_VOLC), intArrayOf(1, Data.P_IMUWEAK), intArrayOf(1, Data.P_IMUSTOP),
        intArrayOf(1, Data.P_IMUSLOW), intArrayOf(1, Data.P_IMUKB), intArrayOf(1, Data.P_IMUWAVE),
        intArrayOf(1, Data.P_IMUVOLC), intArrayOf(1, Data.P_IMUPOIATK),intArrayOf(1, Data.P_BURROW),
        intArrayOf(1, Data.P_REVIVE), intArrayOf(1, Data.P_SATK), intArrayOf(1, Data.P_POIATK),
        intArrayOf(1, Data.P_BARRIER), intArrayOf(1, Data.P_DEMONSHIELD), intArrayOf(0, Data.AB_CSUR),
        intArrayOf(1, Data.P_DEATHSURGE), intArrayOf(0, Data.AB_GLASS), intArrayOf(0, Data.AB_GHOST),
        intArrayOf(0, Data.P_SNIPER), intArrayOf(1, Data.P_SEAL), intArrayOf(1, Data.P_TIME),
        intArrayOf(1, Data.P_SUMMON), intArrayOf(1, Data.P_MOVEWAVE), intArrayOf(1, Data.P_THEME),
        intArrayOf(1, Data.P_POISON), intArrayOf(1, Data.P_BOSS), intArrayOf(1, Data.P_ARMOR),
        intArrayOf(1, Data.P_SPEED), intArrayOf(1, Data.P_COUNTER), intArrayOf(1, Data.P_DMGCAP),
        intArrayOf(1, Data.P_DMGCUT), intArrayOf(1, Data.P_IMUMOVING), intArrayOf(0, Data.AB_SNIPERI),
        intArrayOf(0, Data.AB_TIMEI), intArrayOf(1, Data.P_IMUPOI), intArrayOf(0, Data.AB_THEMEI),
        intArrayOf(1, Data.P_IMUSEAL), intArrayOf(0, Data.AB_IMUSW), intArrayOf(1, Data.P_CRITI),
        intArrayOf(1, Data.P_IMUSUMMON), intArrayOf(1, Data.P_IMUARMOR),
        intArrayOf(1, Data.P_IMUSPEED), intArrayOf(1, Data.P_IMUCANNON))

    private val atkdraw = intArrayOf(212, 112)

    private val abdraw = intArrayOf(195, 197, 198, 207, 266, 289, 231, 196, 199, 200, 201, 300, 264, 296, 293, 208, 310, 239, 213, 214, 215, 216, 210, 243, 237, -1, -1, 229, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)
    private val abfiles = arrayOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Burrow", "Revive", "", "BCPoison", "Barrier", "DemonShield", "CounterSurge", "DeathSurge", "Suicide", "Ghost", "Snipe", "Seal", "Time", "Summon", "Moving", "Theme", "Poison", "BossWave", "ArmorBreak", "Speed", "Counter", "DmgCap", "DmgCut", "MovingX", "SnipeX", "TimeX", "PoisonX", "ThemeX", "SealX", "BossWaveX", "CritX", "SummonX", "ArmorBreakX", "SpeedX", "CannonX")

    private lateinit var abAdapter: SearchAbilityAdapter
    private lateinit var trAdapter: SearchTraitAdapter

    @SuppressLint("SourceLockedOrientationActivity")
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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_enemy_search_filter)

        if (StaticStore.img15 == null) {
            StaticStore.readImg()
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

        val abrec = findViewById<RecyclerView>(R.id.eschchabrec)
        val trrec = findViewById<RecyclerView>(R.id.eschchtgrec)

        abAdapter = SearchAbilityAdapter(this, abtool, abils, abdraw, abfiles)
        abAdapter.setHasStableIds(true)

        trAdapter = SearchTraitAdapter(this, generateTraitToolTip(), generateTraitArray())
        trAdapter.setHasStableIds(true)

        abrec.layoutManager = LinearLayoutManager(this)
        abrec.adapter = abAdapter
        abrec.isNestedScrollingEnabled = false

        trrec.layoutManager = LinearLayoutManager(this)
        trrec.adapter = trAdapter
        trrec.isNestedScrollingEnabled = false

        tgor.isChecked = true
        atkor.isChecked = true
        abor.isChecked = true

        checker()

        listeners()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val back = findViewById<FloatingActionButton>(R.id.eschbck)

                back.performClick()
            }
        })
    }

    private fun getResizeDraw(id: Int, dp: Float): BitmapDrawable {
        val icon = StaticStore.img15?.get(id)?.bimg() ?: StaticStore.empty(this, dp, dp)
        val bd = BitmapDrawable(resources, StaticStore.getResizeb(icon as Bitmap, this, dp))

        bd.isFilterBitmap = true
        bd.setAntiAlias(true)

        return bd
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    protected fun listeners() {
        val back = findViewById<FloatingActionButton>(R.id.eschbck)
        val reset = findViewById<FloatingActionButton>(R.id.schreset)
        val atkgroup = findViewById<RadioGroup>(R.id.eschrgatk)
        val tgor = findViewById<RadioButton>(R.id.eschrdtgor)
        val atkor = findViewById<RadioButton>(R.id.eschrdatkor)
        val abor = findViewById<RadioButton>(R.id.eschrdabor)
        val star = findViewById<CheckBox>(R.id.eschstar)
        val stat = findViewById<FloatingActionButton>(R.id.eschstat)

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

            trAdapter.updateList()
            trAdapter.notifyDataSetChanged()
            abAdapter.updateList()
            abAdapter.notifyDataSetChanged()
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

        for (i in attacks.indices) {
            attacks[i]?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    StaticStore.attack.add(atks[i])
                else
                    StaticStore.attack.remove(atks[i])
            }
        }

        star.setOnCheckedChangeListener { _, isChecked ->
            StaticStore.starred = isChecked
        }

        stat.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@EnemySearchFilter, StatSearchFilter::class.java)

                intent.putExtra("unit", false)

                startActivity(intent)
            }

        })
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
            if (StaticStore.attack.contains(atks[i]))
                attacks[i]?.isChecked = true
        }
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
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    private fun generateTraitArray() : Array<Identifier<Trait>> {
        val traits = ArrayList<Identifier<Trait>>()

        for(i in 0 until 15) {
            traits.add(UserProfile.getBCData().traits.list[i].id)
        }

        for(userPack in UserProfile.getUserPacks()) {
            for(tr in userPack.traits.list) {
                tr ?: continue

                traits.add(tr.id)
            }
        }

        return traits.toTypedArray()
    }

    private fun generateTraitToolTip() : Array<String> {
        val tool = ArrayList<String>()

        for(i in trToolID.indices) {
            tool.add(getText(trToolID[i]).toString())
        }

        for(userPack in UserProfile.getUserPacks()) {
            for(tr in userPack.traits.list) {
                tr ?: continue

                tool.add(tr.name)
            }
        }

        return tool.toTypedArray()
    }
}