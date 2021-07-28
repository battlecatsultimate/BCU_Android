package com.mandarin.bcu.androidutil.lineup.adapters

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.lineup.LineUpView
import common.battle.BasisSet
import java.util.*

class LUTreasureSetting(private val line: LineUpView) : Fragment() {

    private var canbeEdited = true
    private var initialized = false
    private var destroyed = false

    private val techid = intArrayOf(R.id.cdlev, R.id.aclev, R.id.basehlev, R.id.worklev, R.id.walletlev, R.id.rechargelev)
    private val eocid = intArrayOf(R.id.atktrea, R.id.healtrea, R.id.cdtrea, R.id.actrea, R.id.worktrea, R.id.wallettrea)
    private val eocitfid = intArrayOf(R.id.rechargetrea, R.id.canatktrea, R.id.basehtrea)
    private val itfid = intArrayOf(R.id.redfrtrea, R.id.floatfrtrea, R.id.blacktrea, R.id.angelfrtrea)
    private val cotcid = intArrayOf(R.id.metalfrtrea, R.id.zombiefrtrea, R.id.alienfrtrea)
    private val maskid = intArrayOf(R.id.masktrea, R.id.mask2trea, R.id.mask3trea)

    private val limitvals = intArrayOf(30, 30, 10, 300, 600, 300, 300, 600, 1500)
    private val limitmins = intArrayOf(1, 1, 1, 0, 0, 0, 0, 0, 0)
    private val limitvalss = intArrayOf(30, 300, 600, 300, 300, 100)
    private val limitminss = intArrayOf(1, 0, 0, 0, 0, 0)
    private val helpers = arrayOf("1~30 lv", "1~30 lv", "1~10 lv", "0~300 %", "0~600 %", "0~300 %", "0~300 %", "0~600 %", "0~1500 %")
    private val helperss = arrayOf("1~30 lv", "0~300 %", "0~600 %", "0~300 %", "0~300 %", "0~100 %")

    private val techeid = intArrayOf(R.id.cdlevt, R.id.aclevt, R.id.basehlevt, R.id.worklevt, R.id.walletlevt, R.id.rechargelevt)
    private val eoceid = intArrayOf(R.id.atktreat, R.id.healtreat, R.id.cdtreat, R.id.actreat, R.id.worktreat, R.id.wallettreat)
    private val eocitfeid = intArrayOf(R.id.rechargetreat, R.id.canatktreat, R.id.basehtreat)
    private val itfeid = intArrayOf(R.id.redfrtreat, R.id.floatfrtreat, R.id.blacktreat, R.id.angelfrtreat)
    private val cotceid = intArrayOf(R.id.metalfrtreat, R.id.zombiefrtreat, R.id.alienfrtreat)
    private val maskeid = intArrayOf(R.id.masktreat, R.id.mask2treat, R.id.mask3treat)

    private val expandid = intArrayOf(R.id.techexpand, R.id.orbadd, R.id.eocitfexpand, R.id.itfexpand, R.id.cotcexpand)

