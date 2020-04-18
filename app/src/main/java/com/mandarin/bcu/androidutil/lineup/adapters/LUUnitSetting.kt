package com.mandarin.bcu.androidutil.lineup.adapters

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.lineup.LineUpView
import common.battle.BasisSet
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

    private var line: LineUpView? = null
    private var pcoin = intArrayOf(0, 0, 0, 0, 0, 0)
    private val zeros = intArrayOf(0, 0, 0, 0, 0, 0)
    private var destroyed = false

    private var fid = 0

    private val handler = Handler()

    var f: Form? = null

    override fun onDestroy() {
        destroyed = true
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View? {
        val v = inflater.inflate(R.layout.lineup_unit_set, group, false)

        update(v)

        val runnable = object : Runnable {
            override fun run() {
                if(StaticStore.updateForm) {
                    update(v)
                    StaticStore.updateForm = false
                }

                if(!destroyed)
                    handler.postDelayed(this,50)
            }
        }

        handler.postDelayed(runnable, 50)

        return v
    }

    private fun update(v: View) {
        val spinners = arrayOf(v.findViewById(R.id.lineuplevspin), v.findViewById<Spinner>(R.id.lineuplevpspin))
        val plus = v.findViewById<TextView>(R.id.lineuplevplus)
        val row = v.findViewById<TableRow>(R.id.lineupunittable)
        val tal = v.findViewById<TableRow>(R.id.lineuppcoin)
        val t = v.findViewById<CheckBox>(R.id.lineuptalent)
        val hp = v.findViewById<TextView>(R.id.lineupunithp)
        val atk = v.findViewById<TextView>(R.id.lineupunitatk)
        val chform = v.findViewById<Button>(R.id.lineupchform)
        val levt = v.findViewById<TextView>(R.id.lineupunitlevt)

        if (line == null) {
            if (activity == null)
                return

            line = activity?.findViewById(R.id.lineupView)
        }

        f = if (StaticStore.position[0] == -1)
            null
        else if (StaticStore.position[0] == 100)
            line?.repform
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

            setAppear(spinners[0], spinners[1], plus, row, t, tal, chform, levt)

            val s = GetStrings(context!!)

            fid = f?.fid ?: 0

            if (f?.unit?.maxp == 0)
                setDisappear(spinners[1], plus)

            val id = intArrayOf(R.id.lineupp, R.id.lineupp1, R.id.lineupp2, R.id.lineupp3, R.id.lineupp4)

            val talents = arrayOfNulls<Spinner>(id.size)

            for (i in id.indices) {
                talents[i] = v.findViewById(id[i])
            }

            if (f?.pCoin != null) {
                pcoin = BasisSet.current.sele.lu.getLv(f?.unit)

                val max = f?.pCoin?.max

                for (i in 1 until (max?.size ?: 1)) {
                    val ii = i - 1

                    val list = ArrayList<Int>()

                    for (j in 0 until (max?.get(i) ?: 0) + 1)
                        list.add(j)

                    val adapter = ArrayAdapter(context!!, R.layout.spinneradapter, list)

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

            val adapter = ArrayAdapter(context!!, R.layout.spinneradapter, levs)
            val adapter1 = ArrayAdapter(context!!, R.layout.spinneradapter, levp)

            spinners[0].adapter = adapter
            spinners[1].adapter = adapter1

            var loadlev = BasisSet.current.sele.lu.getLv(f?.unit)[0]

            var loadlevp = 0

            if (loadlev > (f?.unit?.max ?: 0)) {
                loadlevp = loadlev - (f?.unit?.max ?: 0)
                loadlev = (f?.unit?.max ?: 0)
            }

            val floadlev = loadlev
            val floadlevp = loadlevp

            spinners[0].setSelection(getIndex(spinners[0], floadlev))
            spinners[1].setSelection(getIndex(spinners[1], floadlevp))

            spinners[0].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    val levs1: IntArray

                    if (t.isChecked) {
                        levs1 = intArrayOf(lev + levp1, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5])
                        hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                    } else {
                        levs1 = intArrayOf(lev + levp1, 0, 0, 0, 0, 0)
                        hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f != null && f?.pCoin != null && t.isChecked, zeros)
                        atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f != null && f?.pCoin != null && t.isChecked, zeros)
                    }

                    if (f != null)
                        BasisSet.current.sele.lu.setLv(f?.unit, levs1)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }

            spinners[1].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    val levs1: IntArray

                    if (t.isChecked) {
                        levs1 = intArrayOf(lev + levp1, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5])
                        hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                        atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                    } else {
                        levs1 = intArrayOf(lev + levp1, 0, 0, 0, 0, 0)
                        hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, zeros)
                        atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, zeros)
                    }

                    if(f != null)
                        BasisSet.current.sele.lu.setLv(f?.unit, levs1)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }

            for (i in talents.indices) {

                talents[i]?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                        pcoin[i + 1] = position

                        val lev = spinners[0].selectedItem as Int
                        val levp1 = spinners[1].selectedItem as Int

                        val levs1: IntArray

                        if (t.isChecked) {
                            levs1 = intArrayOf(lev + levp1, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5])
                            hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                            atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                        } else {
                            levs1 = intArrayOf(lev + levp1, zeros[1], zeros[2], zeros[3], zeros[4], zeros[5])
                            hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, zeros)
                            atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, zeros)
                        }

                        if(f != null) {
                            BasisSet.current.sele.lu.setLv(f?.unit, levs1)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }

                val info = v.findViewById<ImageButton>(R.id.lineupunitinfo)

                info.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(context, UnitInfo::class.java)

                        val pid = f?.unit?.pack?.id ?: 0

                        intent.putExtra("PID", pid)

                        val ids = if(pid != 0) {
                            StaticStore.getID(f?.unit?.id ?: 0)
                        } else {
                            f?.unit?.id ?: 0
                        }

                        intent.putExtra("ID", ids)
                        context?.startActivity(intent)
                    }
                })

                hp.text = s.getHP(f, BasisSet.current.t(), loadlev + loadlevp, f?.pCoin != null && t.isChecked, pcoin)
                atk.text = s.getAtk(f, BasisSet.current.t(), loadlev + loadlevp, f?.pCoin != null && t.isChecked, pcoin)
            }

            t.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val anim = ValueAnimator.ofInt(0, StaticStore.dptopx(64f, context!!))
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

                    val levs1 = intArrayOf(lev + levp1, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5])

                    BasisSet.current.sele.lu.setLv(f?.unit, levs1)

                    hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                    atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)

                } else {
                    val anim = ValueAnimator.ofInt(StaticStore.dptopx(64f, context!!), 0)
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

                    val levs1 = intArrayOf(lev + levp1, zeros[1], zeros[2], zeros[3], zeros[4], zeros[5])

                    BasisSet.current.sele.lu.setLv(f?.unit, levs1)

                    hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, zeros)
                    atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, zeros)
                }
            }

            if (f?.pCoin != null) {
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
                    BasisSet.current.sele.lu.fs[StaticStore.position[0]][StaticStore.position[1]] = f?.unit?.forms?.get(fid % (f?.unit?.forms?.size ?: 2))
                else
                    line?.repform = f?.unit?.forms?.get(fid % (f?.unit?.forms?.size ?: 2))

                f = f?.unit?.forms?.get(fid % (f?.unit?.forms?.size ?: 2))

                line?.updateLineUp()
                line?.toFormArray()

                val lev = spinners[0].selectedItem as Int
                val levp1 = spinners[1].selectedItem as Int

                hp.text = s.getHP(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)
                atk.text = s.getAtk(f, BasisSet.current.t(), lev + levp1, f?.pCoin != null && t.isChecked, pcoin)

                if (f?.pCoin == null) {
                    setDisappear(t, tal)
                    pcoin = intArrayOf(0, 0, 0, 0, 0, 0)
                } else {
                    setAppear(t, tal)

                    pcoin = BasisSet.current.sele.lu.getLv(f?.unit)

                    val max = f?.pCoin?.max ?: intArrayOf(0)

                    for (i in 1 until max.size) {
                        val ii = i - 1

                        val list = ArrayList<Int>()

                        for (j in 0 until max[i] + 1)
                            list.add(j)

                        val adapter2 = ArrayAdapter(context!!, R.layout.spinneradapter, list)

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

                line?.invalidate()
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
}
