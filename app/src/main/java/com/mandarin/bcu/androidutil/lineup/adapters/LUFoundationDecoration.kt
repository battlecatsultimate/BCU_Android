package com.mandarin.bcu.androidutil.lineup.adapters

import android.content.res.ColorStateList
import android.os.Bundle
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
import common.CommonStatic
import common.battle.BasisSet
import common.battle.Treasure
import common.util.Data

class LUFoundationDecoration : Fragment() {

    companion object {
        fun newInstances(isFoundation: Boolean) : LUFoundationDecoration {
            val fragment = LUFoundationDecoration()

            fragment.setFoundation(isFoundation)

            return fragment
        }
    }

    private var isFoundation = false

    private var initialized = false
    private var editable = true

    private val layoutid = intArrayOf(R.id.bsdcslow, R.id.bsdcwall, R.id.bsdcstop, R.id.bsdcwater, R.id.bsdczombie, R.id.bsdcbreaker, R.id.bsdccurse)

    private val textid = intArrayOf(R.id.bsdcslowt, R.id.bsdcwallt, R.id.bsdcstopt, R.id.bsdcwatert, R.id.bsdczombiet, R.id.bsdcbreakert, R.id.bsdccurset)

    private val foundationid = intArrayOf(R.string.lineup_slfd, R.string.lineup_iwfd, R.string.lineup_thfd, R.string.lineup_wtfd, R.string.lineup_hbfd, R.string.lineup_bbfd, R.string.lineup_cbfd)
    private val decorationidd = intArrayOf(R.string.lineup_sldc, R.string.lineup_iwdc, R.string.lineup_thdc, R.string.lineup_wtdc, R.string.lineup_hbdc, R.string.lineup_bbdc, R.string.lineup_cbdc)

    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))

    private var color: IntArray? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.lineup_base_deco, container, false)

        color = intArrayOf(StaticStore.getAttributeColor(requireContext(), R.attr.TextPrimary))

        listeners(view)

        initialized = false

        val texts = Array<TextInputEditText>(Data.DECO_BASE_TOT) {
            view.findViewById(textid[it])
        }

        val text = view.findViewById<TextInputEditText>(R.id.bsdct)

        val values = if(isFoundation)
            BasisSet.current().t().base
        else
            BasisSet.current().t().deco

        for(i in values.indices)
            texts[i].setText(values[i].toString())

        if(valueAllSame(values))
            text.setText(values[0].toString())
        else
            text.setText("")

        initialized = true

        return view
    }

    fun update() {
        val view = view ?: return

        initialized = false

        val texts = Array<TextInputEditText>(Data.DECO_BASE_TOT) {
            view.findViewById(textid[it])
        }

        val text = view.findViewById<TextInputEditText>(R.id.bsdct)

        val values = if(isFoundation)
            BasisSet.current().t().base
        else
            BasisSet.current().t().deco

        for(i in values.indices)
            texts[i].setText(values[i].toString())

        if(valueAllSame(values))
            text.setText(values[0].toString())
        else
            text.setText("")

        initialized = true
    }

    private fun listeners(view: View) {
        val layouts = Array<TextInputLayout>(Data.DECO_BASE_TOT) {
            view.findViewById(layoutid[it])
        }

        val layout = view.findViewById<TextInputLayout>(R.id.bsdc)

        val texts = Array<TextInputEditText>(Data.DECO_BASE_TOT) {
            view.findViewById(textid[it])
        }

        val text = view.findViewById<TextInputEditText>(R.id.bsdct)

        val values = if (isFoundation)
            BasisSet.current().t().base
        else
            BasisSet.current().t().deco

        layout.setHelperTextColor(ColorStateList(states, color))
        layout.setHint(if(isFoundation) R.string.lineup_fd else R.string.lineup_dc)

        if(valueAllSame(values))
            text.setText(values[0].toString())

        text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val t = s.toString()

                if (t.isNotBlank()) {
                    val v = CommonStatic.safeParseInt(t)

                    if(v > 20 || v < 0) {
                        if(layout.isHelperTextEnabled) {
                            layout.isHelperTextEnabled = false
                            layout.isErrorEnabled = true
                            layout.error = requireContext().getString(R.string.treasure_invalid)
                        }
                    } else {
                        if(layout.isErrorEnabled) {
                            layout.isHelperTextEnabled = true
                            layout.isErrorEnabled = false
                            layout.error = null
                            layout.setHelperTextColor(ColorStateList(states, color))
                            layout.helperText = "0~20 Lv."
                        }
                    }
                } else {
                    if(layout.isErrorEnabled) {
                        layout.isHelperTextEnabled = true
                        layout.isErrorEnabled = false
                        layout.error = null
                        layout.setHelperTextColor(ColorStateList(states, color))
                        layout.helperText = "0~20 Lv."
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                if(!initialized)
                    return

                val t = s.toString()

                if(t.isNotBlank()) {
                    val v = CommonStatic.safeParseInt(t)

                    editable = false

                    if(v in 0..20) {
                        for(i in texts.indices) {
                            values[i] = v
                            texts[i].setText(v.toString())
                        }
                    } else {
                        for(i in texts.indices) {
                            values[i] = 20
                            texts[i].setText(20.toString())
                        }
                    }

                    editable = true
                }
            }
        })

        for (i in 0 until Data.DECO_BASE_TOT) {
            layouts[i].setHelperTextColor(ColorStateList(states, color))
            layouts[i].setHint(if (isFoundation) foundationid[i] else decorationidd[i])
            texts[i].setText(values[i].toString())

            val curve = if(isFoundation)
                Treasure.baseData[i + 1]
            else
                Treasure.decorationData[i + 1]

            val max = curve?.max ?: 20

            layouts[i].helperText = "0~$max Lv."

            texts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val t = s.toString()

                    if (t.isNotBlank()) {
                        val v = CommonStatic.safeParseInt(t)

                        if(v > max || v < 0) {
                            if(layouts[i].isHelperTextEnabled) {
                                layouts[i].isHelperTextEnabled = false
                                layouts[i].isErrorEnabled = true
                                layouts[i].error = requireContext().getString(R.string.treasure_invalid)
                            }
                        } else {
                            if(layouts[i].isErrorEnabled) {
                                layouts[i].isHelperTextEnabled = true
                                layouts[i].isErrorEnabled = false
                                layouts[i].error = null
                                layouts[i].setHelperTextColor(ColorStateList(states, color))
                                layouts[i].helperText = "0~$max Lv."
                            }
                        }
                    } else {
                        if(layouts[i].isErrorEnabled) {
                            layouts[i].isHelperTextEnabled = true
                            layouts[i].isErrorEnabled = false
                            layouts[i].error = null
                            layouts[i].setHelperTextColor(ColorStateList(states, color))
                            layouts[i].helperText = "0~$max Lv."
                        }
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    if(!initialized)
                        return

                    val t = s.toString()

                    if(t.isNotBlank()) {
                        val v = CommonStatic.safeParseInt(t)

                        if (editable && v in 0..max) {
                            values[i] = v

                            text.setText("")
                            layout.isHelperTextEnabled = true
                            layout.helperText = "0~20 Lv."
                            layout.error = null
                            layout.isErrorEnabled = false
                        }

                        StaticStore.saveLineUp(requireContext(), false)
                    }
                }
            })
        }
    }

    private fun valueAllSame(values: IntArray) : Boolean {
        if(values.isEmpty())
            return true

        for(i in values.indices) {
            if(values[i] != values[0])
                return false
        }

        return true
    }

    private fun setFoundation(isFoundation: Boolean) {
        this.isFoundation = isFoundation
    }
}