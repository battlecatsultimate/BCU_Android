package com.mandarin.bcu.androidutil.unit.adapters

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.adapter.AdapterAbil
import com.mandarin.bcu.util.Interpret
import common.battle.BasisSet
import common.battle.Treasure
import common.battle.data.MaskUnit
import common.pack.Identifier
import common.util.unit.Form
import common.util.unit.Unit
import java.util.*

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
    private var pcoins = intArrayOf(0, 0, 0, 0, 0, 0)

    private var isRaw = false

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

        private var ids = intArrayOf(R.id.talent0, R.id.talent1, R.id.talent2, R.id.talent3, R.id.talent4)

        var pcoins = arrayOfNulls<Spinner>(ids.size)

        init {
            unitplus.text = " + "
            for (i in ids.indices) pcoins[i] = itemView.findViewById(ids[i])
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(context).inflate(R.layout.unit_table, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
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
        if (f.du.pCoin == null) {
            viewHolder.unittalen.visibility = View.GONE
            viewHolder.npreset.visibility = View.GONE
            viewHolder.nprow.visibility = View.GONE
            pcoins = intArrayOf(0, 0, 0, 0, 0, 0)
        } else {
            val max = f.du.pCoin.max

            pcoins = IntArray(max.size)

            for (j in viewHolder.pcoins.indices) {
                val plev: MutableList<Int> = ArrayList()
                for (k in 0 until max[j + 1] + 1) plev.add(k)
                val adapter = ArrayAdapter(context, R.layout.spinneradapter, plev)
                viewHolder.pcoins[j]!!.adapter = adapter
                viewHolder.pcoins[j]!!.setSelection(getIndex(viewHolder.pcoins[j], max[j + 1]))
                pcoins[j + 1] = max[j + 1]
            }
        }

        pcoins[0] = f.unit.prefLv

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

        viewHolder.unitname.text = names[i]
        viewHolder.unitpack.text = s.getPackName(f.unit.id, isRaw)
        viewHolder.unitid.text = s.getID(viewHolder, StaticStore.trio(u.id.id))
        viewHolder.unithp.text = s.getHP(f, t, false, pcoins)
        viewHolder.unithb.text = s.getHB(f, false, pcoins)
        viewHolder.unitatk.text = s.getTotAtk(f, t, false, pcoins)
        viewHolder.unittrait.text = s.getTrait(f, false, pcoins)
        viewHolder.unitcost.text = s.getCost(f, false, pcoins)
        viewHolder.unitsimu.text = s.getSimu(f)
        viewHolder.unitspd.text = s.getSpd(f, false, pcoins)
        viewHolder.unitcd.text = s.getCD(f, t, fs, false, pcoins)
        viewHolder.unitrang.text = s.getRange(f)
        viewHolder.unitpreatk.text = s.getPre(f, fs)
        viewHolder.unitpost.text = s.getPost(f, fs)
        viewHolder.unittba.text = s.getTBA(f, fs)
        viewHolder.unitatkt.text = s.getAtkTime(f, fs)
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

        listeners(viewHolder)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners(viewHolder: ViewHolder) {
        val cdlev: TextInputLayout = context.findViewById(R.id.cdlev)
        val cdtrea: TextInputLayout = context.findViewById(R.id.cdtrea)
        val atktrea: TextInputLayout = context.findViewById(R.id.atktrea)
        val healtrea: TextInputLayout = context.findViewById(R.id.healtrea)
        val cdlevt: TextInputEditText = context.findViewById(R.id.cdlevt)
        val cdtreat: TextInputEditText = context.findViewById(R.id.cdtreat)
        val atktreat: TextInputEditText = context.findViewById(R.id.atktreat)
        val healtreat: TextInputEditText = context.findViewById(R.id.healtreat)
        val reset = context.findViewById<Button>(R.id.treasurereset)

        viewHolder.unitname.setOnLongClickListener {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText(null, viewHolder.unitname.text)
            clipboardManager.setPrimaryClip(data)
            StaticStore.showShortMessage(context, R.string.unit_info_copied)
            true
        }

        val t = BasisSet.current().t()
        val f = forms[viewHolder.adapterPosition]
        val levels: MutableList<Int> = ArrayList()
        for (j in 1 until f.unit.max + 1) levels.add(j)
        val levelsp = ArrayList<Int>()
        for (j in 0 until f.unit.maxp + 1) levelsp.add(j)
        val arrayAdapter = ArrayAdapter(context, R.layout.spinneradapter, levels)
        val arrayAdapterp = ArrayAdapter(context, R.layout.spinneradapter, levelsp)

        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        pcoins[0] = when {
            shared.getInt("default_level", 50) > f.unit.max -> f.unit.max
            f.unit.rarity != 0 -> shared.getInt("default_level", 50)
            else -> f.unit.max
        }

        viewHolder.unitlevel.adapter = arrayAdapter
        viewHolder.unitlevel.setSelection(getIndex(viewHolder.unitlevel, pcoins[0]))
        viewHolder.unitlevelp.adapter = arrayAdapterp

        if (f.unit.prefLv - f.unit.max < 0) {
            viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp, 0))
        } else {
            viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp, f.unit.prefLv - f.unit.max))
        }

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

                viewHolder.unitcd.text = s.getCD(f, t, fs, talents, pcoins)
                viewHolder.unitpreatk.text = s.getPre(f, fs)
                viewHolder.unitpost.text = s.getPost(f, fs)
                viewHolder.unittba.text = s.getTBA(f, fs)
                viewHolder.unitatkt.text = s.getAtkTime(f, fs)
                viewHolder.frse.text = context.getString(R.string.unit_info_sec)

                if (viewHolder.unitabil.visibility != View.GONE) {
                    var du = f.du

                    if (f.du.pCoin != null)
                        du = if (talents)
                            f.du.pCoin.improve(pcoins)
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

                viewHolder.unitcd.text = s.getCD(f, t, fs, talents, pcoins)
                viewHolder.unitpreatk.text = s.getPre(f, fs)
                viewHolder.unitpost.text = s.getPost(f, fs)
                viewHolder.unittba.text = s.getTBA(f, fs)
                viewHolder.unitatkt.text = s.getAtkTime(f, fs)
                viewHolder.frse.text = context.getString(R.string.unit_info_fr)

                if (viewHolder.unitabil.visibility != View.GONE) {
                    var du = f.du

                    if (f.du.pCoin != null)
                        du = if (talents)
                            f.du.pCoin.improve(pcoins)
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
                viewHolder.unitcd.text = s.getCD(f, t, 1, talents, pcoins)
            else
                viewHolder.unitcd.text = s.getCD(f, t, 0, talents, pcoins)
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
                viewHolder.unittba.text = s.getTBA(f, 1)
            else
                viewHolder.unittba.text = s.getTBA(f, 0)
        }

        viewHolder.unitatkb.setOnClickListener {
            if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk)) {
                viewHolder.unitatkb.text = context.getString(R.string.unit_info_dps)

                viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
            } else {
                viewHolder.unitatkb.text = context.getString(R.string.unit_info_atk)

                viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)
            }
        }

        viewHolder.unitatktb.setOnClickListener {
            if (viewHolder.unitatkt.text.toString().endsWith("f"))
                viewHolder.unitatkt.text = s.getAtkTime(f, 1)
            else
                viewHolder.unitatkt.text = s.getAtkTime(f, 0)
        }

        viewHolder.unitlevel.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val level = viewHolder.unitlevel.selectedItem as Int
                val levelp = viewHolder.unitlevelp.selectedItem as Int

                pcoins[0] = level + levelp

                viewHolder.unithp.text = s.getHP(f, t, talents, pcoins)

                if (f.du.rawAtkData().size > 1) {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
                } else {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getTotAtk(f, t, talents, pcoins)
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewHolder.unitlevelp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val level = viewHolder.unitlevel.selectedItem as Int
                val levelp = viewHolder.unitlevelp.selectedItem as Int

                pcoins[0] = level + levelp

                viewHolder.unithp.text = s.getHP(f, t, talents, pcoins)
                if (f.du.rawAtkData().size > 1) {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
                } else {
                    if (viewHolder.unitatkb.text == context.getString(R.string.unit_info_atk))
                        viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)
                    else
                        viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
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
                            viewHolder.unitcd.text = s.getCD(f, t, 1, talents, pcoins)
                        } else {
                            viewHolder.unitcd.text = s.getCD(f, t, 0, talents, pcoins)
                        }
                    }
                } else {
                    t.tech[0] = 1

                    if (viewHolder.unitcd.text.toString().endsWith("s")) {
                        viewHolder.unitcd.text = s.getCD(f, t, 1, talents, pcoins)
                    } else {
                        viewHolder.unitcd.text = s.getCD(f, t, 0, talents, pcoins)
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
                            viewHolder.unitcd.text = s.getCD(f, t, 1, talents, pcoins)
                        } else {
                            viewHolder.unitcd.text = s.getCD(f, t, 0, talents, pcoins)
                        }
                    }
                } else {
                    t.trea[2] = 0

                    if (viewHolder.unitcd.text.toString().endsWith("s")) {
                        viewHolder.unitcd.text = s.getCD(f, t, 1, talents, pcoins)
                    } else {
                        viewHolder.unitcd.text = s.getCD(f, t, 0, talents, pcoins)
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
                            viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
                        } else {
                            viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)
                        }
                    }
                } else {
                    t.trea[0] = 0

                    if (viewHolder.unitatkb.text.toString() == context.getString(R.string.unit_info_dps)) {
                        viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
                    } else {
                        viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)
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

                        viewHolder.unithp.text = s.getHP(f, t, talents, pcoins)
                    }
                } else {
                    t.trea[1] = 0

                    viewHolder.unithp.text = s.getHP(f, t, talents, pcoins)
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
                viewHolder.unitcd.text = s.getCD(f, t, 1, talents, pcoins)
            } else {
                viewHolder.unitcd.text = s.getCD(f, t, 0, talents, pcoins)
            }

            if (viewHolder.unitatkb.text.toString() == context.getString(R.string.unit_info_dps)) {
                viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
            } else {
                viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)
            }

            viewHolder.unithp.text = s.getHP(f, t, talents, pcoins)
        }

        viewHolder.unittalen.setOnCheckedChangeListener { _, isChecked ->
            talents = true

            validate(viewHolder, f, t)

            if (isChecked) {
                val anim = ValueAnimator.ofInt(0, StaticStore.dptopx(100f, context))

                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = viewHolder.npresetrow.layoutParams
                    layout.width = `val`
                    viewHolder.npresetrow.layoutParams = layout
                }

                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()

                anim.start()

                val anim2: ValueAnimator = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ValueAnimator.ofInt(0, StaticStore.dptopx(48f, context))
                } else {
                    ValueAnimator.ofInt(0, StaticStore.dptopx(56f, context))
                }
                anim2.addUpdateListener { animation ->
                    val params = viewHolder.nprow.layoutParams as ConstraintLayout.LayoutParams

                    params.height = animation.animatedValue as Int

                    viewHolder.nprow.layoutParams = params
                }

                anim2.duration = 300
                anim2.interpolator = DecelerateInterpolator()

                anim2.start()

                val anim3 = ValueAnimator.ofInt(0, StaticStore.dptopx(16f, context))

                anim3.addUpdateListener { animation ->
                    val params = viewHolder.nprow.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = animation.animatedValue as Int
                    viewHolder.nprow.layoutParams = params
                }

                anim3.duration = 300
                anim3.interpolator = DecelerateInterpolator()

                anim3.start()
            } else {
                talents = false

                validate(viewHolder, f, t)


                val anim = ValueAnimator.ofInt(StaticStore.dptopx(100f, context), 0)

                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = viewHolder.npresetrow.layoutParams
                    layout.width = `val`
                    viewHolder.npresetrow.layoutParams = layout
                }

                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()

                anim.start()

                val anim2: ValueAnimator = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    ValueAnimator.ofInt(StaticStore.dptopx(48f, context), 0)
                else
                    ValueAnimator.ofInt(StaticStore.dptopx(56f, context), 0)

                anim2.addUpdateListener { animation ->
                    val params = viewHolder.nprow.layoutParams as ConstraintLayout.LayoutParams
                    params.height = animation.animatedValue as Int
                    viewHolder.nprow.layoutParams = params
                }

                anim2.duration = 300
                anim2.interpolator = DecelerateInterpolator()

                anim2.start()

                val anim3 = ValueAnimator.ofInt(StaticStore.dptopx(16f, context), 0)

                anim3.addUpdateListener { animation ->
                    val params = viewHolder.nprow.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = animation.animatedValue as Int
                    viewHolder.nprow.layoutParams = params
                }

                anim3.duration = 300
                anim3.interpolator = DecelerateInterpolator()

                anim3.start()
            }
        }

        for (i in viewHolder.pcoins.indices) {
            viewHolder.pcoins[i]!!.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    pcoins[i+1] = viewHolder.pcoins[i]!!.selectedItem as Int
                    validate(viewHolder, f, t)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            viewHolder.pcoins[i]!!.setOnLongClickListener {
                viewHolder.pcoins[i]!!.isClickable = false

                StaticStore.showShortMessage(context, s.getTalentName(i, f))

                true
            }
        }

        viewHolder.npreset.setOnClickListener {
            for (i in viewHolder.pcoins.indices) {
                viewHolder.pcoins[i]!!.setSelection(getIndex(viewHolder.pcoins[i], f.du.pCoin.max[i + 1]))

                pcoins[i + 1] = f.du.pCoin.max[i + 1]
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
        val level = viewHolder.unitlevel.selectedItem as Int
        val levelp = viewHolder.unitlevelp.selectedItem as Int

        pcoins[0] = level + levelp

        viewHolder.unithp.text = s.getHP(f, t, talents, pcoins)

        viewHolder.unithb.text = s.getHB(f, talents, pcoins)

        if (viewHolder.unitatkb.text.toString() == "DPS")
            viewHolder.unitatk.text = s.getDPS(f, t, talents, pcoins)
        else
            viewHolder.unitatk.text = s.getAtk(f, t, talents, pcoins)

        viewHolder.unitcost.text = s.getCost(f, talents, pcoins)

        if (viewHolder.unitcd.text.toString().endsWith("s"))
            viewHolder.unitcd.text = s.getCD(f, t, 1, talents, pcoins)
        else
            viewHolder.unitcd.text = s.getCD(f, t, 0, talents, pcoins)

        viewHolder.unittrait.text = s.getTrait(f, talents, pcoins)

        viewHolder.unitspd.text = s.getSpd(f, talents, pcoins)

        val du: MaskUnit = if (f.du.pCoin != null && talents)
            f.du.pCoin.improve(pcoins)
        else
            f.du

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
}