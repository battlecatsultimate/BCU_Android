package com.mandarin.bcu.androidutil.enemy.adapters

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.AdapterAbil
import com.mandarin.bcu.util.Interpret
import common.battle.BasisSet
import common.system.MultiLangCont
import common.util.Data
import common.util.pack.Pack
import common.util.unit.Enemy

class EnemyRecycle : RecyclerView.Adapter<EnemyRecycle.ViewHolder> {
    private val pid: Int
    private val id: Int
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private var activity: Activity?
    private var fs = 0
    private var multi = 100
    private var s: GetStrings
    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private var color: IntArray

    constructor(activity: Activity, id: Int, pid: Int) {
        this.activity = activity
        this.id = id
        this.pid = pid
        s = GetStrings(activity)
        color = intArrayOf(
                getAttributeColor(activity, R.attr.TextPrimary)
        )
    }

    constructor(activity: Activity, id: Int, multi: Int, pid: Int) {
        this.activity = activity
        this.id = id
        this.pid = pid
        this.multi = multi
        s = GetStrings(activity)
        color = intArrayOf(
                getAttributeColor(activity, R.attr.TextPrimary)
        )
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.enemy_table, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val p = Pack.map[pid] ?: return

        val em = p.es[id]
        val t = StaticStore.t
        val shared = activity!!.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        if (shared.getBoolean("frame", true)) {
            fs = 0
            viewHolder.frse.text = activity!!.getString(R.string.unit_info_fr)
        } else {
            fs = 1
            viewHolder.frse.text = activity!!.getString(R.string.unit_info_sec)
        }
        val aclev: TextInputLayout = activity!!.findViewById(R.id.aclev)
        val actrea: TextInputLayout = activity!!.findViewById(R.id.actrea)
        val itfcry: TextInputLayout = activity!!.findViewById(R.id.itfcrytrea)
        val cotccry: TextInputLayout = activity!!.findViewById(R.id.cotccrytrea)
        val godmask = arrayOf<TextInputLayout>(activity!!.findViewById(R.id.godmask), activity!!.findViewById(R.id.godmask1), activity!!.findViewById(R.id.godmask2))
        val aclevt: TextInputEditText = activity!!.findViewById(R.id.aclevt)
        val actreat: TextInputEditText = activity!!.findViewById(R.id.actreat)
        val itfcryt: TextInputEditText = activity!!.findViewById(R.id.itfcrytreat)
        val cotccryt: TextInputEditText = activity!!.findViewById(R.id.cotccrytreat)
        val godmaskt = arrayOf<TextInputEditText>(activity!!.findViewById(R.id.godmaskt), activity!!.findViewById(R.id.godmaskt1), activity!!.findViewById(R.id.godmaskt2))
        aclev.isCounterEnabled = true
        aclev.counterMaxLength = 2
        aclev.setHelperTextColor(ColorStateList(states, color))
        actrea.isCounterEnabled = true
        actrea.counterMaxLength = 3
        actrea.setHelperTextColor(ColorStateList(states, color))
        itfcry.isCounterEnabled = true
        itfcry.counterMaxLength = 3
        itfcry.setHelperTextColor(ColorStateList(states, color))
        cotccry.isCounterEnabled = true
        cotccry.counterMaxLength = 4
        cotccry.setHelperTextColor(ColorStateList(states, color))
        for (til in godmask) {
            til.isCounterEnabled = true
            til.counterMaxLength = 3
            til.setHelperTextColor(ColorStateList(states, color))
        }
        viewHolder.name.text = MultiLangCont.ENAME.getCont(em) ?: em.name
        val name = Data.hex(pid)+"-"+ s.number(id)
        viewHolder.enemid.text = name
        val ratio = 32f / 32f
        val img = em?.anim?.edi?.img
        var b: Bitmap? = null
        if (img != null) b = img.bimg() as Bitmap
        viewHolder.enemicon.setImageBitmap(StaticStore.getResizeb(b, activity, 85f * ratio, 32f * ratio))
        viewHolder.enemhp.text = s.getHP(em, multi)
        viewHolder.enemhb.text = s.getHB(em)
        viewHolder.enemmulti.setText(multi.toString())
        viewHolder.enematk.text = s.getAtk(em, multi)
        viewHolder.enematktime.text = s.getAtkTime(em, fs)
        viewHolder.enemabilt.text = s.getAbilT(em)
        viewHolder.enempre.text = s.getPre(em, fs)
        viewHolder.enempost.text = s.getPost(em, fs)
        viewHolder.enemtba.text = s.getTBA(em, fs)
        viewHolder.enemtrait.text = s.getTrait(em)
        viewHolder.enematkt.text = s.getSimu(em)
        viewHolder.enemdrop.text = s.getDrop(em, t)
        viewHolder.enemrange.text = s.getRange(em)
        viewHolder.enembarrier.text = s.getBarrier(em)
        viewHolder.enemspd.text = s.getSpd(em)
        var language = StaticStore.lang[shared.getInt("Language", 0)]
        if (language == "") {
            language = Resources.getSystem().configuration.locales[0].language
        }
        val proc: List<String>
        proc = if (language == "ko" || language == "ja") {
            Interpret.getProc(activity, em.de, 1, fs)
        } else {
            Interpret.getProc(activity, em.de, 0, fs)
        }
        val ability = Interpret.getAbi(em.de, fragment, StaticStore.addition, 0)
        val abilityicon = Interpret.getAbiid(em.de)
        if (ability.size > 0 || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            viewHolder.emabil.layoutManager = linearLayoutManager
            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity!!)
            viewHolder.emabil.adapter = adapterAbil
            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false)
        } else {
            viewHolder.emabil.visibility = View.GONE
        }
        aclevt.setText(t.tech[1].toString())
        actreat.setText(t.trea[3].toString())
        itfcryt.setText(t.alien.toString())
        cotccryt.setText(t.star.toString())
        for (j in godmaskt.indices) godmaskt[j].setText(t.gods[j].toString())
        listeners(viewHolder)
    }

    private fun listeners(viewHolder: ViewHolder) {
        val p = Pack.map[pid] ?: return
        val em = p.es[id]

        if (activity == null)
            return

        val t = BasisSet.current.t()
        val aclev: TextInputLayout = activity!!.findViewById(R.id.aclev)
        val actrea: TextInputLayout = activity!!.findViewById(R.id.actrea)
        val itfcry: TextInputLayout = activity!!.findViewById(R.id.itfcrytrea)
        val cotccry: TextInputLayout = activity!!.findViewById(R.id.cotccrytrea)
        val godmask = arrayOf<TextInputLayout>(activity!!.findViewById(R.id.godmask), activity!!.findViewById(R.id.godmask1), activity!!.findViewById(R.id.godmask2))
        val aclevt: TextInputEditText = activity!!.findViewById(R.id.aclevt)
        val actreat: TextInputEditText = activity!!.findViewById(R.id.actreat)
        val itfcryt: TextInputEditText = activity!!.findViewById(R.id.itfcrytreat)
        val cotccryt: TextInputEditText = activity!!.findViewById(R.id.cotccrytreat)
        val godmaskt = arrayOf<TextInputEditText>(activity!!.findViewById(R.id.godmaskt), activity!!.findViewById(R.id.godmaskt1), activity!!.findViewById(R.id.godmaskt2))
        viewHolder.name.setOnLongClickListener(OnLongClickListener {
            if (activity == null) return@OnLongClickListener false
            val clipboardManager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText(null, viewHolder.name.text)
            clipboardManager.setPrimaryClip(data)
            StaticStore.showShortMessage(activity, R.string.enem_info_copied)
            true
        })
        val reset = activity!!.findViewById<Button>(R.id.enemtreareset)
        viewHolder.enemmulti.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (viewHolder.enemmulti.text.toString() == "") {
                    multi = 100
                    multiply(viewHolder, em)
                } else {
                    multi = if (viewHolder.enemmulti.text.toString().toDouble() > Int.MAX_VALUE) Int.MAX_VALUE else Integer.valueOf(viewHolder.enemmulti.text.toString())
                    multiply(viewHolder, em)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        viewHolder.frse.setOnClickListener {
            if (fs == 0) {
                fs = 1
                retime(viewHolder, em)
                viewHolder.frse.text = activity!!.getString(R.string.unit_info_sec)
            } else {
                fs = 0
                retime(viewHolder, em)
                viewHolder.frse.text = activity!!.getString(R.string.unit_info_fr)
            }
        }
        viewHolder.enematkb.setOnClickListener {
            if (viewHolder.enematk.text.toString() == s.getAtk(em, multi)) {
                viewHolder.enematk.text = s.getDPS(em, multi)
                viewHolder.enematkb.text = activity!!.getString(R.string.unit_info_dps)
            } else {
                viewHolder.enematk.text = s.getAtk(em, multi)
                viewHolder.enematkb.text = activity!!.getString(R.string.unit_info_atk)
            }
        }
        viewHolder.enempreb.setOnClickListener { if (viewHolder.enempre.text.toString().endsWith("f")) viewHolder.enempre.text = s.getPre(em, 1) else viewHolder.enempre.text = s.getPre(em, 0) }
        viewHolder.enematktimeb.setOnClickListener { if (viewHolder.enematktime.text.toString().endsWith("f")) viewHolder.enematktime.text = s.getAtkTime(em, 1) else viewHolder.enematktime.text = s.getAtkTime(em, 0) }
        viewHolder.enempostb.setOnClickListener { if (viewHolder.enempost.text.toString().endsWith("f")) viewHolder.enempost.text = s.getPost(em, 1) else viewHolder.enempost.text = s.getPost(em, 0) }
        viewHolder.enemtbab.setOnClickListener { if (viewHolder.enemtba.text.toString().endsWith("f")) viewHolder.enemtba.text = s.getTBA(em, 1) else viewHolder.enemtba.text = s.getTBA(em, 0) }
        aclevt.setSelection(aclevt.text?.length ?: 0)
        actreat.setSelection(actreat.text?.length ?: 0)
        itfcryt.setSelection(itfcryt.text?.length ?: 0)
        cotccryt.setSelection(cotccryt.text?.length ?: 0)
        for (tiet in godmaskt) tiet.setSelection(tiet.text?.length ?: 0)
        aclevt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 30 || s.toString().toInt() <= 0) {
                        if (aclev.isHelperTextEnabled) {
                            aclev.isHelperTextEnabled = false
                            aclev.isErrorEnabled = true
                            aclev.error = activity!!.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (aclev.isErrorEnabled) {
                            aclev.error = null
                            aclev.isErrorEnabled = false
                            aclev.isHelperTextEnabled = true
                            aclev.setHelperTextColor(ColorStateList(states, color))
                            aclev.helperText = "1~30"
                        }
                    }
                } else {
                    if (aclev.isErrorEnabled) {
                        aclev.error = null
                        aclev.isErrorEnabled = false
                        aclev.isHelperTextEnabled = true
                        aclev.setHelperTextColor(ColorStateList(states, color))
                        aclev.helperText = "1~30"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() in 1..30) {
                        val lev = text.toString().toInt()
                        t.tech[1] = lev
                        viewHolder.enemdrop.text = s.getDrop(em, t)
                    }
                } else {
                    t.tech[1] = 1
                    viewHolder.enemdrop.text = s.getDrop(em, t)
                }
            }
        })
        actreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 300) {
                        if (actrea.isHelperTextEnabled) {
                            actrea.isHelperTextEnabled = false
                            actrea.isErrorEnabled = true
                            actrea.error = activity!!.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (actrea.isErrorEnabled) {
                            actrea.error = null
                            actrea.isErrorEnabled = false
                            actrea.isHelperTextEnabled = true
                            actrea.setHelperTextColor(ColorStateList(states, color))
                            actrea.helperText = "0~300"
                        }
                    }
                } else {
                    if (actrea.isErrorEnabled) {
                        actrea.error = null
                        actrea.isErrorEnabled = false
                        actrea.isHelperTextEnabled = true
                        actrea.setHelperTextColor(ColorStateList(states, color))
                        actrea.helperText = "0~300"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 300) {
                        val trea = text.toString().toInt()
                        t.trea[3] = trea
                        viewHolder.enemdrop.text = s.getDrop(em, t)
                    }
                } else {
                    t.trea[3] = 0
                    viewHolder.enemdrop.text = s.getDrop(em, t)
                }
            }
        })
        itfcryt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 600) {
                        if (itfcry.isHelperTextEnabled) {
                            itfcry.isHelperTextEnabled = false
                            itfcry.isErrorEnabled = true
                            itfcry.error = activity!!.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (itfcry.isErrorEnabled) {
                            itfcry.error = null
                            itfcry.isErrorEnabled = false
                            itfcry.isHelperTextEnabled = true
                            itfcry.setHelperTextColor(ColorStateList(states, color))
                            itfcry.helperText = "0~600"
                        }
                    }
                } else {
                    if (itfcry.isErrorEnabled) {
                        itfcry.error = null
                        itfcry.isErrorEnabled = false
                        itfcry.isHelperTextEnabled = true
                        itfcry.setHelperTextColor(ColorStateList(states, color))
                        itfcry.helperText = "0~600"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 600) {
                        t.alien = text.toString().toInt()
                        viewHolder.enemhp.text = s.getHP(em, multi)
                        if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                            viewHolder.enematk.text = s.getDPS(em, multi)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, multi)
                        }
                    }
                } else {
                    t.alien = 0
                    viewHolder.enemhp.text = s.getHP(em, multi)
                    if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                        viewHolder.enematk.text = s.getDPS(em, multi)
                    } else {
                        viewHolder.enematk.text = s.getAtk(em, multi)
                    }
                }
            }
        })
        cotccryt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (s.toString().toInt() > 1500) {
                        if (cotccry.isHelperTextEnabled) {
                            cotccry.isHelperTextEnabled = false
                            cotccry.isErrorEnabled = true
                            cotccry.error = activity!!.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (cotccry.isErrorEnabled) {
                            cotccry.error = null
                            cotccry.isErrorEnabled = false
                            cotccry.isHelperTextEnabled = true
                            cotccry.setHelperTextColor(ColorStateList(states, color))
                            cotccry.helperText = "0~1500"
                        }
                    }
                } else {
                    if (cotccry.isErrorEnabled) {
                        cotccry.error = null
                        cotccry.isErrorEnabled = false
                        cotccry.isHelperTextEnabled = true
                        cotccry.setHelperTextColor(ColorStateList(states, color))
                        cotccry.helperText = "0~1500"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if (text.toString().isNotEmpty()) {
                    if (text.toString().toInt() <= 1500) {
                        t.star = text.toString().toInt()
                        viewHolder.enemhp.text = s.getHP(em, multi)
                        if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                            viewHolder.enematk.text = s.getDPS(em, multi)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, multi)
                        }
                    }
                } else {
                    t.star = 0
                    viewHolder.enemhp.text = s.getHP(em, multi)
                    if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                        viewHolder.enematk.text = s.getDPS(em, multi)
                    } else {
                        viewHolder.enematk.text = s.getAtk(em, multi)
                    }
                }
            }
        })
        for (i in godmaskt.indices) {
            godmaskt[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty()) {
                        if (s.toString().toInt() > 100) {
                            if (godmask[i].isHelperTextEnabled) {
                                godmask[i].isHelperTextEnabled = false
                                godmask[i].isErrorEnabled = true
                                godmask[i].error = activity!!.getString(R.string.treasure_invalid)
                            }
                        } else {
                            if (godmask[i].isErrorEnabled) {
                                godmask[i].error = null
                                godmask[i].isErrorEnabled = false
                                godmask[i].isHelperTextEnabled = true
                                godmask[i].setHelperTextColor(ColorStateList(states, color))
                                godmask[i].helperText = "0~100"
                            }
                        }
                    } else {
                        if (godmask[i].isErrorEnabled) {
                            godmask[i].error = null
                            godmask[i].isErrorEnabled = false
                            godmask[i].isHelperTextEnabled = true
                            godmask[i].setHelperTextColor(ColorStateList(states, color))
                            godmask[i].helperText = "0~100"
                        }
                    }
                }

                override fun afterTextChanged(text: Editable) {
                    if (text.toString().isNotEmpty()) {
                        if (text.toString().toInt() <= 100) {
                            t.gods[i] = text.toString().toInt()
                            viewHolder.enemhp.text = s.getHP(em, multi)
                            if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                                viewHolder.enematk.text = s.getDPS(em, multi)
                            } else {
                                viewHolder.enematk.text = s.getAtk(em, multi)
                            }
                        }
                    } else {
                        t.gods[i] = 0
                        viewHolder.enemhp.text = s.getHP(em, multi)
                        if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                            viewHolder.enematk.text = s.getDPS(em, multi)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, multi)
                        }
                    }
                }
            })
        }
        reset.setOnClickListener {
            t.tech[1] = 30
            t.trea[3] = 300
            t.alien = 600
            t.star = 1500
            for (i in t.gods.indices) t.gods[i] = 100
            aclevt.setText(t.tech[1].toString())
            actreat.setText(t.trea[3].toString())
            itfcryt.setText(t.alien.toString())
            cotccryt.setText(t.star.toString())
            for (i in t.gods.indices) godmaskt[i].setText(t.gods[i].toString())
            viewHolder.enemhp.text = s.getHP(em, multi)
            if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                viewHolder.enematk.text = s.getDPS(em, multi)
            } else {
                viewHolder.enematk.text = s.getAtk(em, multi)
            }
            viewHolder.enemdrop.text = s.getDrop(em, t)
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.eneminfname)
        var frse: Button = itemView.findViewById(R.id.eneminffrse)
        var enemid: TextView = itemView.findViewById(R.id.eneminfidr)
        var enemicon: ImageView = itemView.findViewById(R.id.eneminficon)
        var enemhp: TextView = itemView.findViewById(R.id.eneminfhpr)
        var enemhb: TextView = itemView.findViewById(R.id.eneminfhbr)
        var enemmulti: EditText = itemView.findViewById(R.id.eneminfmultir)
        var enematkb: Button = itemView.findViewById(R.id.eneminfatk)
        var enematk: TextView = itemView.findViewById(R.id.eneminfatkr)
        var enematktimeb: Button = itemView.findViewById(R.id.eneminfatktime)
        var enematktime: TextView = itemView.findViewById(R.id.eneminfatktimer)
        var enemabilt: TextView = itemView.findViewById(R.id.eneminfabiltr)
        var enempreb: Button = itemView.findViewById(R.id.eneminfpre)
        var enempre: TextView = itemView.findViewById(R.id.eneminfprer)
        var enempostb: Button = itemView.findViewById(R.id.eneminfpost)
        var enempost: TextView = itemView.findViewById(R.id.eneminfpostr)
        var enemtbab: Button = itemView.findViewById(R.id.eneminftba)
        var enemtba: TextView = itemView.findViewById(R.id.eneminftbar)
        var enemtrait: TextView = itemView.findViewById(R.id.eneminftraitr)
        var enematkt: TextView = itemView.findViewById(R.id.eneminfatktr)
        var enemdrop: TextView = itemView.findViewById(R.id.eneminfdropr)
        var enemrange: TextView = itemView.findViewById(R.id.eneminfranger)
        var enembarrier: TextView = itemView.findViewById(R.id.eneminfbarrierr)
        var enemspd: TextView = itemView.findViewById(R.id.eneminfspdr)
        var none: TextView = itemView.findViewById(R.id.eneminfnone)
        var emabil: RecyclerView = itemView.findViewById(R.id.eneminfabillist)

    }

    private fun multiply(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enemhp.text = s.getHP(em, multi)
        viewHolder.enematk.text = s.getAtk(em, multi)
    }

    private fun retime(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enematktime.text = s.getAtkTime(em, fs)
        viewHolder.enempre.text = s.getPre(em, fs)
        viewHolder.enempost.text = s.getPost(em, fs)
        viewHolder.enemtba.text = s.getTBA(em, fs)
        val shared = activity!!.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        var language = StaticStore.lang[shared.getInt("Language", 0)]
        if (language == "") {
            language = Resources.getSystem().configuration.locales[0].language
        }
        val proc: List<String>
        proc = if (language == "ko" || language == "ja") {
            Interpret.getProc(activity, em.de, 1, fs)
        } else {
            Interpret.getProc(activity, em.de, 0, fs)
        }

        val ability = Interpret.getAbi(em.de, fragment, StaticStore.addition, 0)

        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.size > 0 || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE
            val linearLayoutManager = LinearLayoutManager(activity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            viewHolder.emabil.layoutManager = linearLayoutManager
            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity!!)
            viewHolder.emabil.adapter = adapterAbil
            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false)
        }
    }

    companion object {
        private fun getAttributeColor(context: Context, attributeId: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(attributeId, typedValue, true)
            val colorRes = typedValue.resourceId
            var color = -1
            try {
                color = ContextCompat.getColor(context, colorRes)
            } catch (e: NotFoundException) {
                e.printStackTrace()
            }
            return color
        }
    }
}