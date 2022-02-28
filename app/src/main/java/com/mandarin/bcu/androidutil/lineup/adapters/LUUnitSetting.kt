package com.mandarin.bcu.androidutil.lineup.adapters

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.UnitInfo
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.battle.BasisSet
import common.io.json.JsonEncoder
import common.util.unit.Form

class LUUnitSetting : Fragment() {
    companion object {
        fun newInstance(line: LineUpView): LUUnitSetting {
            val unitSetting = LUUnitSetting()
            unitSetting.setVariable(line)

            return unitSetting
        }
    }

    private lateinit var line: LineUpView
    private lateinit var talents: Array<Spinner>
    private var pcoin = ArrayList<Int>()

    private var fid = 0

    var f: Form? = null

    init {
        for(i in 0 until 6)
            pcoin.add(0)
    }

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View {
        val view = inflater.inflate(R.layout.lineup_unit_set, group, false)

        update()

        return view
    }

    fun update() {
        val v = view ?: return

        val spinners = arrayOf(v.findViewById(R.id.lineuplevspin), v.findViewById<Spinner>(R.id.lineuplevpspin))
        val plus = v.findViewById<TextView>(R.id.lineuplevplus)
        val row = v.findViewById<TableRow>(R.id.lineupunittable)
        val tal = v.findViewById<TableRow>(R.id.lineuppcoin)
        val t = v.findViewById<CheckBox>(R.id.lineuptalent)
        val hp = v.findViewById<TextView>(R.id.lineupunithp)
        val atk = v.findViewById<TextView>(R.id.lineupunitatk)
        val chform = v.findViewById<Button>(R.id.lineupchform)
        val levt = v.findViewById<TextView>(R.id.lineupunitlevt)

        f = if (StaticStore.position[0] == -1)
            null
        else if (StaticStore.position[0] == 100)
            line.repform
        else {
            if (StaticStore.position[0] * 5 + StaticStore.position[1] >= StaticStore.currentForms.size)
                null
            else
                StaticStore.currentForms[StaticStore.position[0] * 5 + StaticStore.position[1]]
        }

        if (f == null) {
            setDisappear(spinners[0], spinners[1], plus, row, t, tal, chform, levt)
        } else {
            if (context == null)
                return

            if(f != null) {
                BasisSet.synchronizeOrb(f!!.unit)
            }

            val f = this.f ?: return

            setAppear(spinners[0], spinners[1], plus, row, t, tal, chform, levt)

            val s = GetStrings(requireContext())

            fid = f.fid

            if (f.unit.maxp == 0)
                setDisappear(spinners[1], plus)

            if(this::talents.isInitialized && talents.isNotEmpty())
                tal.removeAllViews()

            if (f.du.pCoin != null) {
                val max = f.du.pCoin.max

                talents = Array(max.size - 1) {
                    val spin = Spinner(context)

                    val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (f.du.pCoin.max.size - 1)).toFloat())

                    spin.layoutParams = param
                    spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                    spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                    tal.addView(spin)

                    spin
                }

                pcoin = BasisSet.current().sele.lu.getLv(f).lvs ?: return

                for(i in talents.indices) {
                    if(i >= f.du.pCoin.info.size) {
                        talents[i].isEnabled = false
                        talents[i].adapter = null
                        continue
                    } else if(!talents[i].isEnabled) {
                        talents[i].isEnabled = true
                    }

                    val list = ArrayList<Int>()

                    for(j in 0 until max[i+1] + 1)
                        list.add(j)

                    val adapter = ArrayAdapter(requireContext(), R.layout.spinneradapter, list)

                    talents[i].adapter = adapter

                    talents[i].setSelection(getIndex(talents[i], pcoin[i + 1]))

                    talents[i].setOnLongClickListener {
                        talents[i].isClickable = false
                        StaticStore.showShortMessage(context, s.getTalentName(i, f))

                        true
                    }
                }

                for (i in talents.indices) {
                    talents[i].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                            pcoin[i + 1] = position

                            val lev = spinners[0].selectedItem as Int
                            val levp1 = spinners[1].selectedItem as Int

                            pcoin[0] = lev + levp1

                            if (t.isChecked) {
                                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                            } else {
                                removePCoin()
                                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                            }

                            BasisSet.current().sele.lu.setLv(f.unit, pcoin)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {

                        }
                    }

                    val info = v.findViewById<ImageButton>(R.id.lineupunitinfo)

                    info.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            val uid = f.unit.id

                            val intent = Intent(context, UnitInfo::class.java)

                            intent.putExtra("Data", JsonEncoder.encode(uid).toString())
                            requireContext().startActivity(intent)
                        }
                    })
                }
            } else {
                talents = arrayOf()

                pcoin = ArrayList()

                for(i in 0 until 6)
                    pcoin.add(0)

                setDisappear(t, tal)
            }

            val levs = ArrayList<Int>()
            val levp = ArrayList<Int>()

            for (i in 1 until (f.unit.max) + 1)
                levs.add(i)

            for (i in 0 until (f.unit.maxp) + 1)
                levp.add(i)

            val adapter = ArrayAdapter(requireContext(), R.layout.spinneradapter, levs)
            val adapter1 = ArrayAdapter(requireContext(), R.layout.spinneradapter, levp)

            spinners[0].adapter = adapter
            spinners[1].adapter = adapter1

            var loadlev = BasisSet.current().sele.lu.getLv(f).lvs[0]

            var loadlevp = 0

            if (loadlev > f.unit.max) {
                loadlevp = loadlev - f.unit.max
                loadlev = f.unit.max
            }

            spinners[0].setSelection(getIndex(spinners[0], loadlev))
            spinners[1].setSelection(getIndex(spinners[1], loadlevp))

            spinners[0].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    pcoin[0] = lev + levp1

                    if (t.isChecked) {
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                    } else {
                        removePCoin()
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                    }

                    BasisSet.current().sele.lu.setLv(f.unit, pcoin)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            spinners[1].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    pcoin[0] = lev + levp1

                    if (t.isChecked) {
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                    } else {
                        removePCoin()
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                    }

                    BasisSet.current().sele.lu.setLv(f.unit, pcoin)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
            atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)

            t.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val anim = ValueAnimator.ofInt(0, StaticStore.dptopx(64f, requireContext()))
                    anim.addUpdateListener { animation ->
                        val `val` = animation.animatedValue as Int
                        val params = tal.layoutParams
                        params.height = `val`
                        tal.layoutParams = params
                    }

                    anim.duration = 300
                    anim.interpolator = DecelerateInterpolator()
                    anim.start()

                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    pcoin[0] = lev + levp1

                    BasisSet.current().sele.lu.setLv(f.unit, pcoin)

                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)

                } else {
                    val anim = ValueAnimator.ofInt(StaticStore.dptopx(64f, requireContext()), 0)
                    anim.addUpdateListener { animation ->
                        val `val` = animation.animatedValue as Int
                        val params = tal.layoutParams
                        params.height = `val`
                        tal.layoutParams = params
                    }

                    anim.duration = 300
                    anim.interpolator = DecelerateInterpolator()
                    anim.start()

                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    pcoin[0] = lev + levp1
                    removePCoin()

                    BasisSet.current().sele.lu.setLv(f.unit, pcoin)

                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                }
            }

            if (f.du.pCoin != null) {
                if (pcoin[1] == 0 && pcoin[2] == 0 && pcoin[3] == 0 && pcoin[4] == 0 && pcoin[5] == 0) {
                    t.isChecked = false
                    val params = tal.layoutParams
                    params.height = 0
                    tal.layoutParams = params
                } else {
                    t.isChecked = true
                }
            }

            chform.setOnClickListener {
                fid++

                if (StaticStore.position[0] != 100 && StaticStore.position[1] != 100 && StaticStore.position[0] != -1 && StaticStore.position[1] != -1)
                    BasisSet.current().sele.lu.fs[StaticStore.position[0]][StaticStore.position[1]] = f.unit.forms[fid % f.unit.forms.size]
                else
                    line.repform = f.unit.forms[fid % f.unit.forms.size]

                this.f = f.unit.forms[fid % f.unit.forms.size]

                line.updateLineUp()
                line.toFormArray()

                val lev = spinners[0].selectedItem as Int
                val levp1 = spinners[1].selectedItem as Int

                pcoin[0] = lev + levp1

                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)
                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, pcoin)

                if (f.du.pCoin == null) {
                    setDisappear(t, tal)
                    pcoin = ArrayList()

                    for(i in 0 until 6)
                        pcoin.add(0)
                } else {
                    setAppear(t, tal)

                    pcoin = BasisSet.current().sele.lu.getLv(f).lvs ?: return@setOnClickListener

                    val max = f.du.pCoin.max

                    for (i in 1 until max.size) {
                        val ii = i - 1

                        val list = ArrayList<Int>()

                        for (j in 0 until max[i] + 1)
                            list.add(j)

                        val adapter2 = ArrayAdapter(requireContext(), R.layout.spinneradapter, list)

                        talents[i - 1].adapter = adapter2

                        talents[i - 1].setSelection(getIndex(talents[i-1], pcoin[i]))

                        talents[i - 1].setOnLongClickListener {
                            talents[ii].isClickable = false
                            StaticStore.showShortMessage(context, s.getTalentName(ii, f))

                            true
                        }
                    }

                    if (allZero(pcoin)) {
                        t.isChecked = false
                        val params = tal.layoutParams
                        params.height = 0
                        tal.layoutParams = params
                    } else {
                        t.isChecked = true
                    }
                }

                line.invalidate()

                line.updateUnitOrb()
            }
        }
    }

    private fun setDisappear(vararg views: View) {
        for (v in views)
            v.visibility = View.GONE
    }

    private fun setAppear(vararg views: View) {
        for (v in views)
            v.visibility = View.VISIBLE
    }

    private fun setVariable(line: LineUpView) {
        this.line = line
    }

    private fun getIndex(spinner: Spinner, lev: Int): Int {
        var index = 0

        for (i in 0 until spinner.count)
            if (lev == spinner.getItemAtPosition(i) as Int)
                index = i

        return index
    }

    private fun removePCoin() {
        for(i in 1 until pcoin.size) {
            pcoin[i] = 0
        }
    }

    private fun allZero(l: List<Int>) : Boolean {
        for(e in l)
            if(e != 0)
                return false

        return true
    }
}
