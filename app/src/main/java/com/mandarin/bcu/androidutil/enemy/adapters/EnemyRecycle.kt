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
import com.mandarin.bcu.androidutil.Interpret
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.adapter.AdapterAbil
import common.battle.BasisSet
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Enemy

class EnemyRecycle : RecyclerView.Adapter<EnemyRecycle.ViewHolder> {
    private val fragment = arrayOf(arrayOf("Immune to "), arrayOf(""))
    private var activity: Activity
    private var fs = 0
    private var multiplication = 100
    private var attackMultiplication = 100
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

    constructor(activity: Activity, multi: Int, attackMultiplication: Int, data: Identifier<AbEnemy>) {
        this.activity = activity
        this.multiplication = multi
        this.attackMultiplication = attackMultiplication
        s = GetStrings(activity)
        color = intArrayOf(
                StaticStore.getAttributeColor(activity, R.attr.TextPrimary)
        )
        this.data = data
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pack = itemView.findViewById<Button>(R.id.eneminfpack)
        val enemyPack = itemView.findViewById<TextView>(R.id.eneminfpackr)
        val name = itemView.findViewById<TextView>(R.id.eneminfname)
        val unitSwitch = itemView.findViewById<Button>(R.id.eneminffrse)
        val enemyId = itemView.findViewById<TextView>(R.id.eneminfidr)
        val enemyIcon = itemView.findViewById<ImageView>(R.id.eneminficon)
        val enemhp = itemView.findViewById<TextView>(R.id.eneminfhpr)
        val enemhb = itemView.findViewById<TextView>(R.id.eneminfhbr)
        val enemmulti = itemView.findViewById<EditText>(R.id.eneminfmultir)
        val enematkb = itemView.findViewById<Button>(R.id.eneminfatk)
        val enematk = itemView.findViewById<TextView>(R.id.eneminfatkr)
        val enematktimeb = itemView.findViewById<Button>(R.id.eneminfatktime)
        val enematktime = itemView.findViewById<TextView>(R.id.eneminfatktimer)
        val enemabilt = itemView.findViewById<TextView>(R.id.eneminfabiltr)
        val enempreb = itemView.findViewById<Button>(R.id.eneminfpre)
        val enempre = itemView.findViewById<TextView>(R.id.eneminfprer)
        val enempostb = itemView.findViewById<Button>(R.id.eneminfpost)
        val enempost = itemView.findViewById<TextView>(R.id.eneminfpostr)
        val enemtbab = itemView.findViewById<Button>(R.id.eneminftba)
        val enemtba = itemView.findViewById<TextView>(R.id.eneminftbar)
        val enemtrait = itemView.findViewById<TextView>(R.id.eneminftraitr)
        val enematkt = itemView.findViewById<TextView>(R.id.eneminfatktr)
        val enemdrop = itemView.findViewById<TextView>(R.id.eneminfdropr)
        val enemrange = itemView.findViewById<TextView>(R.id.eneminfranger)
        val enembarrier = itemView.findViewById<TextView>(R.id.eneminfbarrierr)
        val enemspd = itemView.findViewById<TextView>(R.id.eneminfspdr)
        val none = itemView.findViewById<TextView>(R.id.eneminfnone)
        val emabil = itemView.findViewById<RecyclerView>(R.id.eneminfabillist)
        val enemamulti = itemView.findViewById<EditText>(R.id.eneminfamultir)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.enemy_table, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val em = Identifier.get(data) ?: return

        if(em !is Enemy)
            return

        val t = BasisSet.current().t()

        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        if (shared.getBoolean("frame", true)) {
            fs = 0
            viewHolder.unitSwitch.text = activity.getString(R.string.unit_info_fr)
        } else {
            fs = 1
            viewHolder.unitSwitch.text = activity.getString(R.string.unit_info_sec)
        }

        val aclev = activity.findViewById<TextInputLayout>(R.id.aclev)
        val actrea = activity.findViewById<TextInputLayout>(R.id.actrea)
        val itfcry = activity.findViewById<TextInputLayout>(R.id.itfcrytrea)
        val cotccry = activity.findViewById<TextInputLayout>(R.id.cotccrytrea)

        val godmask = arrayOf<TextInputLayout>(activity.findViewById(R.id.godmask), activity.findViewById(R.id.godmask1), activity.findViewById(R.id.godmask2))

        val aclevt = activity.findViewById<TextInputEditText>(R.id.aclevt)
        val actreat = activity.findViewById<TextInputEditText>(R.id.actreat)
        val itfcryt = activity.findViewById<TextInputEditText>(R.id.itfcrytreat)
        val cotccryt = activity.findViewById<TextInputEditText>(R.id.cotccrytreat)

        val godmaskt = arrayOf<TextInputEditText>(activity.findViewById(R.id.godmaskt), activity.findViewById(R.id.godmaskt1), activity.findViewById(R.id.godmaskt2))

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

        viewHolder.name.text = MultiLangCont.get(em) ?: em.names.toString()

        val name = StaticStore.trio(em.id.id)

        viewHolder.enemyId.text = name

        val ratio = 32f / 32f

        val img = em.anim?.edi?.img

        var b: Bitmap? = null

        if (img != null)
            b = img.bimg() as Bitmap

        viewHolder.enemyPack.text = s.getPackName(em.id, isRaw)
        viewHolder.enemyIcon.setImageBitmap(StaticStore.getResizeb(b, activity, 85f * ratio, 32f * ratio))
        viewHolder.enemhp.text = s.getHP(em, multiplication)
        viewHolder.enemhb.text = s.getHB(em)
        viewHolder.enemmulti.setText(multiplication.toString())
        viewHolder.enemamulti.setText(attackMultiplication.toString())
        viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
        viewHolder.enematktime.text = s.getAtkTime(em, fs)
        viewHolder.enemabilt.text = s.getAbilT(em)
        viewHolder.enempre.text = s.getPre(em, fs)
        viewHolder.enempost.text = s.getPost(em, fs)
        viewHolder.enemtba.text = s.getTBA(em, fs)
        viewHolder.enemtrait.text = s.getTrait(em, activity)
        viewHolder.enematkt.text = s.getSimu(em)
        viewHolder.enemdrop.text = s.getDrop(em, t)
        viewHolder.enemrange.text = s.getRange(em)
        viewHolder.enembarrier.text = s.getBarrier(em)
        viewHolder.enemspd.text = s.getSpd(em)

        val proc: List<String> = Interpret.getProc(em.de, fs == 1, true, arrayOf(multiplication / 100.0, attackMultiplication / 100.0).toDoubleArray(), activity)
        val ability = Interpret.getAbi(em.de, fragment, 0, this.activity)
        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE

            val linearLayoutManager = LinearLayoutManager(this.activity)

            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

            viewHolder.emabil.layoutManager = linearLayoutManager

            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)

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

