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
import java.util.*

class LUUnitSetting : Fragment() {
    companion object {
        fun newInstance(line: LineUpView): LUUnitSetting {
            val unitSetting = LUUnitSetting()
            unitSetting.setVariable(line)

            return unitSetting
        }
    }

    private lateinit var line: LineUpView
    private var pcoin = intArrayOf(0, 0, 0, 0, 0, 0)

    private var fid = 0

    var f: Form? = null

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

            setAppear(spinners[0], spinners[1], plus, row, t, tal, chform, levt)

            val s = GetStrings(requireContext())

            fid = f?.fid ?: 0

            if (f?.unit?.maxp == 0)
                setDisappear(spinners[1], plus)

            val id = intArrayOf(R.id.lineupp, R.id.lineupp1, R.id.lineupp2, R.id.lineupp3, R.id.lineupp4)

            val talents = arrayOfNulls<Spinner>(id.size)

            for (i in id.indices) {
                talents[i] = v.findViewById(id[i])
            }

            if (f?.du?.pCoin != null) {
                pcoin = BasisSet.current().sele.lu.getLv(f)?.lvs ?: return

                val max = f?.du?.pCoin?.max

                for (i in 1 until (max?.size ?: 1)) {
                    val ii = i - 1

                    val list = ArrayList<Int>()

                    for (j in 0 until (max?.get(i) ?: 0) + 1)
                        list.add(j)

                    val adapter = ArrayAdapter(requireContext(), R.layout.spinneradapter, list)

                    talents[i - 1]?.adapter = adapter

                    talents[i - 1]?.setSelection(getIndex(talents[i - 1], pcoin[i]))

                    talents[i - 1]?.setOnLongClickListener {
                        talents[ii]?.isClickable = false
                        StaticStore.showShortMessage(context, s.getTalentName(ii, f))

                        true
                    }
                }
            } else {
                pcoin = intArrayOf(0, 0, 0, 0, 0, 0)
                setDisappear(t, tal)
            }

            val levs = ArrayList<Int>()
            val levp = ArrayList<Int>()

            for (i in 1 until (f?.unit?.max ?: 0) + 1)
                levs.add(i)

            for (i in 0 until (f?.unit?.maxp ?: -1) + 1)
                levp.add(i)

            val adapter = ArrayAdapter(requireContext(), R.layout.spinneradapter, levs)
            val adapter1 = ArrayAdapter(requireContext(), R.layout.spinneradapter, levp)

            spinners[0].adapter = adapter
            spinners[1].adapter = adapter1

            var loadlev = BasisSet.current().sele.lu.getLv(f)?.lvs?.get(0) ?: return

            var loadlevp = 0

            if (loadlev > (f?.unit?.max ?: 0)) {
                loadlevp = loadlev - (f?.unit?.max ?: 0)
                loadlev = (f?.unit?.max ?: 0)
            }

            spinners[0].setSelection(getIndex(spinners[0], loadlev))
            spinners[1].setSelection(getIndex(spinners[1], loadlevp))

            spinners[0].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    pcoin[0] = lev + levp1

                    if (t.isChecked) {
                        hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                    } else {
                        removePCoin()
                        hp.text = s.getHP(f, BasisSet.current().t(), f != null && f?.du?.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f != null && f?.du?.pCoin != null && t.isChecked, pcoin)
                    }

                    if (f != null)
                        BasisSet.current().sele.lu.setLv(f?.unit, pcoin)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            spinners[1].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    pcoin[0] = lev + levp1

                    if (t.isChecked) {
                        hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                    } else {
                        removePCoin()
                        hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                    }

                    if(f != null)
                        BasisSet.current().sele.lu.setLv(f?.unit, pcoin)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            for (i in talents.indices) {
                talents[i]?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                        pcoin[i + 1] = position

                        val lev = spinners[0].selectedItem as Int
                        val levp1 = spinners[1].selectedItem as Int

                        pcoin[0] = lev + levp1

                        if (t.isChecked) {
                            hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                            atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                        } else {
                            removePCoin()
                            hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                            atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                        }

                        if(f != null) {
                            BasisSet.current().sele.lu.setLv(f?.unit, pcoin)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }

                val info = v.findViewById<ImageButton>(R.id.lineupunitinfo)

                info.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val form = f ?: return

                        val uid = form.unit.id

                        val intent = Intent(context, UnitInfo::class.java)

                        intent.putExtra("Data", JsonEncoder.encode(uid).toString())
                        context?.startActivity(intent)
                    }
                })
            }

            hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
            atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)

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

                    BasisSet.current().sele.lu.setLv(f?.unit, pcoin)

                    hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                    atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)

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

                    BasisSet.current().sele.lu.setLv(f?.unit, pcoin)

                    hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                    atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                }
            }

            if (f?.du?.pCoin != null) {
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
                    BasisSet.current().sele.lu.fs[StaticStore.position[0]][StaticStore.position[1]] = f?.unit?.forms?.get(fid % (f?.unit?.forms?.size ?: 2))
                else
                    line.repform = f?.unit?.forms?.get(fid % (f?.unit?.forms?.size ?: 2))

                f = f?.unit?.forms?.get(fid % (f?.unit?.forms?.size ?: 2))

                line.updateLineUp()
                line.toFormArray()

                val lev = spinners[0].selectedItem as Int
                val levp1 = spinners[1].selectedItem as Int

                pcoin[0] = lev + levp1

                hp.text = s.getHP(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)
                atk.text = s.getAtk(f, BasisSet.current().t(), f?.du?.pCoin != null && t.isChecked, pcoin)

                if (f?.du?.pCoin == null) {
                    setDisappear(t, tal)
                    pcoin = intArrayOf(0, 0, 0, 0, 0, 0)
                } else {
                    setAppear(t, tal)

                    pcoin = BasisSet.current().sele.lu.getLv(f)?.lvs ?: return@setOnClickListener

                    val max = f?.du?.pCoin?.max ?: intArrayOf(0)

                    for (i in 1 until max.size) {
                        val ii = i - 1

                        val list = ArrayList<Int>()

                        for (j in 0 until max[i] + 1)
                            list.add(j)

                        val adapter2 = ArrayAdapter(requireContext(), R.layout.spinneradapter, list)

                        talents[i - 1]?.adapter = adapter2

                        talents[i - 1]?.setSelection(getIndex(talents[i-1], pcoin[i]))

                        talents[i - 1]?.setOnLongClickListener {
                            talents[ii]?.isClickable = false
                            StaticStore.showShortMessage(context, s.getTalentName(ii, f))

                            true
                        }
                    }

                    if (pcoin[1] == 0 && pcoin[2] == 0 && pcoin[3] == 0 && pcoin[4] == 0 && pcoin[5] == 0) {
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

    private fun getIndex(spinner: Spinner?, lev: Int): Int {
        var index = 0

        for (i in 0 until (spinner?.count ?: 0))
            if (lev == spinner?.getItemAtPosition(i) as Int)
                index = i

        return index
    }

    private fun removePCoin() {
        for(i in 1 until pcoin.size) {
            pcoin[i] = 0
        }
    }
}
