package com.mandarin.bcu.androidutil.unit.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import com.mandarin.bcu.androidutil.supports.ScaleAnimator
import com.mandarin.bcu.androidutil.supports.adapter.AdapterAbil
import com.mandarin.bcu.util.Interpret
import common.CommonStatic
import common.battle.BasisSet
import common.battle.Treasure
import common.battle.data.MaskUnit
import common.pack.Identifier
import common.util.unit.Form
import common.util.unit.Level
import common.util.unit.Unit

class UnitinfRecycle(private val context: Activity,
                     private val names: ArrayList<String>, private val forms: Array<Form>,
                     private val data: Identifier<Unit>
) : RecyclerView.Adapter<UnitinfRecycle.ViewHolder>() {
    private var fs = 0
    private val s: GetStrings = GetStrings(this.context)
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private val color: IntArray = intArrayOf(
            StaticStore.getAttributeColor(context, R.attr.TextPrimary)
    )
    
    private var talents = false
    private val level = Level(8)

    private var isRaw = false
    private val talentIndex = java.util.ArrayList<Int>()
    private val superTalentIndex = java.util.ArrayList<Int>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pack: Button = itemView.findViewById(R.id.unitinfpack)
        val unitpack: TextView = itemView.findViewById(R.id.unitinfpackr)
        var frse: Button = itemView.findViewById(R.id.unitinffrse)
        var unitname: TextView = itemView.findViewById(R.id.unitinfname)
        var unitid: TextView = itemView.findViewById(R.id.unitinfidr)
        var unithp: TextView = itemView.findViewById(R.id.unitinfhpr)
        var unithb: TextView = itemView.findViewById(R.id.unitinfhbr)
        var unitlevel: Spinner = itemView.findViewById(R.id.unitinflevr)
        var unitlevelp: Spinner = itemView.findViewById(R.id.unitinflevpr)
        var unitplus: TextView = itemView.findViewById(R.id.unitinfplus)
        var uniticon: ImageView = itemView.findViewById(R.id.unitinficon)
        var unitatkb: Button = itemView.findViewById(R.id.unitinfatk)
        var unitatk: TextView = itemView.findViewById(R.id.unitinfatkr)
        var unittrait: TextView = itemView.findViewById(R.id.unitinftraitr)
        var unitcost: TextView = itemView.findViewById(R.id.unitinfcostr)
        var unitsimu: TextView = itemView.findViewById(R.id.unitinfsimur)
        var unitspd: TextView = itemView.findViewById(R.id.unitinfspdr)
        var unitcdb: Button = itemView.findViewById(R.id.unitinfcd)
        var unitcd: TextView = itemView.findViewById(R.id.unitinfcdr)
        var unitrang: TextView = itemView.findViewById(R.id.unitinfrangr)
        var unitpreatkb: Button = itemView.findViewById(R.id.unitinfpreatk)
        var unitpreatk: TextView = itemView.findViewById(R.id.unitinfpreatkr)
        var unitpostb: Button = itemView.findViewById(R.id.unitinfpost)
        var unitpost: TextView = itemView.findViewById(R.id.unitinfpostr)
        var unittbab: Button = itemView.findViewById(R.id.unitinftba)
        var unittba: TextView = itemView.findViewById(R.id.unitinftbar)
        var unitatktb: Button = itemView.findViewById(R.id.unitinfatktime)
        var unitatkt: TextView = itemView.findViewById(R.id.unitinfatktimer)
        var unitabilt: TextView = itemView.findViewById(R.id.unitinfabiltr)
        var none: TextView = itemView.findViewById(R.id.unitabilnone)
        var unitabil: RecyclerView = itemView.findViewById(R.id.unitinfabilr)
        var unittalen: CheckBox = itemView.findViewById(R.id.unitinftalen)
        var npresetrow: TableRow = itemView.findViewById(R.id.talresetrow)
        var npreset: Button = itemView.findViewById(R.id.unitinftalreset)
        var nprow: TableRow = itemView.findViewById(R.id.talenrow)
        var supernprow: TableRow = itemView.findViewById(R.id.supertalenrow)

        init {
            unitplus.text = " + "
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(context).inflate(R.layout.unit_table, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val u = data.get() ?: return

        val cdlev: TextInputLayout = context.findViewById(R.id.cdlev)
        val cdtrea: TextInputLayout = context.findViewById(R.id.cdtrea)
        val atktrea: TextInputLayout = context.findViewById(R.id.atktrea)
        val healtrea: TextInputLayout = context.findViewById(R.id.healtrea)

        cdlev.isCounterEnabled = true
        cdlev.counterMaxLength = 2
        cdtrea.isCounterEnabled = true
        cdtrea.counterMaxLength = 3
        atktrea.isCounterEnabled = true
        atktrea.counterMaxLength = 3
        healtrea.isCounterEnabled = true
        healtrea.counterMaxLength = 3
        
        cdlev.setHelperTextColor(ColorStateList(states, color))
        cdtrea.setHelperTextColor(ColorStateList(states, color))
        atktrea.setHelperTextColor(ColorStateList(states, color))
        healtrea.setHelperTextColor(ColorStateList(states, color))

        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        
        if (shared.getBoolean("frame", true)) {
            fs = 0
            viewHolder.frse.text = context.getString(R.string.unit_info_fr)
        } else {
            fs = 1
            viewHolder.frse.text = context.getString(R.string.unit_info_sec)
        }

        val t = BasisSet.current().t()
        
        val f = forms[viewHolder.adapterPosition]

        level.setLevel(f.unit.preferredLevel)
        level.setPlusLevel(f.unit.preferredPlusLevel)

        val ability = Interpret.getAbi(f.du, fragment, StaticStore.addition, 0)
        val abilityicon = Interpret.getAbiid(f.du)
        val cdlevt: TextInputEditText = context.findViewById(R.id.cdlevt)
        val cdtreat: TextInputEditText = context.findViewById(R.id.cdtreat)
        val atktreat: TextInputEditText = context.findViewById(R.id.atktreat)
        val healtreat: TextInputEditText = context.findViewById(R.id.healtreat)

        cdlevt.setText(t.tech[0].toString())
        cdtreat.setText(t.trea[2].toString())
        atktreat.setText(t.trea[0].toString())
        healtreat.setText(t.trea[1].toString())

        val proc = Interpret.getProc(f.du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

        val icon = f.anim?.uni?.img?.bimg()

        if(icon == null) {
            viewHolder.uniticon.setImageBitmap(StaticStore.makeIcon(context, null, 48f))
        } else {
            viewHolder.uniticon.setImageBitmap(StaticStore.makeIcon(context, icon as Bitmap, 48f))
        }

        viewHolder.unitname.text = names[position]
        viewHolder.unitpack.text = s.getPackName(f.unit.id, isRaw)
        viewHolder.unitid.text = s.getID(viewHolder, StaticStore.trio(u.id.id))
        viewHolder.unithp.text = s.getHP(f, t, false, level)
        viewHolder.unithb.text = s.getHB(f, false, level)
        viewHolder.unitatk.text = s.getTotAtk(f, t, false, level)
        viewHolder.unittrait.text = s.getTrait(f, false, level)
        viewHolder.unitcost.text = s.getCost(f, false, level)
        viewHolder.unitsimu.text = s.getSimu(f)
        viewHolder.unitspd.text = s.getSpd(f, false, level)
        viewHolder.unitcd.text = s.getCD(f, t, fs, false, level)
        viewHolder.unitrang.text = s.getRange(f)
        viewHolder.unitpreatk.text = s.getPre(f, fs)
        viewHolder.unitpost.text = s.getPost(f, fs)
        viewHolder.unittba.text = s.getTBA(f, false, fs, level)
        viewHolder.unitatkt.text = s.getAtkTime(f, false, fs, level)
        viewHolder.unitabilt.text = s.getAbilT(f)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            viewHolder.unitabil.layoutManager = linearLayoutManager
            val adapterAbil = AdapterAbil(ability, proc, abilityicon, context)
            viewHolder.unitabil.adapter = adapterAbil
            ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false)
        } else {
            viewHolder.unitabil.visibility = View.GONE
        }

        if (f.du.pCoin == null) {
            viewHolder.unittalen.visibility = View.GONE
            viewHolder.npreset.visibility = View.GONE
            viewHolder.nprow.visibility = View.GONE
            viewHolder.supernprow.visibility = View.GONE

            for(i in level.talents.indices)
                level.talents[i] = 0

            listeners(viewHolder, arrayOf(), arrayOf())
        } else {
            for(i in f.du.pCoin.info.indices) {
                if(f.du.pCoin.info[i][13] == 1)
                    superTalentIndex.add(i)
                else
                    talentIndex.add(i)
            }

            val talent = Array(talentIndex.size) {
                val spin = Spinner(context)

                val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (talentIndex.size)).toFloat())

                spin.layoutParams = param
                spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                viewHolder.nprow.addView(spin)

                spin
            }

            val superTalent = Array(superTalentIndex.size) {
                val spin = Spinner(context)

                val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (superTalentIndex.size)).toFloat())

                spin.layoutParams = param
                spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                viewHolder.supernprow.addView(spin)

                spin
            }

            val max = f.du.pCoin.max

            for(i in max.indices)
                level.talents[i] = max[i]

            for(i in talent.indices) {
                if(talentIndex[i] >= f.du.pCoin.info.size) {
                    talent[i].isEnabled = false
                    continue
                }

                val talentLevels = java.util.ArrayList<Int>()

                for(j in 0 until max[talentIndex[i]] + 1)
                    talentLevels.add(j)

                val adapter = ArrayAdapter(context, R.layout.spinneradapter, talentLevels)

                talent[i].adapter = adapter
                talent[i].setSelection(getIndex(talent[i], max[talentIndex[i]]))

                level.talents[talentIndex[i]] = max[talentIndex[i]]
            }

            for(i in superTalent.indices) {
                if(superTalentIndex[i] >= f.du.pCoin.info.size) {
                    superTalent[i].isEnabled = false
                    continue
                }

                val superTalentLevels = java.util.ArrayList<Int>()

                for(j in 0 until max[superTalentIndex[i]] + 1)
                    superTalentLevels.add(j)

                val adapter = ArrayAdapter(context, R.layout.spinneradapter, superTalentLevels)

                superTalent[i].adapter = adapter
                superTalent[i].setSelection(getIndex(superTalent[i], max[superTalentIndex[i]]))

                level.talents[superTalentIndex[i]] = max[superTalentIndex[i]]

                if(CommonStatic.getConfig().realLevel)
                    changeSpinner(superTalent[i], level.lv + level.plusLv >= 60)
            }

            if(superTalent.isEmpty())
                viewHolder.supernprow.visibility = View.GONE

            listeners(viewHolder, talent, superTalent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners(viewHolder: ViewHolder, talent: Array<Spinner>, superTalent: Array<Spinner>) {
        val cdlev: TextInputLayout = context.findViewById(R.id.cdlev)
        val cdtrea: TextInputLayout = context.findViewById(R.id.cdtrea)
        val atktrea: TextInputLayout = context.findViewById(R.id.atktrea)
        val healtrea: TextInputLayout = context.findViewById(R.id.healtrea)
        val cdlevt: TextInputEditText = context.findViewById(R.id.cdlevt)
        val cdtreat: TextInputEditText = context.findViewById(R.id.cdtreat)
        val atktreat: TextInputEditText = context.findViewById(R.id.atktreat)
        val healtreat: TextInputEditText = context.findViewById(R.id.healtreat)
        val reset = context.findViewById<Button>(R.id.treasurereset)

        val t = BasisSet.current().t()
        
        val f = forms[viewHolder.adapterPosition]

        val levels: MutableList<Int> = ArrayList()

        for (j in 1 until f.unit.max + 1)
            levels.add(j)

        val levelsp = ArrayList<Int>()

        for (j in 0 until f.unit.maxp + 1)
            levelsp.add(j)

        val arrayAdapter = ArrayAdapter(context, R.layout.spinneradapter, levels)
        val arrayAdapterp = ArrayAdapter(context, R.layout.spinneradapter, levelsp)

        viewHolder.unitname.setOnLongClickListener {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText(null, viewHolder.unitname.text)
            clipboardManager.setPrimaryClip(data)
            StaticStore.showShortMessage(context, R.string.unit_info_copied)
            true
        }

        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        level.setLevel(
            when {
                shared.getInt("default_level", 50) > f.unit.max -> f.unit.max
                f.unit.rarity != 0 -> shared.getInt("default_level", 50)
                else -> f.unit.max
            }
        )

        level.setPlusLevel(f.unit.preferredPlusLevel)

        viewHolder.unitlevel.adapter = arrayAdapter
        viewHolder.unitlevel.setSelection(getIndex(viewHolder.unitlevel, level.lv))
        
        viewHolder.unitlevelp.adapter = arrayAdapterp
        viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp, level.plusLv))

        if (levelsp.size == 1) {
            viewHolder.unitlevelp.visibility = View.GONE
            viewHolder.unitplus.visibility = View.GONE
        }

        viewHolder.pack.setOnClickListener {
            isRaw = !isRaw

            viewHolder.unitpack.text = s.getPackName(f.unit.id, isRaw)
        }

        viewHolder.frse.setOnClickListener {
            if (fs == 0) {
                fs = 1

                viewHolder.unitcd.text = s.getCD(f, t, fs, talents, level)
                viewHolder.unitpreatk.text = s.getPre(f, fs)
                viewHolder.unitpost.text = s.getPost(f, fs)
                viewHolder.unittba.text = s.getTBA(f, talents, fs, level)
                viewHolder.unitatkt.text = s.getAtkTime(f, talents, fs, level)
                viewHolder.frse.text = context.getString(R.string.unit_info_sec)

                if (viewHolder.unitabil.visibility != View.GONE) {
                    var du = f.du

                    if (f.du.pCoin != null)
                        du = if (talents)
                            f.du.pCoin.improve(level.talents)
                        else
                            f.du

                    val ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0)

                    val abilityicon = Interpret.getAbiid(du)

                    val proc = Interpret.getProc(du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

                    val linearLayoutManager = LinearLayoutManager(context)

                    linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

                    viewHolder.unitabil.layoutManager = linearLayoutManager

                    val adapterAbil = AdapterAbil(ability, proc, abilityicon, context)

                    viewHolder.unitabil.adapter = adapterAbil

                    ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false)
                }
            } else {
                fs = 0

                viewHolder.unitcd.text = s.getCD(f, t, fs, talents, level)
                viewHolder.unitpreatk.text = s.getPre(f, fs)
                viewHolder.unitpost.text = s.getPost(f, fs)
                viewHolder.unittba.text = s.getTBA(f, talents, fs, level)
                viewHolder.unitatkt.text = s.getAtkTime(f, talents, fs, level)
                viewHolder.frse.text = context.getString(R.string.unit_info_fr)

                if (viewHolder.unitabil.visibility != View.GONE) {
                    var du = f.du

                    if (f.du.pCoin != null)
                        du = if (talents)
                            f.du.pCoin.improve(level.talents)
                        else
                            f.du

                    val ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0)

                    val abilityicon = Interpret.getAbiid(du)

                    val proc = Interpret.getProc(du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

                    val linearLayoutManager = LinearLayoutManager(context)

                    linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

                    viewHolder.unitabil.layoutManager = linearLayoutManager

                    val adapterAbil = AdapterAbil(ability, proc, abilityicon, context)

                    viewHolder.unitabil.adapter = adapterAbil

                    ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false)
                }
            }
        }

        viewHolder.unitcdb.setOnClickListener {
            if (viewHolder.unitcd.text.toString().endsWith("f"))
                viewHolder.unitcd.text = s.getCD(f, t, 1, talents, level)
            else
                viewHolder.unitcd.text = s.getCD(f, t, 0, talents, level)
        }

        viewHolder.unitpreatkb.setOnClickListener {
            if (viewHolder.unitpreatk.text.toString().endsWith("f"))
                viewHolder.unitpreatk.text = s.getPre(f, 1)
            else
                viewHolder.unitpreatk.text = s.getPre(f, 0)
        }

        viewHolder.unitpostb.setOnClickListener {
            if (viewHolder.unitpost.text.toString().endsWith("f"))
                viewHolder.unitpost.text = s.getPost(f, 1)
            else
                viewHolder.unitpost.text = s.getPost(f, 0)
        }

        viewHolder.unittbab.setOnClickListener {
            if (viewHolder.unittba.text.toString().endsWith("f"))
                viewHolder.unittba.text = s.getTBA(f, talents, 1, level)
            else
                viewHolder.unittba.text = s.getTBA(f, talents, 0, level)
        }

        viewHolder.unitatkb.setOnClickListener {
            if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk)) {
                viewHolder.unitatkb.text = context.getString(R.string.unit_info_dps)

                viewHolder.unitatk.text = s.getDPS(f, t, talents, level)
            } else {
                viewHolder.unitatkb.text = context.getString(R.string.unit_info_atk)

                viewHolder.unitatk.text = s.getAtk(f, t, talents, level)
            }
        }

        viewHolder.unitatktb.setOnClickListener {
            if (viewHolder.unitatkt.text.toString().endsWith("f"))
                viewHolder.unitatkt.text = s.getAtkTime(f, talents, 1, level)
            else
                viewHolder.unitatkt.text = s.getAtkTime(f, talents, 0, level)
        }

        viewHolder.unitlevel.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val level = viewHolder.unitlevel.selectedItem as Int
                val levelp = viewHolder.unitlevelp.selectedItem as Int

                this@UnitinfRecycle.level.setLevel(level)

                viewHolder.unithp.text = s.getHP(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)

                if (f.du.rawAtkData().size > 1) {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getAtk(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                } else {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getTotAtk(f, t, this@UnitinfRecycle.talents,
                            this@UnitinfRecycle.level
                        )
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                }

                if(CommonStatic.getConfig().realLevel) {
                    for(i in superTalent.indices) {
                        changeSpinner(superTalent[i], level + levelp >= 60)
                    }

                    validate(viewHolder, f, t)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewHolder.unitlevelp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val level = viewHolder.unitlevel.selectedItem as Int
                val levelp = viewHolder.unitlevelp.selectedItem as Int

                this@UnitinfRecycle.level.setPlusLevel(levelp)

                viewHolder.unithp.text = s.getHP(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                if (f.du.rawAtkData().size > 1) {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getAtk(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                } else {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getAtk(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, this@UnitinfRecycle.talents, this@UnitinfRecycle.level)
                }

                if(CommonStatic.getConfig().realLevel) {
                    for(i in superTalent.indices) {
                        changeSpinner(superTalent[i], level + levelp >= 60)
                    }

                    validate(viewHolder, f, t)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        cdlevt.setSelection(cdlevt.text?.length ?: 0)
        cdtreat.setSelection(cdtreat.text?.length ?: 0)
        atktreat.setSelection(atktreat.text?.length ?: 0)
        healtreat.setSelection(healtreat.text?.length ?: 0)

        cdlevt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 30 || s.toString().toInt() <= 0) {
                        if (cdlev.isHelperTextEnabled) {
                            cdlev.isHelperTextEnabled = false
                            cdlev.isErrorEnabled = true
                            cdlev.error = context.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (cdlev.isErrorEnabled) {
                            cdlev.error = null
                            cdlev.isErrorEnabled = false
                            cdlev.isHelperTextEnabled = true
                            cdlev.setHelperTextColor(ColorStateList(states, color))
                            cdlev.helperText = "1~30 Lv."
                        }
                    }
                } else {
                    if (cdlev.isErrorEnabled) {
                        cdlev.error = null
                        cdlev.isErrorEnabled = false
                        cdlev.isHelperTextEnabled = true
                        cdlev.setHelperTextColor(ColorStateList(states, color))
                        cdlev.helperText = "1~30 Lv."
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() in 1..30) {
                        val lev = text.toString().toInt()

                        t.tech[0] = lev

                        if (viewHolder.unitcd.text.toString().endsWith("s")) {
                            viewHolder.unitcd.text = s.getCD(f, t, 1,
                                this@UnitinfRecycle.talents, level)
                        } else {
                            viewHolder.unitcd.text = s.getCD(f, t, 0,
                                this@UnitinfRecycle.talents, level)
                        }
                    }
                } else {
                    t.tech[0] = 1

                    if (viewHolder.unitcd.text.toString().endsWith("s")) {
                        viewHolder.unitcd.text = s.getCD(f, t, 1, this@UnitinfRecycle.talents, level)
                    } else {
                        viewHolder.unitcd.text = s.getCD(f, t, 0, this@UnitinfRecycle.talents, level)
                    }
                }
            }
        })
        cdtreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 300) {
                        if (cdtrea.isHelperTextEnabled) {
                            cdtrea.isHelperTextEnabled = false
                            cdtrea.isErrorEnabled = true
                            cdtrea.error = context.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (cdtrea.isErrorEnabled) {
                            cdtrea.error = null
                            cdtrea.isErrorEnabled = false
                            cdtrea.isHelperTextEnabled = true
                            cdtrea.setHelperTextColor(ColorStateList(states, color))
                            cdtrea.helperText = "0~300"
                        }
                    }
                } else {
                    if (cdtrea.isErrorEnabled) {
                        cdtrea.error = null
                        cdtrea.isErrorEnabled = false
                        cdtrea.isHelperTextEnabled = true
                        cdtrea.setHelperTextColor(ColorStateList(states, color))
                        cdtrea.helperText = "0~300"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 300) {
                        val trea = text.toString().toInt()

                        t.trea[2] = trea

                        if (viewHolder.unitcd.text.toString().endsWith("s")) {
                            viewHolder.unitcd.text = s.getCD(f, t, 1,
                                this@UnitinfRecycle.talents, level)
                        } else {
                            viewHolder.unitcd.text = s.getCD(f, t, 0,
                                this@UnitinfRecycle.talents, level)
                        }
                    }
                } else {
                    t.trea[2] = 0

                    if (viewHolder.unitcd.text.toString().endsWith("s")) {
                        viewHolder.unitcd.text = s.getCD(f, t, 1, this@UnitinfRecycle.talents, level)
                    } else {
                        viewHolder.unitcd.text = s.getCD(f, t, 0, this@UnitinfRecycle.talents, level)
                    }
                }
            }
        })
        atktreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 300) {
                        if (atktrea.isHelperTextEnabled) {
                            atktrea.isHelperTextEnabled = false
                            atktrea.isErrorEnabled = true
                            atktrea.error = context.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (atktrea.isErrorEnabled) {
                            atktrea.error = null
                            atktrea.isErrorEnabled = false
                            atktrea.isHelperTextEnabled = true
                            atktrea.setHelperTextColor(ColorStateList(states, color))
                            atktrea.helperText = "0~300"
                        }
                    }
                } else {
                    if (atktrea.isErrorEnabled) {
                        atktrea.error = null
                        atktrea.isErrorEnabled = false
                        atktrea.isHelperTextEnabled = true
                        atktrea.setHelperTextColor(ColorStateList(states, color))
                        atktrea.helperText = "0~300"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 300) {
                        t.trea[0] = text.toString().toInt()

                        if (viewHolder.unitatkb.text.toString() == context.getString(R.string.unit_info_dps)) {
                            viewHolder.unitatk.text = s.getDPS(f, t,
                                this@UnitinfRecycle.talents, level)
                        } else {
                            viewHolder.unitatk.text = s.getAtk(f, t,
                                this@UnitinfRecycle.talents, level)
                        }
                    }
                } else {
                    t.trea[0] = 0

                    if (viewHolder.unitatkb.text.toString() == context.getString(R.string.unit_info_dps)) {
                        viewHolder.unitatk.text = s.getDPS(f, t, this@UnitinfRecycle.talents, level)
                    } else {
                        viewHolder.unitatk.text = s.getAtk(f, t, this@UnitinfRecycle.talents, level)
                    }
                }
            }
        })
        healtreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 300) {
                        if (healtrea.isHelperTextEnabled) {
                            healtrea.isHelperTextEnabled = false
                            healtrea.isErrorEnabled = true
                            healtrea.error = context.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (healtrea.isErrorEnabled) {
                            healtrea.error = null
                            healtrea.isErrorEnabled = false
                            healtrea.isHelperTextEnabled = true
                            healtrea.setHelperTextColor(ColorStateList(states, color))
                            healtrea.helperText = "0~300"
                        }
                    }
                } else {
                    if (healtrea.isErrorEnabled) {
                        healtrea.error = null
                        healtrea.isErrorEnabled = false
                        healtrea.isHelperTextEnabled = true
                        healtrea.setHelperTextColor(ColorStateList(states, color))
                        healtrea.helperText = "0~300"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 300) {
                        t.trea[1] = text.toString().toInt()

                        viewHolder.unithp.text = s.getHP(f, t, this@UnitinfRecycle.talents, level)
                    }
                } else {
                    t.trea[1] = 0

                    viewHolder.unithp.text = s.getHP(f, t, this@UnitinfRecycle.talents, level)
                }
            }
        })

        reset.setOnClickListener {
            t.tech[0] = 30
            t.trea[0] = 300
            t.trea[1] = 300
            t.trea[2] = 300

            cdlevt.setText(t.tech[0].toString())
            cdtreat.setText(t.trea[0].toString())
            atktreat.setText(t.trea[1].toString())
            healtreat.setText(t.trea[2].toString())

            if (viewHolder.unitcd.text.toString().endsWith("s")) {
                viewHolder.unitcd.text = s.getCD(f, t, 1, talents, level)
            } else {
                viewHolder.unitcd.text = s.getCD(f, t, 0, talents, level)
            }

            if (viewHolder.unitatkb.text.toString() == context.getString(R.string.unit_info_dps)) {
                viewHolder.unitatk.text = s.getDPS(f, t, talents, level)
            } else {
                viewHolder.unitatk.text = s.getAtk(f, t, talents, level)
            }

            viewHolder.unithp.text = s.getHP(f, t, talents, level)
        }

        viewHolder.unittalen.setOnCheckedChangeListener { _, isChecked ->
            talents = true

            validate(viewHolder, f, t)

            if (isChecked) {
                val anim = ScaleAnimator(viewHolder.npresetrow, AnimatorConst.WIDTH, 300, AnimatorConst.DECELERATE, 0, StaticStore.dptopx(100f, context))
                anim.start()

                val anim2 = ScaleAnimator(viewHolder.nprow, AnimatorConst.HEIGHT, 300, AnimatorConst.DECELERATE, 0, StaticStore.dptopx(48f, context))
                anim2.start()

                val anim3 = ScaleAnimator(viewHolder.nprow, AnimatorConst.TOP_MARGIN, 300, AnimatorConst.DECELERATE, 0, StaticStore.dptopx(16f, context))
                anim3.start()

                val anim4 = ScaleAnimator(viewHolder.supernprow, AnimatorConst.HEIGHT, 300, AnimatorConst.DECELERATE, 0, StaticStore.dptopx(48f, context))
                anim4.start()
            } else {
                val anim = ScaleAnimator(viewHolder.npresetrow, AnimatorConst.WIDTH, 300, AnimatorConst.DECELERATE, StaticStore.dptopx(100f, context), 0)
                anim.start()

                val anim2 = ScaleAnimator(viewHolder.nprow, AnimatorConst.HEIGHT, 300, AnimatorConst.DECELERATE, StaticStore.dptopx(48f, context), 0)
                anim2.start()

                val anim3 = ScaleAnimator(viewHolder.nprow, AnimatorConst.TOP_MARGIN, 300, AnimatorConst.DECELERATE, StaticStore.dptopx(16f, context), 0)
                anim3.start()

                val anim4 = ScaleAnimator(viewHolder.supernprow, AnimatorConst.HEIGHT, 300, AnimatorConst.DECELERATE, StaticStore.dptopx(48f, context), 0)
                anim4.start()
            }
        }

        for (i in talent.indices) {
            talent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[talentIndex[i]] = talent[i].selectedItem as Int

                    validate(viewHolder, f, t)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            talent[i].setOnLongClickListener {
                talent[i].isClickable = false

                StaticStore.showShortMessage(context, s.getTalentName(talentIndex[i], f))
                true
            }
        }

        for(i in superTalent.indices) {
            superTalent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[superTalentIndex[i]] = superTalent[i].selectedItem as Int

                    validate(viewHolder, f, t)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            superTalent[i].setOnLongClickListener {
                superTalent[i].isClickable = false

                StaticStore.showShortMessage(context, s.getTalentName(superTalentIndex[i], f))
                true
            }
        }

        viewHolder.npreset.setOnClickListener {
            val max = f.du.pCoin.max

            for(i in max.indices) {
                level.talents[i] = max[i]
            }

            for (i in talent.indices) {
                talent[i].setSelection(getIndex(talent[i], max[talentIndex[i]]))
            }

            for (i in superTalent.indices) {
                superTalent[i].setSelection(getIndex(superTalent[i], max[superTalentIndex[i]]))
            }

            validate(viewHolder, f, t)
        }
    }

    private fun getIndex(spinner: Spinner?, lev: Int): Int {
        var index = 0

        for (i in 0 until spinner!!.count)
            if (lev == spinner.getItemAtPosition(i) as Int)
                index = i

        return index
    }

    override fun getItemCount(): Int {
        return names.size
    }

    private fun validate(viewHolder: ViewHolder, f: Form, t: Treasure) {
        viewHolder.unithp.text = s.getHP(f, t, talents, level)

        viewHolder.unithb.text = s.getHB(f, talents, level)

        if (viewHolder.unitatkb.text.toString() == "DPS")
            viewHolder.unitatk.text = s.getDPS(f, t, talents, level)
        else
            viewHolder.unitatk.text = s.getAtk(f, t, talents, level)

        viewHolder.unitcost.text = s.getCost(f, talents, level)

        if (viewHolder.unitcd.text.toString().endsWith("s"))
            viewHolder.unitcd.text = s.getCD(f, t, 1, talents, level)
        else
            viewHolder.unitcd.text = s.getCD(f, t, 0, talents, level)

        viewHolder.unittrait.text = s.getTrait(f, talents, level)

        viewHolder.unitspd.text = s.getSpd(f, talents, level)

        viewHolder.unittba.text = s.getTBA(f, talents, fs, level)

        viewHolder.unitatkt.text = s.getAtkTime(f, talents, fs, level)

        val du: MaskUnit = if (f.du.pCoin != null && talents)
            f.du.pCoin.improve(level.talents)
        else
            f.du

        val level = viewHolder.unitlevel.selectedItem as Int
        val levelp = viewHolder.unitlevelp.selectedItem as Int

        this.level.setLevel(level)
        this.level.setPlusLevel(levelp)

        val abil = Interpret.getAbi(du, fragment, StaticStore.addition, 0)

        val proc = Interpret.getProc(du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

        val abilityicon = Interpret.getAbiid(du)

        if (abil.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE

            val linearLayoutManager = LinearLayoutManager(context)

            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

            viewHolder.unitabil.layoutManager = linearLayoutManager

            val adapterAbil = AdapterAbil(abil, proc, abilityicon, context)

            viewHolder.unitabil.adapter = adapterAbil

            ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false)
        } else {
            viewHolder.unitabil.visibility = View.GONE
        }
    }

    private fun changeSpinner(spinner: Spinner, enable: Boolean) {
        spinner.isEnabled = enable
        spinner.background.alpha = if(enable)
            255
        else
            64

        if(spinner.childCount >= 1 && spinner.getChildAt(0) is AutoMarquee) {
            (spinner.getChildAt(0) as AutoMarquee).setTextColor((spinner.getChildAt(0) as AutoMarquee).textColors.withAlpha(if(enable) 255 else 64))
        }
    }
}