    private val layoutid = intArrayOf(R.id.techlayout, R.id.eoclayout, R.id.eocitflayout, R.id.itffruitlayout, R.id.cotclayout)

    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))

    private val tilid = intArrayOf(R.id.statschmulti, R.id.canatklev, R.id.canrangelev, R.id.eoctrea, R.id.eocitftrea, R.id.itffruittrea, R.id.cotctrea, R.id.itfcrytrea, R.id.cotccrytrea)
    private val tilsid = arrayOf(techid, eocid, eocitfid, itfid, cotcid, maskid)

    private lateinit var color: IntArray

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.lineup_treasure_set, group, false)

        color = intArrayOf(StaticStore.getAttributeColor(requireContext(), R.attr.TextPrimary))

        listeners(view)

        val teche = view.findViewById<TextInputEditText>(R.id.statschmultiedit)
        val canatke = view.findViewById<TextInputEditText>(R.id.canatklevt)
        val canrangee = view.findViewById<TextInputEditText>(R.id.canrangelevt)
        val eoce = view.findViewById<TextInputEditText>(R.id.eoctreat)
        val eocitfe = view.findViewById<TextInputEditText>(R.id.eocitftreat)

        val teches = Array<TextInputEditText>(6) {
            view.findViewById(techeid[it])
        }

        val eoces = Array<TextInputEditText>(6) {
            view.findViewById(eoceid[it])
        }

        val eocitfes = Array<TextInputEditText>(3) {
            view.findViewById(eocitfeid[it])
        }

        val itfe = view.findViewById<TextInputEditText>(R.id.itffruittreat)

        val itfes = Array<TextInputEditText>(4) {
            view.findViewById(itfeid[it])
        }

        val cotce = view.findViewById<TextInputEditText>(R.id.cotctreat)

        val cotces = Array<TextInputEditText>(3) {
            view.findViewById(cotceid[it])
        }

        val itfcrye = view.findViewById<TextInputEditText>(R.id.itfcrytreat)

        val cotccrye = view.findViewById<TextInputEditText>(R.id.cotccrytreat)

        val maskes = Array<TextInputEditText>(3) {
            view.findViewById(maskeid[it])
        }


        val t = BasisSet.current().t()

        initialized = false

        itfcrye.setText(t.alien.toString())
        cotccrye.setText(t.star.toString())

        for (i in 0..5) {
            teches[i].setText(t.tech[i].toString())
        }

        canatke.setText(t.tech[6].toString())
        canrangee.setText(t.tech[7].toString())

        for (i in 0..5) {
            eoces[i].setText(t.trea[i].toString())
        }

        for (i in eocitfes.indices) {
            eocitfes[i].setText(t.trea[i + 6].toString())
        }

        for (i in 0..3) {
            itfes[i].setText(t.fruit[i].toString())
        }

        for (i in 4 until t.fruit.size) {
            cotces[i - 4].setText(t.fruit[i].toString())
        }

        for (i in t.gods.indices) {
            maskes[i].setText(t.gods[i].toString())
        }

        if (valuesAllSame(0))
            teche.setText(t.tech[0].toString())

        if (valuesAllSame(1))
            eoce.setText(t.trea[0].toString())

        if (valuesAllSame(2))
            eocitfe.setText(t.trea[6].toString())

        if (valuesAllSame(3))
            itfe.setText(t.fruit[0].toString())

        if (valuesAllSame(4))
            cotce.setText(t.fruit[4].toString())

        initialized = true

        return view
    }

    fun update() {
        val view = view ?: return

        val teche = view.findViewById<TextInputEditText>(R.id.statschmultiedit)
        val canatke = view.findViewById<TextInputEditText>(R.id.canatklevt)
        val canrangee = view.findViewById<TextInputEditText>(R.id.canrangelevt)
        val eoce = view.findViewById<TextInputEditText>(R.id.eoctreat)
        val eocitfe = view.findViewById<TextInputEditText>(R.id.eocitftreat)

        val teches = Array<TextInputEditText>(6) {
            view.findViewById(techeid[it])
        }

        val eoces = Array<TextInputEditText>(6) {
            view.findViewById(eoceid[it])
        }

        val eocitfes = Array<TextInputEditText>(3) {
            view.findViewById(eocitfeid[it])
        }

        val itfe = view.findViewById<TextInputEditText>(R.id.itffruittreat)

        val itfes = Array<TextInputEditText>(4) {
            view.findViewById(itfeid[it])
        }

        val cotce = view.findViewById<TextInputEditText>(R.id.cotctreat)

        val cotces = Array<TextInputEditText>(3) {
            view.findViewById(cotceid[it])
        }

        val itfcrye = view.findViewById<TextInputEditText>(R.id.itfcrytreat)

        val cotccrye = view.findViewById<TextInputEditText>(R.id.cotccrytreat)

        val maskes = Array<TextInputEditText>(3) {
            view.findViewById(maskeid[it])
        }

        val t = BasisSet.current().t()

        initialized = false

        itfcrye.setText(t.alien.toString())
        cotccrye.setText(t.star.toString())

        for (i in 0..5) {
            teches[i].setText(t.tech[i].toString())
        }

        canatke.setText(t.tech[6].toString())
        canrangee.setText(t.tech[7].toString())

        for (i in 0..5) {
            eoces[i].setText(t.trea[i].toString())
        }

        for (i in eocitfes.indices) {
            eocitfes[i].setText(t.trea[i + 6].toString())
        }

        for (i in 0..3) {
            itfes[i].setText(t.fruit[i].toString())
        }

        for (i in 4 until t.fruit.size) {
            cotces[i - 4].setText(t.fruit[i].toString())
        }

        for (i in t.gods.indices) {
            maskes[i].setText(t.gods[i].toString())
        }

        if (valuesAllSame(0))
            teche.setText(t.tech[0].toString())

        if (valuesAllSame(1))
            eoce.setText(t.trea[0].toString())

        if (valuesAllSame(2))
            eocitfe.setText(t.trea[6].toString())

        if (valuesAllSame(3))
            itfe.setText(t.fruit[0].toString())

        if (valuesAllSame(4))
            cotce.setText(t.fruit[4].toString())

        initialized = true
    }

    private fun listeners(view: View) {
        val t = BasisSet.current().t()

        val tech = view.findViewById<TextInputLayout>(R.id.statschmulti)
        val teche = view.findViewById<TextInputEditText>(R.id.statschmultiedit)

        val techs = Array<TextInputLayout>(6) {
            view.findViewById(techid[it])
        }

        val teches = Array<TextInputEditText>(6) {
            view.findViewById(techeid[it])
        }

        val canatk = view.findViewById<TextInputLayout>(R.id.canatklev)
        val canatke = view.findViewById<TextInputEditText>(R.id.canatklevt)

        val canrange = view.findViewById<TextInputLayout>(R.id.canrangelev)
        val canrangee = view.findViewById<TextInputEditText>(R.id.canrangelevt)

        val eoc = view.findViewById<TextInputLayout>(R.id.eoctrea)
        val eoce = view.findViewById<TextInputEditText>(R.id.eoctreat)

        val eocitf = view.findViewById<TextInputLayout>(R.id.eocitftrea)
        val eocitfe = view.findViewById<TextInputEditText>(R.id.eocitftreat)

        val eocs = Array<TextInputLayout>(6) {
            view.findViewById(eocid[it])
        }

        val eoces = Array<TextInputEditText>(6) {
            view.findViewById(eoceid[it])
        }

        val itf = view.findViewById<TextInputLayout>(R.id.itffruittrea)
        val itfe = view.findViewById<TextInputEditText>(R.id.itffruittreat)

        val eocitfs = Array<TextInputLayout>(3) {
            view.findViewById(eocitfid[it])
        }

        val eocitfes = Array<TextInputEditText>(3) {
            view.findViewById(eocitfeid[it])
        }

        val itfs = Array<TextInputLayout>(4) {
            view.findViewById(itfid[it])
        }

        val itfes = Array<TextInputEditText>(4) {
            view.findViewById(itfeid[it])
        }

        val cotc = view.findViewById<TextInputLayout>(R.id.cotctrea)
        val cotce = view.findViewById<TextInputEditText>(R.id.cotctreat)

        val cotcs = Array<TextInputLayout>(3) {
            view.findViewById(cotcid[it])
        }

        val cotces = Array<TextInputEditText>(3) {
            view.findViewById(cotceid[it])
        }

        val itfcry = view.findViewById<TextInputLayout>(R.id.itfcrytrea)
        val itfcrye = view.findViewById<TextInputEditText>(R.id.itfcrytreat)

        val cotccry = view.findViewById<TextInputLayout>(R.id.cotccrytrea)
        val cotccrye = view.findViewById<TextInputEditText>(R.id.cotccrytreat)

        val masks = Array<TextInputLayout>(3) {
            view.findViewById(maskid[it])
        }

        val maskes = Array<TextInputEditText>(3) {
            view.findViewById(maskeid[it])
        }

        val expands = Array<FloatingActionButton>(5) {
            view.findViewById(expandid[it])
        }

        val layouts = Array<LinearLayout>(5) {
            view.findViewById(layoutid[it])
        }

        //Listeners for expand image buttons
        for (i in expands.indices) {

            expands[i].setOnClickListener(View.OnClickListener {
                if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                    return@OnClickListener

                StaticStore.infoClick = SystemClock.elapsedRealtime()

                if (layouts[i].height == 0) {
                    layouts[i].measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val height = layouts[i].measuredHeight

                    val anim = ValueAnimator.ofInt(0, height)
                    anim.addUpdateListener { animation ->
                        val `val` = animation.animatedValue as Int
                        val params = layouts[i].layoutParams
                        params?.height = `val`
                        layouts[i].layoutParams = params
                    }

                    anim.duration = 300
                    anim.interpolator = DecelerateInterpolator()
                    anim.start()

                    val c = context ?: return@OnClickListener

                    expands[i].setImageDrawable(ContextCompat.getDrawable(c, R.drawable.ic_expand_more_black_24dp))
                } else {
                    layouts[i].measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val height = layouts[i].measuredHeight

                    val anim = ValueAnimator.ofInt(height, 0)
                    anim.addUpdateListener { animation ->
                        val `val` = animation.animatedValue as Int
                        val params = layouts[i].layoutParams
                        params?.height = `val`
                        layouts[i].layoutParams = params
                    }

                    anim.duration = 300
                    anim.interpolator = DecelerateInterpolator()
                    anim.start()

                    val c = context ?: return@OnClickListener

                    expands[i].setImageDrawable(ContextCompat.getDrawable(c, R.drawable.ic_expand_less_black_24dp))
                }
            })
        }

        //Listeners for TextInputLayout

        itfcrye.setText(t.alien.toString())
        cotccrye.setText(t.star.toString())

        setListenerforTextInputLayout(tech, canatk, canrange, eoc, eocitf, itf, cotc, itfcry, cotccry)
        setListenerforTextInputLayouts(techs, eocs, eocitfs, itfs, cotcs, masks)

        //Listeners for TextInputLayouts

        for (i in 0..5) {
            teches[i].setText(t.tech[i].toString())
        }

        canatke.setText(t.tech[6].toString())
        canrangee.setText(t.tech[7].toString())

        for (i in 0..5) {
            eoces[i].setText(t.trea[i].toString())
        }

        for (i in eocitfes.indices) {
            eocitfes[i].setText(t.trea[i + 6].toString())
        }

        for (i in 0..3) {
            itfes[i].setText(t.fruit[i].toString())
        }

        for (i in 4 until t.fruit.size) {
            cotces[i - 4].setText(t.fruit[i].toString())
        }

        for (i in t.gods.indices) {
            maskes[i].setText(t.gods[i].toString())
        }


        if (valuesAllSame(0))
            teche.post { teche.setText(t.tech[0].toString()) }

        if (valuesAllSame(1))
            eoce.post { eoce.setText(t.trea[0].toString()) }

        if (valuesAllSame(2))
            eocitfe.post { eocitfe.setText(t.trea[6].toString()) }

        if (valuesAllSame(3))
            itfe.post { itfe.setText(t.fruit[0].toString()) }

        if (valuesAllSame(4))
            cotce.post { cotce.setText(t.fruit[4].toString()) }

        setListenerforTextInputEditText(view, teche, canatke, canrangee, eoce, eocitfe, itfe, cotce, itfcrye, cotccrye)
        setListenerforTextInptEditTexts(view, teches, eoces, eocitfes, itfes, cotces, maskes)

        initialized = true
    }

    private fun valuesAllSame(mode: Int): Boolean {
        when (mode) {
            0 -> {
                val value = BasisSet.current().t().tech[0]

                for (i in 1..5) {
                    if (value != BasisSet.current().t().tech[i])
                        return false
                }

                return true
            }
            1 -> {
                val value = BasisSet.current().t().trea[0]

                for (i in 1..5) {
                    if (value != BasisSet.current().t().trea[i])
                        return false
                }

                return true
            }
            2 -> {
                val value = BasisSet.current().t().trea[6]

                for (i in 7..8)
                    if (value != BasisSet.current().t().trea[i])
                        return false

                return true
            }
            3 -> {
                val value = BasisSet.current().t().fruit[0]

                for (i in 1..3)
                    if (value != BasisSet.current().t().fruit[i])
                        return false

                return true
            }
            4 -> {
                val value = BasisSet.current().t().fruit[4]

                for (i in 5 until BasisSet.current().t().fruit.size)
                    if (value != BasisSet.current().t().fruit[i])
                        return false

                return true
            }
        }

        return false
    }

    private fun setListenerforTextInputLayout(vararg texts: TextInputLayout) {
        for (t in texts) {
            t.setHelperTextColor(ColorStateList(states, color))
        }
    }

    private fun setListenerforTextInputLayouts(vararg texts: Array<TextInputLayout>) {
        for (ts in texts) {
            for (t in ts) {
                t.setHelperTextColor(ColorStateList(states, color))
            }
        }
    }

    private fun setListenerforTextInputEditText(view: View?, vararg texts: TextInputEditText) {
        if (context == null) return

        if (view == null) return

        val teches = Array<TextInputEditText>(6) {
            view.findViewById(techeid[it])
        }

        val eoces = Array<TextInputEditText>(6) {
            view.findViewById(eoceid[it])
        }

        val eocitfes = Array<TextInputEditText>(3) {
            view.findViewById(eocitfeid[it])
        }

        val itfes = Array<TextInputEditText>(4) {
            view.findViewById(itfeid[it])
        }

        val cotces = Array<TextInputEditText>(3) {
            view.findViewById(cotceid[it])
        }

        for (i in texts.indices) {
            texts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val tils = view.findViewById<TextInputLayout>(tilid[i])

                    if (s.toString().isNotEmpty()) {

                        if (Integer.parseInt(s.toString()) > limitvals[i] || Integer.parseInt(s.toString()) < limitmins[i]) {
                            if (tils.isHelperTextEnabled) {
                                tils.isHelperTextEnabled = false
                                tils.isErrorEnabled = true
                                tils.error = Objects.requireNonNull<Context>(context).getText(R.string.treasure_invalid)
                            }
                        } else {
                            if (tils.isErrorEnabled) {
                                tils.error = null
                                tils.isErrorEnabled = false
                                tils.isHelperTextEnabled = true
                                tils.setHelperTextColor(ColorStateList(states, color))
                                tils.helperText = helpers[i]
                            }
                        }
                    } else {
                        if (tils.isErrorEnabled) {
                            tils.error = null
                            tils.isErrorEnabled = false
                            tils.isHelperTextEnabled = true
                            tils.setHelperTextColor(ColorStateList(states, color))
                            tils.helperText = helpers[i]
                        }
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    if (!initialized)
                        return

                    if (s.toString().isNotEmpty()) {
                        val t = BasisSet.current().t()

                        canbeEdited = false

                        if (Integer.parseInt(s.toString()) <= limitvals[i] && Integer.parseInt(s.toString()) >= limitmins[i]) {
                            val `val` = Integer.parseInt(s.toString())

                            when (i) {
                                0 -> for (j in 0..5) {
                                    t.tech[j] = `val`
                                    teches[j].setText(`val`.toString())
                                }
                                1 -> t.tech[6] = `val`
                                2 -> t.tech[7] = `val`
                                3 -> for (j in 0..5) {
                                    t.trea[j] = `val`
                                    eoces[j].setText(`val`.toString())
                                }
                                4 -> for (j in 6..8) {
                                    t.trea[j] = `val`
                                    eocitfes[j - 6].setText(`val`.toString())
                                }
                                5 -> for (j in 0..3) {
                                    t.fruit[j] = `val`
                                    itfes[j].setText(`val`.toString())
                                }
                                6 -> for (j in 4 until t.fruit.size) {
                                    t.fruit[j] = `val`
                                    cotces[j - 4].setText(`val`.toString())
                                }
                                7 -> t.alien = `val`
                                8 -> t.star = `val`
                            }
                        } else {
                            when (i) {
                                0 -> for (j in 0..5) {
                                    t.tech[j] = 30
                                    teches[j].setText(30.toString())
                                }
                                1 -> t.tech[6] = 30
                                2 -> t.tech[7] = 10
                                3 -> for (j in 0..5) {
                                    t.trea[j] = 300
                                    eoces[j].setText(300.toString())
                                }
                                4 -> for (j in 6..8) {
                                    t.trea[j] = 600
                                    eocitfes[j - 6].setText(600.toString())
                                }
                                5 -> for (j in 0..3) {
                                    t.fruit[j] = 300
                                    itfes[j].setText(300.toString())
                                }
                                6 -> for (j in 4 until t.fruit.size) {
                                    t.fruit[j] = 300
                                    cotces[j - 4].setText(300.toString())
                                }
                                7 -> t.alien = 600
                                8 -> t.star = 1500
                            }
                        }

                        line

                        canbeEdited = true

                        val c = context ?: return

                        StaticStore.saveLineUp(c)
                    }
                }
            })
        }
    }

    private fun setListenerforTextInptEditTexts(view: View, vararg texts: Array<TextInputEditText>) {
        if (context == null) return

        val tech = view.findViewById<TextInputLayout>(R.id.statschmulti)
        val teche = view.findViewById<TextInputEditText>(R.id.statschmultiedit)

        val eoc = view.findViewById<TextInputLayout>(R.id.eoctrea)
        val eoce = view.findViewById<TextInputEditText>(R.id.eoctreat)

        val eocitf = view.findViewById<TextInputLayout>(R.id.eocitftrea)
        val eocitfe = view.findViewById<TextInputEditText>(R.id.eocitftreat)

        val itf = view.findViewById<TextInputLayout>(R.id.itffruittrea)
        val itfe = view.findViewById<TextInputEditText>(R.id.itffruittreat)

        val cotc = view.findViewById<TextInputLayout>(R.id.cotctrea)
        val cotce = view.findViewById<TextInputEditText>(R.id.cotctreat)

        for (i in texts.indices) {
            for (j in texts[i].indices) {

                texts[i][j].addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (!initialized)
                            return

                        val tilss = view.findViewById<TextInputLayout>(tilsid[i][j])

                        if (s.toString().isNotEmpty()) {
                            if (Integer.parseInt(s.toString()) > limitvalss[i] || Integer.parseInt(s.toString()) < limitminss[i]) {
                                if (tilss.isHelperTextEnabled) {
                                    tilss.isHelperTextEnabled = false
                                    tilss.isErrorEnabled = true
                                    tilss.error = Objects.requireNonNull<Context>(context).getText(R.string.treasure_invalid)
                                }
                            } else {
                                if (tilss.isErrorEnabled) {
                                    tilss.error = null
                                    tilss.isErrorEnabled = false
                                    tilss.isHelperTextEnabled = true
                                    tilss.setHelperTextColor(ColorStateList(states, color))
                                    tilss.helperText = helperss[i]
                                }
                            }
                        } else {
                            if (tilss.isErrorEnabled) {
                                tilss.error = null
                                tilss.isErrorEnabled = false
                                tilss.isHelperTextEnabled = true
                                tilss.setHelperTextColor(ColorStateList(states, color))
                                tilss.helperText = helperss[i]
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable) {
                        if (s.toString().isNotEmpty()) {
                            val t = BasisSet.current().t()

                            if (canbeEdited && Integer.parseInt(s.toString()) <= limitvalss[i] && Integer.parseInt(s.toString()) >= limitminss[i]) {
                                val `val` = Integer.parseInt(s.toString())

                                when (i) {
                                    0 -> {
                                        t.tech[j] = `val`

                                        teche.setText("")
                                        tech.isHelperTextEnabled = true
                                        tech.helperText = "1~30 Lv."
                                    }
                                    1 -> {
                                        t.trea[j] = `val`

                                        eoce.text = null
                                        eoc.isHelperTextEnabled = true
                                        eoc.helperText = helperss[i]
                                    }
                                    2 -> {
                                        t.trea[j + 6] = `val`

                                        eocitfe.text = null
                                        eocitf.isHelperTextEnabled = true
                                        eocitf.helperText = helperss[i]
                                    }
                                    3 -> {
                                        t.fruit[j] = `val`

                                        itfe.text = null
                                        itf.isHelperTextEnabled = true
                                        itf.helperText = helperss[i]
                                    }
                                    4 -> {
                                        t.fruit[j + 4] = `val`

                                        cotce.text = null
                                        cotc.isHelperTextEnabled = true
                                        cotc.helperText = helperss[i]
                                    }
                                    5 -> {
                                        t.gods[j] = `val`
                                    }
                                }
                            }

                            line.updateUnitSetting()

                            val c = context ?: return

                            StaticStore.saveLineUp(c)
                        }
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
    }

    companion object {

        fun newInstance(line: LineUpView): LUTreasureSetting {
            return LUTreasureSetting(line)
        }
    }
}
