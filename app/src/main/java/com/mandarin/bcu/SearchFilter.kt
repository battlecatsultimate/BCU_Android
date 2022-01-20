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

class SearchFilter : AppCompatActivity() {
    private val rareid = intArrayOf(R.id.schchba, R.id.schchex, R.id.schchr, R.id.schchsr, R.id.schchur, R.id.schchlr)
    private val rarity = arrayOf("0", "1", "2", "3", "4", "5")
    private val atkid = intArrayOf(R.id.schchld, R.id.schchom, R.id.schchmu)
    private val atks = arrayOf("2", "4", "3")
    private val abtool = intArrayOf(R.string.sch_abi_we, R.string.sch_abi_fr, R.string.sch_abi_sl, R.string.sch_abi_ao, R.string.sch_abi_st, R.string.sch_abi_re, R.string.sch_abi_it, R.string.sch_abi_md, R.string.sch_abi_id, R.string.sch_abi_kb,
            R.string.sch_abi_wa, R.string.sch_abi_cu, R.string.sch_abi_iv, R.string.sch_abi_str, R.string.sch_abi_su, R.string.sch_abi_bd, R.string.sch_abi_cr, R.string.sch_abi_zk, R.string.sch_abi_bb, R.string.sch_abi_shb, R.string.sch_abi_sb, R.string.sch_abi_em, R.string.sch_abi_me,
            R.string.sch_abi_mw, R.string.sch_abi_wv, R.string.sch_abi_surge, R.string.sch_abi_ws, R.string.sch_abi_bk, R.string.sch_abi_iw, R.string.sch_abi_if, R.string.sch_abi_is, R.string.sch_abi_ik, R.string.sch_abi_iwv, R.string.sch_abi_imsu, R.string.sch_abi_iwa, R.string.sch_abi_ic, R.string.sch_abi_impoi, R.string.sch_abi_wk, R.string.sch_abi_eva, R.string.sch_abi_poi, R.string.enem_info_barrier, R.string.sch_abi_ds, R.string.sch_abi_sd, R.string.abi_sui,
            R.string.abi_bu, R.string.abi_rev, R.string.abi_gh, R.string.abi_snk, R.string.abi_seal, R.string.abi_stt, R.string.abi_sum, R.string.abi_mvatk, R.string.abi_thch, R.string.abi_poi, R.string.abi_boswv, R.string.abi_armbr, R.string.abi_hast, R.string.sch_abi_cou, R.string.sch_abi_cap, R.string.sch_abi_cut,
            R.string.abi_imvatk, R.string.abi_isnk, R.string.abi_istt, R.string.abi_ipoi, R.string.abi_ithch, R.string.abi_iseal, R.string.abi_iboswv, R.string.abi_imcri, R.string.sch_abi_imusm, R.string.sch_abi_imar, R.string.sch_abi_imsp)
    private val tgToolID = intArrayOf(R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_de, R.string.sch_re, R.string.sch_wh)
    private val abils = arrayOf(intArrayOf(1, Data.P_WEAK), intArrayOf(1, Data.P_STOP), intArrayOf(1, Data.P_SLOW),
            intArrayOf(0, Data.AB_ONLY), intArrayOf(0, Data.AB_GOOD), intArrayOf(0, Data.AB_RESIST),
            intArrayOf(0, Data.AB_RESISTS), intArrayOf(0, Data.AB_MASSIVE), intArrayOf(0, Data.AB_MASSIVES),
            intArrayOf(1, Data.P_KB), intArrayOf(1, Data.P_WARP), intArrayOf(1, Data.P_CURSE), intArrayOf(1, Data.P_IMUATK), intArrayOf(1, Data.P_STRONG),
            intArrayOf(1, Data.P_LETHAL), intArrayOf(0, Data.AB_BASE), intArrayOf(1, Data.P_CRIT),
            intArrayOf(0, Data.AB_ZKILL), intArrayOf(1, Data.P_BREAK), intArrayOf(1, Data.P_SHIELDBREAK), intArrayOf(1, Data.P_SATK),
            intArrayOf(0, Data.AB_EARN), intArrayOf(0, Data.AB_METALIC), intArrayOf(1, Data.P_MINIWAVE), intArrayOf(1, Data.P_WAVE),
            intArrayOf(1, Data.P_VOLC), intArrayOf(0, Data.AB_WAVES), intArrayOf(0, Data.AB_BAKILL), intArrayOf(1, Data.P_IMUWEAK), intArrayOf(1, Data.P_IMUSTOP),
            intArrayOf(1, Data.P_IMUSLOW), intArrayOf(1, Data.P_IMUKB), intArrayOf(1, Data.P_IMUWAVE), intArrayOf(1, Data.P_IMUVOLC),
            intArrayOf(1, Data.P_IMUWARP), intArrayOf(1, Data.P_IMUCURSE), intArrayOf(1, Data.P_IMUPOIATK),
            intArrayOf(0, Data.AB_WKILL), intArrayOf(0, Data.AB_EKILL), intArrayOf(1, Data.P_POIATK), intArrayOf(1, Data.P_BARRIER), intArrayOf(1, Data.P_DEMONSHIELD), intArrayOf(1, Data.P_DEATHSURGE),
            intArrayOf(0, Data.AB_GLASS), intArrayOf(1, Data.P_BURROW), intArrayOf(1, Data.P_REVIVE), intArrayOf(0, Data.AB_GHOST),
            intArrayOf(0, Data.P_SNIPER), intArrayOf(1, Data.P_SEAL), intArrayOf(1, Data.P_TIME),
            intArrayOf(1, Data.P_SUMMON), intArrayOf(1, Data.P_MOVEWAVE), intArrayOf(1, Data.P_THEME),
            intArrayOf(1, Data.P_POISON), intArrayOf(1, Data.P_BOSS), intArrayOf(1, Data.P_ARMOR), intArrayOf(1, Data.P_SPEED), intArrayOf(1, Data.P_COUNTER), intArrayOf(1, Data.P_DMGCAP), intArrayOf(1, Data.P_DMGCUT), intArrayOf(1, Data.P_IMUMOVING),
            intArrayOf(0, Data.AB_SNIPERI), intArrayOf(0, Data.AB_TIMEI), intArrayOf(1, Data.P_IMUPOI),
            intArrayOf(0, Data.AB_THEMEI), intArrayOf(1, Data.P_IMUSEAL), intArrayOf(0, Data.AB_IMUSW),
            intArrayOf(1, Data.P_CRITI), intArrayOf(1, Data.P_IMUSUMMON), intArrayOf(1, Data.P_IMUARMOR), intArrayOf(1, Data.P_IMUSPEED))
    private val rarities = arrayOfNulls<CheckBox>(rareid.size)
    private val attacks = arrayOfNulls<CheckBox>(atkid.size)
    private val atkdraw = intArrayOf(212, 112)
    private val abdraw = intArrayOf(195, 197, 198, 202, 203, 204, 122, 206, 114, 207, 266, 289, 231, 196, 199, 200, 201, 260, 264, 296, 229, 205, 209, 293, 208, 239, 218, 297, 213, 214, 215, 216, 210, 243, 262, 116, 237, 258, 110, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)
    private val abdrawf = arrayOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "BCPoison", "Barrier", "DemonShield", "DeathSurge", "Suicide", "Burrow", "Revive", "Ghost", "Snipe", "Seal", "Time", "Summon", "Moving", "Theme", "Poison", "BossWave", "ArmorBreak", "Speed", "Counter", "DmgCap", "DmgCut", "MovingX", "SnipeX", "TimeX", "PoisonX", "ThemeX", "SealX", "BossWaveX", "CritX", "SummonX", "ArmorBreakX", "SpeedX")
    private lateinit var abilAdapter: SearchAbilityAdapter
    private lateinit var traitAdapter: SearchTraitAdapter

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (StaticStore.img15 == null)
            StaticStore.readImg()

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

        for (i in rareid.indices)
            rarities[i] = findViewById(rareid[i])

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

        val abrec = findViewById<RecyclerView>(R.id.schchabrec)
        val tgrec = findViewById<RecyclerView>(R.id.schchtgrec)

        abrec.isNestedScrollingEnabled = false

        abilAdapter = SearchAbilityAdapter(this, abtool, abils, abdraw, abdrawf)
        abilAdapter.setHasStableIds(true)

        traitAdapter = SearchTraitAdapter(this, generateTraitToolTip(), generateTraitArray())
        traitAdapter.setHasStableIds(true)

        abrec.layoutManager = LinearLayoutManager(this)
        abrec.adapter = abilAdapter

        tgrec.layoutManager = LinearLayoutManager(this)
        tgrec.adapter = traitAdapter

        tgor.isChecked = true
        atkor.isChecked = true
        abor.isChecked = true

        checker()

        listeners()
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
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
        val stat = findViewById<FloatingActionButton>(R.id.eschstat)

        back.setOnClickListener { returner() }

        reset.setOnClickListener {
            StaticStore.filterReset()
            atkgroup!!.clearCheck()
            tgor!!.isChecked = true
            atkor!!.isChecked = true
            abor!!.isChecked = true
            chnp!!.isChecked = false

            for (rarity1 in rarities) {
                if (rarity1!!.isChecked)
                    rarity1.isChecked = false
            }

            for (attack1 in attacks) {
                if (attack1!!.isChecked)
                    attack1.isChecked = false
            }

            traitAdapter.updateList()
            traitAdapter.notifyDataSetChanged()

            abilAdapter.updateList()
            abilAdapter.notifyDataSetChanged()
        }

        tggroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.tgorand = checkedId == tgor!!.id }

        atkgroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.atksimu = checkedId == atkmu!!.id }

        atkgroupor.setOnCheckedChangeListener { _, checkedId -> StaticStore.atkorand = checkedId == atkor!!.id }

        abgroup.setOnCheckedChangeListener { _, checkedId -> StaticStore.aborand = checkedId == abor!!.id }

        for (i in rarities.indices) {
            rarities[i]!!.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    StaticStore.rare.add(rarity[i])
                else
                    StaticStore.rare.remove(rarity[i])
            }
        }

        for (i in attacks.indices) {
            attacks[i]!!.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    StaticStore.attack.add(atks[i])
                else
                    StaticStore.attack.remove(atks[i])
            }
        }

        chnp.setOnCheckedChangeListener { _, isChecked ->
            StaticStore.talents = isChecked
        }

        stat.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(this@SearchFilter, StatSearchFilter::class.java)
                intent.putExtra("unit", true)

                startActivity(intent)
            }
        })
    }

    private fun returner() {
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val result = Intent()

        StaticStore.empty = atkgroup!!.checkedRadioButtonId == -1

        setResult(Activity.RESULT_OK, result)

        finish()
    }

    private fun checker() {
        val atkgroup = findViewById<RadioGroup>(R.id.schrgatk)
        val atkgroupor = findViewById<RadioGroup>(R.id.schrgatkor)
        val tggroup = findViewById<RadioGroup>(R.id.schrgtg)
        val abgroup = findViewById<RadioGroup>(R.id.schrgab)
        val chnp = findViewById<CheckBox>(R.id.schnp)

        if (!StaticStore.empty)
            atkgroup.check(R.id.schrdatkmu)

        if (!StaticStore.atksimu && !StaticStore.empty)
            atkgroup.check(R.id.schrdatksi)

        if (!StaticStore.atkorand)
            atkgroupor.check(R.id.schrdatkand)

        if (!StaticStore.tgorand)
            tggroup.check(R.id.schrdtgand)

        if (!StaticStore.aborand)
            abgroup.check(R.id.schrdaband)

        for (i in rarity.indices)
            if (StaticStore.rare.contains(rarity[i]))
                rarities[i]?.isChecked = true

        for (i in atks.indices)
            if (StaticStore.attack.contains(atks[i]))
                attacks[i]?.isChecked = true

        if (StaticStore.talents)
            chnp?.isChecked = true
    }

    private fun getResizeDraw(id: Int, dp: Float): BitmapDrawable {
        val icon = StaticStore.img15?.get(id)?.bimg() ?: StaticStore.empty(this, dp, dp)
        val bd = BitmapDrawable(resources, StaticStore.getResizeb(icon as Bitmap, this, dp))

        bd.isFilterBitmap = true
        bd.setAntiAlias(true)

        return bd
    }

    override fun onBackPressed() {
        val back = findViewById<FloatingActionButton>(R.id.schbck)

        back.performClick()
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

    private fun generateTraitArray() : Array<Identifier<Trait>> {
        val traits = ArrayList<Identifier<Trait>>()

        for(i in 0 until 10) {
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

        for(i in tgToolID) {
            tool.add(getText(i).toString())
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