package com.mandarin.bcu.androidutil.unit.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.Interpret
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import com.mandarin.bcu.androidutil.supports.ScaleAnimator
import com.mandarin.bcu.androidutil.supports.adapter.AdapterAbil
import common.CommonStatic
import common.battle.BasisSet
import common.battle.Treasure
import common.battle.data.MaskUnit
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.Form
import common.util.unit.Level
import common.util.unit.Unit
import java.util.*

class UnitinfPager : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(form: Int, data: Identifier<Unit>, names: Array<String>): UnitinfPager {
            val pager = UnitinfPager()

            val bundle = Bundle()

            bundle.putString("Data", JsonEncoder.encode(data).toString())
            bundle.putInt("Form", form)
            bundle.putStringArray("Names", names)

            pager.arguments = bundle

            return pager
        }
    }

    private var form = 0
    private var fs = 0
    private lateinit var s: GetStrings
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private var color: IntArray = IntArray(1)
    private var talents = false
    private val level = Level(8)
    
    private var isRaw = false
    private val talentIndex = ArrayList<Int>()
    private val superTalentIndex = ArrayList<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.unit_table, container, false)
        val frse = view.findViewById<Button>(R.id.unitinffrse)
        val unitpack = view.findViewById<TextView>(R.id.unitinfpackr)
        val unitname = view.findViewById<TextView>(R.id.unitinfname)
        val unitid = view.findViewById<TextView>(R.id.unitinfidr)
        val unithp = view.findViewById<TextView>(R.id.unitinfhpr)
        val unithb = view.findViewById<TextView>(R.id.unitinfhbr)
        val uniticon = view.findViewById<ImageView>(R.id.unitinficon)
        val unitatk = view.findViewById<TextView>(R.id.unitinfatkr)
        val unittrait = view.findViewById<TextView>(R.id.unitinftraitr)
        val unitcost = view.findViewById<TextView>(R.id.unitinfcostr)
        val unitsimu = view.findViewById<TextView>(R.id.unitinfsimur)
        val unitspd = view.findViewById<TextView>(R.id.unitinfspdr)
        val unitcd = view.findViewById<TextView>(R.id.unitinfcdr)
        val unitrang = view.findViewById<TextView>(R.id.unitinfrangr)
        val unitpreatk = view.findViewById<TextView>(R.id.unitinfpreatkr)
        val unitpost = view.findViewById<TextView>(R.id.unitinfpostr)
        val unittba = view.findViewById<TextView>(R.id.unitinftbar)
        val unitatkt = view.findViewById<TextView>(R.id.unitinfatktimer)
        val unitabilt = view.findViewById<TextView>(R.id.unitinfabiltr)
        val none = view.findViewById<TextView>(R.id.unitabilnone)
        val unitabil: RecyclerView = view.findViewById(R.id.unitinfabilr)
        val unittalen = view.findViewById<CheckBox>(R.id.unitinftalen)
        val npreset = view.findViewById<Button>(R.id.unitinftalreset)
        val nprow = view.findViewById<TableRow>(R.id.talenrow)
        val supernprow = view.findViewById<TableRow>(R.id.supertalenrow)

        val activity = requireActivity()

        s = GetStrings(activity)

        color = intArrayOf(
                StaticStore.getAttributeColor(activity, R.attr.TextPrimary)
        )

        val arg = arguments

        if(arg == null) {
            Log.e("UnitinfPager", "Arguments is null")
            return view
        }

        form = arg.getInt("Form")

        unitabil.isFocusableInTouchMode = false
        unitabil.isFocusable = false
        unitabil.isNestedScrollingEnabled = false

        val cdlev: TextInputLayout = activity.findViewById(R.id.cdlev)
        val cdtrea: TextInputLayout = activity.findViewById(R.id.cdtrea)
        val atktrea: TextInputLayout = activity.findViewById(R.id.atktrea)
        val healtrea: TextInputLayout = activity.findViewById(R.id.healtrea)

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

        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        if (shared.getBoolean("frame", true)) {
            fs = 0
            frse.text = activity.getString(R.string.unit_info_fr)
        } else {
            fs = 1
            frse.text = activity.getString(R.string.unit_info_sec)
        }

        val t = BasisSet.current().t()

        val u = Identifier.get(StaticStore.transformIdentifier<Unit>(arg.getString("Data")))

        if(u == null) {
            Log.e("UnitinfPager", "Identifier is null\nArgument : ${arg.getString("Data")}")

            return view
        }

        val f = u.forms[form]

        level.setLevel(f.unit.preferredLevel)
        level.setPlusLevel(f.unit.preferredPlusLevel)

        val ability = Interpret.getAbi(f.du, fragment, StaticStore.addition, 0, activity)
        val abilityicon = Interpret.getAbiid(f.du)
        val cdlevt: TextInputEditText = activity.findViewById(R.id.cdlevt)
        val cdtreat: TextInputEditText = activity.findViewById(R.id.cdtreat)
        val atktreat: TextInputEditText = activity.findViewById(R.id.atktreat)
        val healtreat: TextInputEditText = activity.findViewById(R.id.healtreat)

        cdlevt.setText(t.tech[0].toString())
        cdtreat.setText(t.trea[2].toString())
        atktreat.setText(t.trea[0].toString())
        healtreat.setText(t.trea[1].toString())

        val proc = Interpret.getProc(f.du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

        val name = MultiLangCont.get(f) ?: f.names.toString()

        val uni = f.anim.uni

        var icon = if(uni != null) {
            f.anim.uni.img.bimg() as Bitmap
        } else {
            StaticStore.empty(StaticStore.dptopx(48f, activity),StaticStore.dptopx(48f, activity))
        }

        icon = if (icon.height != icon.width)
            StaticStore.makeIcon(activity, icon, 48f)
        else
            StaticStore.getResizeb(icon, activity, 48f)

        uniticon.setImageBitmap(icon)

        unitname.text = name

        unitpack.text = s.getPackName(f.unit.id, isRaw)
        unitid.text = s.getID(form, StaticStore.trio(u.id.id))
        unithp.text = s.getHP(f, t, false, level)
        unithb.text = s.getHB(f, false, level)
        unitatk.text = s.getTotAtk(f, t, false, level)
        unittrait.text = s.getTrait(f, false, level, activity)
        unitcost.text = s.getCost(f, false, level)
        unitsimu.text = s.getSimu(f)
        unitspd.text = s.getSpd(f, false, level)
        unitcd.text = s.getCD(f, t, fs, false, level)
        unitrang.text = s.getRange(f)
        unitpreatk.text = s.getPre(f, fs)
        unitpost.text = s.getPost(f, fs)
        unittba.text = s.getTBA(f, talents, fs, level)
        unitatkt.text = s.getAtkTime(f, talents, fs, level)
        unitabilt.text = s.getAbilT(f)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            none.visibility = View.GONE
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            unitabil.layoutManager = linearLayoutManager
            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)
            unitabil.adapter = adapterAbil
            ViewCompat.setNestedScrollingEnabled(unitabil, true)
        } else {
            unitabil.visibility = View.GONE
        }

        if(f.du.pCoin != null) {
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

                nprow.addView(spin)

                spin
            }

            val superTalent = Array(superTalentIndex.size) {
                val spin = Spinner(context)

                val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (superTalentIndex.size)).toFloat())

                spin.layoutParams = param
                spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                supernprow.addView(spin)

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

                val talentLevels = ArrayList<Int>()

                for(j in 0 until max[talentIndex[i]] + 1)
                    talentLevels.add(j)

                val adapter = ArrayAdapter(activity, R.layout.spinneradapter, talentLevels)

                talent[i].adapter = adapter
                talent[i].setSelection(getIndex(talent[i], max[talentIndex[i]]))

                level.talents[talentIndex[i]] = max[talentIndex[i]]
            }

            for(i in superTalent.indices) {
                if(superTalentIndex[i] >= f.du.pCoin.info.size) {
                    superTalent[i].isEnabled = false
                    continue
                }

                val superTalentLevels = ArrayList<Int>()

                for(j in 0 until max[superTalentIndex[i]] + 1)
                    superTalentLevels.add(j)

                val adapter = ArrayAdapter(activity, R.layout.spinneradapter, superTalentLevels)

                superTalent[i].adapter = adapter
                superTalent[i].setSelection(getIndex(superTalent[i], max[superTalentIndex[i]]))

                level.talents[superTalentIndex[i]] = max[superTalentIndex[i]]

                if(CommonStatic.getConfig().realLevel) {
                    changeSpinner(superTalent[i], level.lv + level.plusLv >= 60)
                }
            }

            if(superTalent.isEmpty())
                supernprow.visibility = View.GONE

            listeners(view, talent, superTalent)
        } else {
            unittalen.visibility = View.GONE
            npreset.visibility = View.GONE
            nprow.visibility = View.GONE
            supernprow.visibility = View.GONE

            for (i in 0 until level.talents.size)
                level.talents[i] = 0

            listeners(view, arrayOf(), arrayOf())
        }

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners(view: View, talent: Array<Spinner>, superTalent: Array<Spinner>) {
        val activity = activity ?: return

        val cdlev: TextInputLayout = Objects.requireNonNull<Activity>(activity).findViewById(R.id.cdlev)
        val cdtrea: TextInputLayout = activity.findViewById(R.id.cdtrea)
        val atktrea: TextInputLayout = activity.findViewById(R.id.atktrea)
        val healtrea: TextInputLayout = activity.findViewById(R.id.healtrea)
        val cdlevt: TextInputEditText = activity.findViewById(R.id.cdlevt)
        val cdtreat: TextInputEditText = activity.findViewById(R.id.cdtreat)
        val atktreat: TextInputEditText = activity.findViewById(R.id.atktreat)
        val healtreat: TextInputEditText = activity.findViewById(R.id.healtreat)
        val reset = activity.findViewById<Button>(R.id.treasurereset)
        val frse = view.findViewById<Button>(R.id.unitinffrse)
        val pack = view.findViewById<Button>(R.id.unitinfpack)
        val unitpack = view.findViewById<TextView>(R.id.unitinfpackr)
        val unitname = view.findViewById<TextView>(R.id.unitinfname)
        val unithp = view.findViewById<TextView>(R.id.unitinfhpr)
        val unitlevel = view.findViewById<Spinner>(R.id.unitinflevr)
        val unitlevelp = view.findViewById<Spinner>(R.id.unitinflevpr)
        val unitplus = view.findViewById<TextView>(R.id.unitinfplus)
        val unitatkb = view.findViewById<Button>(R.id.unitinfatk)
        val unitatk = view.findViewById<TextView>(R.id.unitinfatkr)
        val unitcdb = view.findViewById<Button>(R.id.unitinfcd)
        val unitcd = view.findViewById<TextView>(R.id.unitinfcdr)
        val unitpreatkb = view.findViewById<Button>(R.id.unitinfpreatk)
        val unitpreatk = view.findViewById<TextView>(R.id.unitinfpreatkr)
        val unitpostb = view.findViewById<Button>(R.id.unitinfpost)
        val unitpost = view.findViewById<TextView>(R.id.unitinfpostr)
        val unittbab = view.findViewById<Button>(R.id.unitinftba)
        val unittba = view.findViewById<TextView>(R.id.unitinftbar)
        val unitatktb = view.findViewById<Button>(R.id.unitinfatktime)
        val unitatkt = view.findViewById<TextView>(R.id.unitinfatktimer)
        val unitabil: RecyclerView = view.findViewById(R.id.unitinfabilr)
        val unittalen = view.findViewById<CheckBox>(R.id.unitinftalen)
        val npreset = view.findViewById<Button>(R.id.unitinftalreset)
        val npresetrow = view.findViewById<TableRow>(R.id.talresetrow)
        val nprow = view.findViewById<TableRow>(R.id.talenrow)
        val supernprow = view.findViewById<TableRow>(R.id.supertalenrow)

        val t = BasisSet.current().t()

        val arg = arguments ?: return

        val u = Identifier.get(StaticStore.transformIdentifier<Unit>(arg.getString("Data"))) ?: return

        val f = u.forms[form]

        unitplus.text = " + "

        val levels: MutableList<Int> = ArrayList()

        for (j in 1 until f.unit.max + 1)
            levels.add(j)

        val levelsp = ArrayList<Int>()

        for (j in 0 until f.unit.maxp + 1)
            levelsp.add(j)

        val arrayAdapter = ArrayAdapter(activity, R.layout.spinneradapter, levels)
        val arrayAdapterp = ArrayAdapter(activity, R.layout.spinneradapter, levelsp)

        unitname.setOnLongClickListener(OnLongClickListener {
            if (getActivity() == null)
                return@OnLongClickListener false

            val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText(null, unitname.text)

            clipboardManager.setPrimaryClip(data)

            StaticStore.showShortMessage(activity, R.string.unit_info_copied)

            true
        })

        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        level.setLevel(
            when {
                shared.getInt("default_level", 50) > f.unit.max -> f.unit.max
                f.unit.rarity != 0 -> shared.getInt("default_level", 50)
                else -> f.unit.max
            }
        )

        level.setPlusLevel(f.unit.preferredPlusLevel)

        unitlevel.adapter = arrayAdapter
        unitlevel.setSelection(getIndex(unitlevel, level.lv))

        unitlevelp.adapter = arrayAdapterp
        unitlevelp.setSelection(getIndex(unitlevelp, level.plusLv))

        if (levelsp.size == 1) {
            unitlevelp.visibility = View.GONE
            unitplus.visibility = View.GONE
        }

        pack.setOnClickListener {
            isRaw = !isRaw

            unitpack.text = s.getPackName(f.unit.id, isRaw)
        }

        frse.setOnClickListener {
            if (fs == 0) {
                fs = 1

                unitcd.text = s.getCD(f, t, fs, talents, level)
                unitpreatk.text = s.getPre(f, fs)
                unitpost.text = s.getPost(f, fs)
                unittba.text = s.getTBA(f, talents, fs, level)
                unitatkt.text = s.getAtkTime(f, talents, fs, level)
                frse.text = activity.getString(R.string.unit_info_sec)

                if (unitabil.visibility != View.GONE) {
                    var du = f.du

                    if (f.du.pCoin != null)
                        du = if (talents)
                            f.du.pCoin.improve(level.talents)
                        else
                            f.du

                    val ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0, activity)

                    val abilityicon = Interpret.getAbiid(du)

                    val proc = Interpret.getProc(du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

                    val linearLayoutManager = LinearLayoutManager(activity)

                    linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

                    unitabil.layoutManager = linearLayoutManager

                    val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)

                    unitabil.adapter = adapterAbil

                    ViewCompat.setNestedScrollingEnabled(unitabil, false)
                }
            } else {
                fs = 0

                unitcd.text = s.getCD(f, t, fs, talents, level)
                unitpreatk.text = s.getPre(f, fs)
                unitpost.text = s.getPost(f, fs)
                unittba.text = s.getTBA(f, talents, fs, level)
                unitatkt.text = s.getAtkTime(f, talents, fs, level)
                frse.text = activity.getString(R.string.unit_info_fr)

                if (unitabil.visibility != View.GONE) {
                    var du = f.du

                    if (f.du.pCoin != null)
                        du = if (talents)
                            f.du.pCoin.improve(level.talents)
                        else
                            f.du

                    val ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0, activity)

                    val abilityicon = Interpret.getAbiid(du)

                    val proc = Interpret.getProc(du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

                    val linearLayoutManager = LinearLayoutManager(activity)

                    linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

                    unitabil.layoutManager = linearLayoutManager

                    val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)

                    unitabil.adapter = adapterAbil

                    ViewCompat.setNestedScrollingEnabled(unitabil, false)
                }
            }
        }

        unitcdb.setOnClickListener {
            if (unitcd.text.toString().endsWith("f"))
                unitcd.text = s.getCD(f, t, 1, talents, level)
            else
                unitcd.text = s.getCD(f, t, 0, talents, level)
        }

        unitpreatkb.setOnClickListener {
            if (unitpreatk.text.toString().endsWith("f"))
                unitpreatk.text = s.getPre(f, 1)
            else
                unitpreatk.text = s.getPre(f, 0)
        }

        unitpostb.setOnClickListener {
            if (unitpost.text.toString().endsWith("f"))
                unitpost.text = s.getPost(f, 1)
            else
                unitpost.text = s.getPost(f, 0)
        }

        unittbab.setOnClickListener {
            if (unittba.text.toString().endsWith("f"))
                unittba.text = s.getTBA(f, talents, 1, level)
            else
                unittba.text = s.getTBA(f, talents, 0, level)
        }

        unitatkb.setOnClickListener {
            if (unitatkb.text == activity.getString(R.string.unit_info_atk)) {
                unitatkb.text = activity.getString(R.string.unit_info_dps)
                unitatk.text = s.getDPS(f, t, talents, level)
            } else {
                unitatkb.text = activity.getString(R.string.unit_info_atk)
                unitatk.text = s.getAtk(f, t, talents, level)
            }
        }

        unitatktb.setOnClickListener {
            if (unitatkt.text.toString().endsWith("f"))
                unitatkt.text = s.getAtkTime(f, talents, 1, level)
            else
                unitatkt.text = s.getAtkTime(f, talents, 0, level)
        }

        unitlevel.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val level = unitlevel.selectedItem as Int
                val levelp = unitlevelp.selectedItem as Int

                this@UnitinfPager.level.setLevel(level)

                unithp.text = s.getHP(f, t, talents, this@UnitinfPager.level)

                if (f.du.rawAtkData().size > 1) {
                    if (unitatkb.text == activity.getString(R.string.unit_info_atk))
                        unitatk.text = s.getAtk(f, t, talents, this@UnitinfPager.level)
                    else
                        unitatk.text = s.getDPS(f, t, talents, this@UnitinfPager.level)
                } else {
                    if (unitatkb.text == activity.getString(R.string.unit_info_atk))
                        unitatk.text = s.getTotAtk(f, t, talents, this@UnitinfPager.level)
                    else
                        unitatk.text = s.getDPS(f, t, talents, this@UnitinfPager.level)
                }

                if(CommonStatic.getConfig().realLevel) {
                    for(i in superTalent.indices) {
                        changeSpinner(superTalent[i], level + levelp >= 60)
                    }

                    validate(view, f, t)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        unitlevelp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val level = unitlevel.selectedItem as Int
                val levelp = unitlevelp.selectedItem as Int

                this@UnitinfPager.level.setPlusLevel(levelp)

                unithp.text = s.getHP(f, t, talents, this@UnitinfPager.level)

                if (f.du.rawAtkData().size > 1) {
                    if (unitatkb.text == activity.getString(R.string.unit_info_atk))
                        unitatk.text = s.getAtk(f, t, talents, this@UnitinfPager.level)
                    else
                        unitatk.text = s.getDPS(f, t, talents, this@UnitinfPager.level)
                } else {
                    if (unitatkb.text == activity.getString(R.string.unit_info_atk))
                        unitatk.text = s.getAtk(f, t, talents, this@UnitinfPager.level)
                    else
                        unitatk.text = s.getDPS(f, t, talents, this@UnitinfPager.level)
                }

                if(CommonStatic.getConfig().realLevel) {
                    for(i in superTalent.indices) {
                        changeSpinner(superTalent[i], level + levelp >= 60)
                    }

                    validate(view, f, t)
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
                            cdlev.error = activity.getString(R.string.treasure_invalid)
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

                        if (unitcd.text.toString().endsWith("s")) {
                            unitcd.text = s.getCD(f, t, 1, talents, level)
                        } else {
                            unitcd.text = s.getCD(f, t, 0, talents, level)
                        }
                    }
                } else {
                    t.tech[0] = 1

                    if (unitcd.text.toString().endsWith("s")) {
                        unitcd.text = s.getCD(f, t, 1, talents, level)
                    } else {
                        unitcd.text = s.getCD(f, t, 0, talents, level)
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
                            cdtrea.error = activity.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (cdtrea.isErrorEnabled) {
                            cdtrea.error = null
                            cdtrea.isErrorEnabled = false
                            cdtrea.isHelperTextEnabled = true
                            cdtrea.setHelperTextColor(ColorStateList(states, color))
                            cdtrea.helperText = "0~300 %"
                        }
                    }
                } else {
                    if (cdtrea.isErrorEnabled) {
                        cdtrea.error = null
                        cdtrea.isErrorEnabled = false
                        cdtrea.isHelperTextEnabled = true
                        cdtrea.setHelperTextColor(ColorStateList(states, color))
                        cdtrea.helperText = "0~300 %"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 300) {
                        val trea = text.toString().toInt()
                        t.trea[2] = trea
                        if (unitcd.text.toString().endsWith("s")) {
                            unitcd.text = s.getCD(f, t, 1, talents, level)
                        } else {
                            unitcd.text = s.getCD(f, t, 0, talents, level)
                        }
                    }
                } else {
                    t.trea[2] = 0
                    if (unitcd.text.toString().endsWith("s")) {
                        unitcd.text = s.getCD(f, t, 1, talents, level)
                    } else {
                        unitcd.text = s.getCD(f, t, 0, talents, level)
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
                            atktrea.error = activity.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (atktrea.isErrorEnabled) {
                            atktrea.error = null
                            atktrea.isErrorEnabled = false
                            atktrea.isHelperTextEnabled = true
                            atktrea.setHelperTextColor(ColorStateList(states, color))
                            atktrea.helperText = "0~300 %"
                        }
                    }
                } else {
                    if (atktrea.isErrorEnabled) {
                        atktrea.error = null
                        atktrea.isErrorEnabled = false
                        atktrea.isHelperTextEnabled = true
                        atktrea.setHelperTextColor(ColorStateList(states, color))
                        atktrea.helperText = "0~300 %"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 300) {
                        t.trea[0] = text.toString().toInt()

                        if (unitatkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                            unitatk.text = s.getDPS(f, t, talents, level)
                        } else {
                            unitatk.text = s.getAtk(f, t, talents, level)
                        }
                    }
                } else {
                    t.trea[0] = 0

                    if (unitatkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                        unitatk.text = s.getDPS(f, t, talents, level)
                    } else {
                        unitatk.text = s.getAtk(f, t, talents, level)
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
                            healtrea.error = activity.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (healtrea.isErrorEnabled) {
                            healtrea.error = null
                            healtrea.isErrorEnabled = false
                            healtrea.isHelperTextEnabled = true
                            healtrea.setHelperTextColor(ColorStateList(states, color))
                            healtrea.helperText = "0~300 %"
                        }
                    }
                } else {
                    if (healtrea.isErrorEnabled) {
                        healtrea.error = null
                        healtrea.isErrorEnabled = false
                        healtrea.isHelperTextEnabled = true
                        healtrea.setHelperTextColor(ColorStateList(states, color))
                        healtrea.helperText = "0~300 %"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 300) {
                        t.trea[1] = text.toString().toInt()

                        unithp.text = s.getHP(f, t, talents, level)
                    }
                } else {
                    t.trea[1] = 0

                    unithp.text = s.getHP(f, t, talents, level)
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

            if (unitcd.text.toString().endsWith("s")) {
                unitcd.text = s.getCD(f, t, 1, talents, level)
            } else {
                unitcd.text = s.getCD(f, t, 0, talents, level)
            }

            if (unitatkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                unitatk.text = s.getDPS(f, t, talents, level)
            } else {
                unitatk.text = s.getAtk(f, t, talents, level)
            }

            unithp.text = s.getHP(f, t, talents, level)
        }

        unittalen.setOnCheckedChangeListener { _, isChecked ->
            talents = isChecked

            validate(view, f, t)

            if (isChecked) {
                val anim = ScaleAnimator(npresetrow, AnimatorConst.Dimension.WIDTH, 300, AnimatorConst.Accelerator.DECELERATE, 0, StaticStore.dptopx(100f, activity))
                anim.start()

                val anim2 = ScaleAnimator(nprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, 0, StaticStore.dptopx(48f, activity))
                anim2.start()

                val anim3 = ScaleAnimator(nprow, AnimatorConst.Dimension.TOP_MARGIN, 300, AnimatorConst.Accelerator.DECELERATE, 0, StaticStore.dptopx(16f, activity))
                anim3.start()

                val anim4 = ScaleAnimator(supernprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, 0, StaticStore.dptopx(48f, activity))
                anim4.start()
            } else {
                val anim = ScaleAnimator(npresetrow, AnimatorConst.Dimension.WIDTH, 300, AnimatorConst.Accelerator.DECELERATE, StaticStore.dptopx(100f, activity), 0)
                anim.start()

                val anim2 = ScaleAnimator(nprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, StaticStore.dptopx(48f, activity), 0)
                anim2.start()

                val anim3 = ScaleAnimator(nprow, AnimatorConst.Dimension.TOP_MARGIN, 300, AnimatorConst.Accelerator.DECELERATE, StaticStore.dptopx(16f, activity), 0)
                anim3.start()

                val anim4 = ScaleAnimator(supernprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, StaticStore.dptopx(48f, activity), 0)
                anim4.start()
            }
        }

        for (i in talent.indices) {
            talent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[talentIndex[i]] = talent[i].selectedItem as Int

                    validate(view, f, t)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            talent[i].setOnLongClickListener {
                talent[i].isClickable = false

                StaticStore.showShortMessage(activity, s.getTalentName(talentIndex[i], f, activity))
                true
            }
        }

        for(i in superTalent.indices) {
            superTalent[i].onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, views: View?, position: Int, id: Long) {
                    level.talents[superTalentIndex[i]] = superTalent[i].selectedItem as Int

                    validate(view, f, t)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            superTalent[i].setOnLongClickListener {
                superTalent[i].isClickable = false

                StaticStore.showShortMessage(activity, s.getTalentName(superTalentIndex[i], f, activity))
                true
            }
        }

        npreset.setOnClickListener {
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

            validate(view, f, t)
        }
    }

    private fun validate(view: View, f: Form, t: Treasure) {
        val activity = activity ?: return

        val unithb = view.findViewById<TextView>(R.id.unitinfhbr)
        val unithp = view.findViewById<TextView>(R.id.unitinfhpr)
        val unitlevel = view.findViewById<Spinner>(R.id.unitinflevr)
        val unitlevelp = view.findViewById<Spinner>(R.id.unitinflevpr)
        val unitatkb = view.findViewById<Button>(R.id.unitinfatk)
        val unitatk = view.findViewById<TextView>(R.id.unitinfatkr)
        val unittrait = view.findViewById<TextView>(R.id.unitinftraitr)
        val unitcost = view.findViewById<TextView>(R.id.unitinfcostr)
        val unitspd = view.findViewById<TextView>(R.id.unitinfspdr)
        val unitcd = view.findViewById<TextView>(R.id.unitinfcdr)
        val unittba = view.findViewById<TextView>(R.id.unitinftbar)
        val unitatkt = view.findViewById<TextView>(R.id.unitinfatktimer)
        val none = view.findViewById<TextView>(R.id.unitabilnone)
        val unitabil: RecyclerView = view.findViewById(R.id.unitinfabilr)

        val level = unitlevel.selectedItem as Int
        val levelp = unitlevelp.selectedItem as Int

        this.level.setLevel(level)
        this.level.setPlusLevel(levelp)

        unithp.text = s.getHP(f, t, talents, this.level)
        unithb.text = s.getHB(f, talents, this.level)

        if (unitatkb.text.toString() == "DPS")
            unitatk.text = s.getDPS(f, t, talents, this.level)
        else
            unitatk.text = s.getAtk(f, t, talents, this.level)

        unitcost.text = s.getCost(f, talents, this.level)

        if (unitcd.text.toString().endsWith("s"))
            unitcd.text = s.getCD(f, t, 1, talents, this.level)
        else
            unitcd.text = s.getCD(f, t, 0, talents, this.level)

        unittrait.text = s.getTrait(f, talents, this.level, activity)
        unitspd.text = s.getSpd(f, talents, this.level)
        unittba.text = s.getTBA(f, talents, fs, this.level)
        unitatkt.text = s.getAtkTime(f, talents, fs, this.level)

        val du: MaskUnit = if (f.du.pCoin != null && talents)
            f.du.pCoin.improve(this.level.talents)
        else
            f.du

        val abil = Interpret.getAbi(du, fragment, StaticStore.addition, 0, activity)

        val proc = Interpret.getProc(du, fs == 1, true, arrayOf(1.0, 1.0).toDoubleArray())

        val abilityicon = Interpret.getAbiid(du)

        if (abil.isNotEmpty() || proc.isNotEmpty()) {
            none.visibility = View.GONE
            unitabil.visibility = View.VISIBLE

            val linearLayoutManager = LinearLayoutManager(activity)

            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

            unitabil.layoutManager = linearLayoutManager

            val adapterAbil = AdapterAbil(abil, proc, abilityicon, activity)

            unitabil.adapter = adapterAbil

            ViewCompat.setNestedScrollingEnabled(unitabil, false)
        } else {
            none.visibility = View.VISIBLE
            unitabil.visibility = View.GONE
        }
    }

    private fun getIndex(spinner: Spinner?, lev: Int): Int {
        var index = 0
        for (i in 0 until spinner!!.count)
            if (lev == spinner.getItemAtPosition(i) as Int) index = i
        return index
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