        val t = BasisSet.current().t()

        val aclev = activity.findViewById<TextInputLayout>(R.id.aclev)
        val actrea = activity.findViewById<TextInputLayout>(R.id.actrea)
        val itfcry = activity.findViewById<TextInputLayout>(R.id.itfcrytrea)
        val cotccry = activity.findViewById<TextInputLayout>(R.id.cotccrytrea)

        val godmask = arrayOf<TextInputLayout>(activity.findViewById(R.id.godmask), activity.findViewById(R.id.godmask1), activity.findViewById(R.id.godmask2))

        val aclevt = activity.findViewById<TextInputEditText>(R.id.aclevt)
        val actreat = activity.findViewById<TextInputEditText>(R.id.actreat)
        val itfcryt = activity.findViewById<TextInputEditText>(R.id.itfcrytreat)
        val cotccryt = activity.findViewById<TextInputEditText>(R.id.cotccrytreat)

        val godmaskt = arrayOf<TextInputEditText>(activity.findViewById(R.id.godmaskt), activity.findViewById(R.id.godmaskt1), activity.findViewById(R.id.godmaskt2))

        viewHolder.pack.setOnClickListener {
            isRaw = !isRaw

            viewHolder.enemyPack.text = s.getPackName(em.id, isRaw)
        }

        viewHolder.name.setOnLongClickListener {
            val clipboardManager =
                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val data = ClipData.newPlainText(null, viewHolder.name.text)

            clipboardManager.setPrimaryClip(data)

            StaticStore.showShortMessage(activity, R.string.enem_info_copied)

            true
        }

        val reset = activity.findViewById<Button>(R.id.enemtreareset)

