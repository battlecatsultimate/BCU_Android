package com.mandarin.bcu.androidutil.enemy.adapters

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
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Enemy

class EnemyRecycle : RecyclerView.Adapter<EnemyRecycle.ViewHolder> {
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private var activity: Activity?
    private var fs = 0
    private var multi = 100
    private var amulti = 100
    private var s: GetStrings
    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private var color: IntArray
    private val data: Identifier<AbEnemy>

    private var isRaw = false

    constructor(activity: Activity, data: Identifier<AbEnemy>) {
        this.activity = activity
        s = GetStrings(activity)
        color = intArrayOf(
                StaticStore.getAttributeColor(activity, R.attr.TextPrimary)
        )
        this.data = data
    }

    constructor(activity: Activity, multi: Int, amulti: Int, data: Identifier<AbEnemy>) {
        this.activity = activity
        this.multi = multi
        this.amulti = amulti
        s = GetStrings(activity)
        color = intArrayOf(
                StaticStore.getAttributeColor(activity, R.attr.TextPrimary)
        )
        this.data = data
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pack: Button = itemView.findViewById(R.id.eneminfpack)
        val enempack: TextView = itemView.findViewById(R.id.eneminfpackr)
        val name: TextView = itemView.findViewById(R.id.eneminfname)
        val frse: Button = itemView.findViewById(R.id.eneminffrse)
        val enemid: TextView = itemView.findViewById(R.id.eneminfidr)
        val enemicon: ImageView = itemView.findViewById(R.id.eneminficon)
        val enemhp: TextView = itemView.findViewById(R.id.eneminfhpr)
        val enemhb: TextView = itemView.findViewById(R.id.eneminfhbr)
        val enemmulti: EditText = itemView.findViewById(R.id.eneminfmultir)
        val enematkb: Button = itemView.findViewById(R.id.eneminfatk)
        val enematk: TextView = itemView.findViewById(R.id.eneminfatkr)
        val enematktimeb: Button = itemView.findViewById(R.id.eneminfatktime)
        val enematktime: TextView = itemView.findViewById(R.id.eneminfatktimer)
        val enemabilt: TextView = itemView.findViewById(R.id.eneminfabiltr)
        val enempreb: Button = itemView.findViewById(R.id.eneminfpre)
        val enempre: TextView = itemView.findViewById(R.id.eneminfprer)
        val enempostb: Button = itemView.findViewById(R.id.eneminfpost)
        val enempost: TextView = itemView.findViewById(R.id.eneminfpostr)
        val enemtbab: Button = itemView.findViewById(R.id.eneminftba)
        val enemtba: TextView = itemView.findViewById(R.id.eneminftbar)
        val enemtrait: TextView = itemView.findViewById(R.id.eneminftraitr)
        val enematkt: TextView = itemView.findViewById(R.id.eneminfatktr)
        val enemdrop: TextView = itemView.findViewById(R.id.eneminfdropr)
        val enemrange: TextView = itemView.findViewById(R.id.eneminfranger)
        val enembarrier: TextView = itemView.findViewById(R.id.eneminfbarrierr)
        val enemspd: TextView = itemView.findViewById(R.id.eneminfspdr)
        val none: TextView = itemView.findViewById(R.id.eneminfnone)
        val emabil: RecyclerView = itemView.findViewById(R.id.eneminfabillist)
        val enemamulti: EditText = itemView.findViewById(R.id.eneminfamultir)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.enemy_table, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val em = Identifier.get(data) ?: return
        val ac = activity ?: return

        if(em !is Enemy)
            return

        val t = BasisSet.current().t()

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

        viewHolder.name.text = MultiLangCont.get(em) ?: em.name

        val name = StaticStore.trio(em.id.id)

        viewHolder.enemid.text = name

        val ratio = 32f / 32f

        val img = em.anim?.edi?.img

        var b: Bitmap? = null

        if (img != null)
            b = img.bimg() as Bitmap

        viewHolder.enempack.text = s.getPackName(em.id, isRaw)
        viewHolder.enemicon.setImageBitmap(StaticStore.getResizeb(b, ac, 85f * ratio, 32f * ratio))
        viewHolder.enemhp.text = s.getHP(em, multi)
        viewHolder.enemhb.text = s.getHB(em)
        viewHolder.enemmulti.setText(multi.toString())
        viewHolder.enemamulti.setText(amulti.toString())
        viewHolder.enematk.text = s.getAtk(em, amulti)
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

        val proc: List<String> = Interpret.getProc(em.de, fs == 1, false, multi / 100.0)
        val ability = Interpret.getAbi(em.de, fragment, StaticStore.addition, 0)
        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
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

        for (j in godmaskt.indices)
            godmaskt[j].setText(t.gods[j].toString())

        listeners(viewHolder)
    }

