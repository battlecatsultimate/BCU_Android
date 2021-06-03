package com.mandarin.bcu.androidutil.lineup.adapters

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.battle.BasisSet
import common.battle.Treasure
import common.util.Data

class LUConstruction : Fragment() {

    private var initialized = false
    private var editable = true
    private var destroyed = false

    private val layoutid = intArrayOf(R.id.castlelev, R.id.slowlev, R.id.walllev, R.id.stoplev, R.id.waterlev, R.id.zombielev, R.id.breakerlev, R.id.curselev)

    private val textid = intArrayOf(R.id.castlelevt, R.id.slowlevt, R.id.walllevt, R.id.stoplevt, R.id.waterlevt, R.id.zombielevt, R.id.breakerlevt, R.id.curselevt)

    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))

    private var color: IntArray? = null

    private val handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable { }

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.lineup_construction, group, false)

        if (context == null) return view

        color = intArrayOf(StaticStore.getAttributeColor(requireContext(), R.attr.TextPrimary))

        listeners(view)

        runnable = object : Runnable {
            override fun run() {
                if (StaticStore.updateConst) {
                    initialized = false

                    val texts = arrayOfNulls<TextInputEditText>(Data.BASE_TOT)

                    val text = view.findViewById<TextInputEditText>(R.id.constlevt)

                    if (valuesAllSame())
                        text.setText(BasisSet.current().t().bslv[0].toString())

                    val vals = BasisSet.current().t().bslv

                    for (i in vals.indices) {
                        texts[i] = view.findViewById(textid[i])
                        texts[i]?.setText(vals[i].toString())
                    }

                    initialized = true

                    StaticStore.updateConst = false
                }

                if (!destroyed)
                    handler.postDelayed(this, 50)
            }
        }

        handler.postDelayed(runnable, 50)

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
        handler.removeCallbacks(runnable)
    }

    private fun listeners(view: View) {
        val constructions = arrayOfNulls<TextInputLayout>(Data.BASE_TOT)
        val construction = view.findViewById<TextInputLayout>(R.id.constlev)
        val text = view.findViewById<TextInputEditText>(R.id.constlevt)
        val texts = arrayOfNulls<TextInputEditText>(Data.BASE_TOT)

        for (i in layoutid.indices) {
            constructions[i] = view.findViewById(layoutid[i])
            texts[i] = view.findViewById(textid[i])
        }

        construction.setHelperTextColor(ColorStateList(states, color))
        setListenerforTextInputLayouts(constructions)

        if (valuesAllSame())
            text.setText(BasisSet.current().t().bslv[0].toString())

        val vals = BasisSet.current().t().bslv

        for (i in vals.indices) {
            texts[i]?.setText(vals[i].toString())
        }

        text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (Integer.parseInt(s.toString()) > 30 || Integer.parseInt(s.toString()) < 1) {
                        if (construction.isHelperTextEnabled) {
                            construction.isHelperTextEnabled = false
                            construction.isErrorEnabled = true
                            construction.error = context!!.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if (construction.isErrorEnabled) {
                            construction.error = null
                            construction.isErrorEnabled = false
                            construction.isHelperTextEnabled = true
                            construction.setHelperTextColor(ColorStateList(states, color))
                            construction.helperText = "1~30 Lv."
                        }
                    }
                } else {
                    if (construction.isErrorEnabled) {
                        construction.error = null
                        construction.isErrorEnabled = false
                        construction.isHelperTextEnabled = true
                        construction.setHelperTextColor(ColorStateList(states, color))
                        construction.helperText = "1~30 Lv."
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty()) {
                    val t = BasisSet.current().t()

                    editable = false

                    if (Integer.parseInt(s.toString()) in 1..30) {
                        val `val` = Integer.parseInt(s.toString())

                        for (i in texts.indices) {
                            t.bslv[i] = `val`
                            texts[i]?.setText(`val`.toString())
                        }
                    } else {
                        for (i in texts.indices) {
                            t.bslv[i] = 30
                            texts[i]?.setText(30.toString())
                        }
                    }

                    editable = true
                }
            }
        })

        setListenersFortextInputEditText(construction, text, constructions, texts)

        initialized = true
    }

    private fun setListenerforTextInputLayouts(layouts: Array<TextInputLayout?>) {
        for (t in layouts) {
            t?.setHelperTextColor(ColorStateList(states, color))
        }
    }

    private fun setListenersFortextInputEditText(construction: TextInputLayout, text: TextInputEditText?, constructions: Array<TextInputLayout?>, texts: Array<TextInputEditText?>) {
        if (context == null)
            return

        for (i in texts.indices) {

            val max = if(i == 0)
                30
            else {
                val curve = Treasure.curveData[i]

                curve?.max ?: 30
            }

            constructions[i]?.helperText = "1~$max Lv."

            texts[i]?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty()) {
                        if (Integer.parseInt(s.toString()) > max || Integer.parseInt(s.toString()) < 1) {
                            if (constructions[i]?.isHelperTextEnabled == true) {
                                constructions[i]?.isHelperTextEnabled = false
                                constructions[i]?.isErrorEnabled = true
                                constructions[i]?.error = context!!.getString(R.string.treasure_invalid)
                            }
                        } else {
                            if (constructions[i]?.isErrorEnabled != false) {
                                constructions[i]?.error = null
                                constructions[i]?.isErrorEnabled = false
                                constructions[i]?.isHelperTextEnabled = true
                                constructions[i]?.setHelperTextColor(ColorStateList(states, color))
                                constructions[i]?.helperText = "1~$max Lv."
                            }
                        }
                    } else {
                        if (constructions[i]?.isErrorEnabled != false) {
                            constructions[i]?.error = null
                            constructions[i]?.isErrorEnabled = false
                            constructions[i]?.isHelperTextEnabled = true
                            constructions[i]?.setHelperTextColor(ColorStateList(states, color))
                            constructions[i]?.helperText = "1~$max Lv."
                        }
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    if (!initialized) return

                    if (s.toString().isNotEmpty()) {
                        val t = BasisSet.current().t()

                        if (editable && Integer.parseInt(s.toString()) <= max && Integer.parseInt(s.toString()) >= 1) {
                            val `val` = Integer.parseInt(s.toString())

                            t.bslv[i] = `val`

                            text?.setText("")
                            construction.isHelperTextEnabled = true
                            construction.helperText = "1~$max Lv."
                        }

                        val c = context ?: return

                        StaticStore.saveLineUp(c)
                    }
                }
            })
        }
    }

    private fun valuesAllSame(): Boolean {
        val bases = BasisSet.current().t().bslv ?: return false

        val check = bases[0]

        for (i in 1 until bases.size) {
            if (check != bases[i])
                return false
        }

        return true
    }

    companion object {

        fun newInstance(): LUConstruction {
            return LUConstruction()
        }
    }
}