        viewHolder.enemmulti.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (viewHolder.enemmulti.text.toString() == "") {
                    multiplication = 100
                    multiply(viewHolder, em)
                } else {
                    multiplication = if (viewHolder.enemmulti.text.toString().toDouble() > Int.MAX_VALUE)
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
                    attackMultiplication = 100
                    multiply(viewHolder, em)
                } else {
                    attackMultiplication = if (viewHolder.enemamulti.text.toString().toDouble() > Int.MAX_VALUE)
                        Int.MAX_VALUE
                    else
                        Integer.valueOf(viewHolder.enemamulti.text.toString())

                    multiply(viewHolder, em)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        viewHolder.unitSwitch.setOnClickListener {
            if (fs == 0) {
                fs = 1
                retime(viewHolder, em)
                viewHolder.unitSwitch.text = activity.getString(R.string.unit_info_sec)
            } else {
                fs = 0
                retime(viewHolder, em)
                viewHolder.unitSwitch.text = activity.getString(R.string.unit_info_fr)
            }
        }

        viewHolder.enematkb.setOnClickListener {
            if (viewHolder.enematkb.text == activity.getString(R.string.unit_info_atk)) {
                viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
                viewHolder.enematkb.text = activity.getString(R.string.unit_info_dps)
            } else {
                viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
                viewHolder.enematkb.text = activity.getString(R.string.unit_info_atk)
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
                            aclev.error = activity.getString(R.string.treasure_invalid)
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
                            actrea.error = activity.getString(R.string.treasure_invalid)
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
                            itfcry.error = activity.getString(R.string.treasure_invalid)
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
                        viewHolder.enemhp.text = s.getHP(em, multiplication)
                        if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                            viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
                        }
                    }
                } else {
                    t.alien = 0
                    viewHolder.enemhp.text = s.getHP(em, multiplication)
                    if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                        viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
                    } else {
                        viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
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
                            cotccry.error = activity.getString(R.string.treasure_invalid)
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
                        viewHolder.enemhp.text = s.getHP(em, multiplication)
                        if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                            viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
                        }
                    }
                } else {
                    t.star = 0
                    viewHolder.enemhp.text = s.getHP(em, multiplication)
                    if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                        viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
                    } else {
                        viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
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
                                godmask[i].error = activity.getString(R.string.treasure_invalid)
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
                            viewHolder.enemhp.text = s.getHP(em, multiplication)
                            if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                                viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
                            } else {
                                viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
                            }
                        }
                    } else {
                        t.gods[i] = 0
                        viewHolder.enemhp.text = s.getHP(em, multiplication)
                        if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                            viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
                        } else {
                            viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
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

            viewHolder.enemhp.text = s.getHP(em, multiplication)

            if (viewHolder.enematkb.text.toString() == activity.getString(R.string.unit_info_dps)) {
                viewHolder.enematk.text = s.getDPS(em, attackMultiplication)
            } else {
                viewHolder.enematk.text = s.getAtk(em, attackMultiplication)
            }

            viewHolder.enemdrop.text = s.getDrop(em, t)
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    private fun multiply(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enemhp.text = s.getHP(em, multiplication)
        viewHolder.enematk.text = s.getAtk(em, attackMultiplication)

        val proc: List<String> = Interpret.getProc(em.de, fs == 1, true, arrayOf(multiplication / 100.0, attackMultiplication / 100.0).toDoubleArray(), activity)

        val ability = Interpret.getAbi(em.de, fragment, 0, activity)

        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE

            val linearLayoutManager = LinearLayoutManager(activity)

            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

            viewHolder.emabil.layoutManager = linearLayoutManager

            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)

            viewHolder.emabil.adapter = adapterAbil

            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false)
        }
    }

    private fun retime(viewHolder: ViewHolder, em: Enemy) {
        viewHolder.enematktime.text = s.getAtkTime(em, fs)
        viewHolder.enempre.text = s.getPre(em, fs)
        viewHolder.enempost.text = s.getPost(em, fs)
        viewHolder.enemtba.text = s.getTBA(em, fs)

        val proc: List<String> = Interpret.getProc(em.de, fs == 1, true, arrayOf(multiplication / 100.0, attackMultiplication / 100.0).toDoubleArray(), activity)

        val ability = Interpret.getAbi(em.de, fragment, 0, activity)

        val abilityicon = Interpret.getAbiid(em.de)

        if (ability.isNotEmpty() || proc.isNotEmpty()) {
            viewHolder.none.visibility = View.GONE

            val linearLayoutManager = LinearLayoutManager(activity)

            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

            viewHolder.emabil.layoutManager = linearLayoutManager

            val adapterAbil = AdapterAbil(ability, proc, abilityicon, activity)

            viewHolder.emabil.adapter = adapterAbil

            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false)
        }
    }
}