    private fun listeners(viewHolder: ViewHolder) {
        val em = data.get() ?: return

        if(em !is Enemy)
            return

        val ac = activity ?: return

        val t = BasisSet.current().t()

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

        viewHolder.pack.setOnClickListener {
            isRaw = !isRaw

            viewHolder.enempack.text = s.getPackName(em.id, isRaw)
        }

        viewHolder.name.setOnLongClickListener(OnLongClickListener {
            if (activity == null)
                return@OnLongClickListener false

            val clipboardManager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val data = ClipData.newPlainText(null, viewHolder.name.text)

            clipboardManager.setPrimaryClip(data)

            StaticStore.showShortMessage(ac, R.string.enem_info_copied)

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
                    multi = if (viewHolder.enemmulti.text.toString().toDouble() > Int.MAX_VALUE)
                        Int.MAX_VALUE
                    else
                        Integer.valueOf(viewHolder.enemmulti.text.toString())

                    multiply(viewHolder, em)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        viewHolder.enemamulti.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (viewHolder.enemamulti.text.toString() == "") {
                    amulti = 100
                    multiply(viewHolder, em)
                } else {
                    amulti = if (viewHolder.enemamulti.text.toString().toDouble() > Int.MAX_VALUE)
                        Int.MAX_VALUE
                    else
                        Integer.valueOf(viewHolder.enemamulti.text.toString())

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
            if (viewHolder.enematkb.text == activity!!.getString(R.string.unit_info_atk)) {
                viewHolder.enematk.text = s.getDPS(em, amulti)
                viewHolder.enematkb.text = activity!!.getString(R.string.unit_info_dps)
            } else {
                viewHolder.enematk.text = s.getAtk(em, amulti)
                viewHolder.enematkb.text = activity!!.getString(R.string.unit_info_atk)
            }
        }

        viewHolder.enempreb.setOnClickListener {
            if (viewHolder.enempre.text.toString().endsWith("f"))
                viewHolder.enempre.text = s.getPre(em, 1)
            else
                viewHolder.enempre.text = s.getPre(em, 0)
        }

        viewHolder.enematktimeb.setOnClickListener {
            if (viewHolder.enematktime.text.toString().endsWith("f"))
            viewHolder.enematktime.text = s.getAtkTime(em, 1)
            else
                viewHolder.enematktime.text = s.getAtkTime(em, 0)
        }

        viewHolder.enempostb.setOnClickListener {
            if (viewHolder.enempost.text.toString().endsWith("f"))
                viewHolder.enempost.text = s.getPost(em, 1)
            else
                viewHolder.enempost.text = s.getPost(em, 0)
        }

        viewHolder.enemtbab.setOnClickListener {
            if (viewHolder.enemtba.text.toString().endsWith("f"))
                viewHolder.enemtba.text = s.getTBA(em, 1)
            else
                viewHolder.enemtba.text = s.getTBA(em, 0)
        }

        aclevt.setSelection(aclevt.text?.length ?: 0)
        actreat.setSelection(actreat.text?.length ?: 0)

        itfcryt.setSelection(itfcryt.text?.length ?: 0)

        cotccryt.setSelection(cotccryt.text?.length ?: 0)

        for (tiet in godmaskt)
            tiet.setSelection(tiet.text?.length ?: 0)

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
                            viewHolder.enematk.text = s.getDPS(em, amulti)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, amulti)
                        }
                    }
                } else {
                    t.alien = 0
                    viewHolder.enemhp.text = s.getHP(em, multi)
                    if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                        viewHolder.enematk.text = s.getDPS(em, amulti)
                    } else {
                        viewHolder.enematk.text = s.getAtk(em, amulti)
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
                            viewHolder.enematk.text = s.getDPS(em, amulti)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, amulti)
                        }
                    }
                } else {
                    t.star = 0
                    viewHolder.enemhp.text = s.getHP(em, multi)
                    if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                        viewHolder.enematk.text = s.getDPS(em, amulti)
                    } else {
                        viewHolder.enematk.text = s.getAtk(em, amulti)
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
                                viewHolder.enematk.text = s.getDPS(em, amulti)
                            } else {
                                viewHolder.enematk.text = s.getAtk(em, amulti)
                            }
                        }
                    } else {
                        t.gods[i] = 0
                        viewHolder.enemhp.text = s.getHP(em, multi)
                        if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                            viewHolder.enematk.text = s.getDPS(em, amulti)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, amulti)
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

            for (i in t.gods.indices)
                t.gods[i] = 100

            aclevt.setText(t.tech[1].toString())
            actreat.setText(t.trea[3].toString())

            itfcryt.setText(t.alien.toString())

            cotccryt.setText(t.star.toString())

            for (i in t.gods.indices)
                godmaskt[i].setText(t.gods[i].toString())

            viewHolder.enemhp.text = s.getHP(em, multi)

            if (viewHolder.enematkb.text.toString() == activity!!.getString(R.string.unit_info_dps)) {
                viewHolder.enematk.text = s.getDPS(em, amulti)
            } else {
                viewHolder.enematk.text = s.getAtk(em, amulti)
            }

            viewHolder.enemdrop.text = s.getDrop(em, t)
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    private fun multiply(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enemhp.text = s.getHP(em, multi)
        viewHolder.enematk.text = s.getAtk(em, amulti)

        val proc: List<String> = Interpret.getProc(em.de, fs == 1, false, multi / 100.0)

        val ability = Interpret.getAbi(em.de, fragment, StaticStore.addition, 0)

        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE

            val linearLayoutManager = LinearLayoutManager(activity)

            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

            viewHolder.emabil.layoutManager = linearLayoutManager

            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity!!)

            viewHolder.emabil.adapter = adapterAbil

            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false)
        }
    }

    private fun retime(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enematktime.text = s.getAtkTime(em, fs)
        viewHolder.enempre.text = s.getPre(em, fs)
        viewHolder.enempost.text = s.getPost(em, fs)
        viewHolder.enemtba.text = s.getTBA(em, fs)

        val proc: List<String> = Interpret.getProc(em.de, fs == 1, false, multi / 100.0)

        val ability = Interpret.getAbi(em.de, fragment, StaticStore.addition, 0)

        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE

            val linearLayoutManager = LinearLayoutManager(activity)

            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

            viewHolder.emabil.layoutManager = linearLayoutManager

            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity!!)

            viewHolder.emabil.adapter = adapterAbil

            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false)
        }
    }
}