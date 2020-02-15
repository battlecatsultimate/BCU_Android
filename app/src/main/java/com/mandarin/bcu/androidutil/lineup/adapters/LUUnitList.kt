package com.mandarin.bcu.androidutil.lineup.adapters

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.FilterEntity
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.unit.adapters.UnitListAdapter
import common.battle.BasisSet
import common.util.unit.Form
import java.util.*

class LUUnitList : Fragment() {
    private var line: LineUpView? = null
    private val handler = Handler()
    private var runnable: Runnable? = null

    private var destroyed = false
    private var numbers = ArrayList<Int>()

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.lineup_unit_list, group, false)

        if (line == null) {
            if (activity != null)
                line = activity!!.findViewById(R.id.lineupView)
        }

        if (arguments == null) return view

        val entity = FilterEntity(StaticStore.unitnumber)
        numbers = entity.setFilter()
        val names = ArrayList<String>()

        for (i in numbers) {
            names.add(StaticStore.names[i])
        }

        val adapter = UnitListAdapter(activity!!, names.toTypedArray(), numbers)

        val ulist = view.findViewById<ListView>(R.id.lineupunitlist)

        ulist.adapter = adapter

        runnable = object : Runnable {
            override fun run() {
                if (StaticStore.updateList) {
                    val entity1 = FilterEntity(StaticStore.unitnumber)
                    numbers.clear()
                    numbers = entity1.setFilter()
                    val names1 = ArrayList<String>()

                    for (i in numbers) {
                        names1.add(StaticStore.names[i])
                    }

                    val adapter1 = UnitListAdapter(activity!!, names1.toTypedArray(), numbers)

                    ulist.adapter = adapter1

                    StaticStore.updateList = false
                }

                if (!destroyed)
                    handler.postDelayed(this, 50)
            }
        }

        handler.postDelayed(runnable, 50)

        ulist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val f = StaticStore.units[numbers[position]].forms[StaticStore.units[numbers[position]].forms.size - 1]

            if (alreadyExist(f)) return@OnItemClickListener

            val posit = StaticStore.getPossiblePosition(BasisSet.current.sele.lu.fs)

            if (posit[0] != 100)
                BasisSet.current.sele.lu.fs[posit[0]][posit[1]] = f
            else
                line!!.repform = f

            line!!.updateLineUp()
            line!!.toFormArray()
            line!!.invalidate()
        }

        return view
    }

    private fun alreadyExist(form: Form): Boolean {
        val u = form.unit

        for (i in BasisSet.current.sele.lu.fs.indices) {
            for (j in BasisSet.current.sele.lu.fs[i].indices) {
                if (BasisSet.current.sele.lu.fs[i][j] == null) {
                    return if (line!!.repform == null) false else u == line!!.repform!!.unit

                }

                val u2 = BasisSet.current.sele.lu.fs[i][j].unit

                if (u == u2)
                    return true
            }
        }

        return false
    }

    override fun onDestroy() {
        destroyed = !destroyed
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    private fun setLineUp(line: LineUpView) {
        this.line = line
    }

    companion object {

        fun newInstance(names: Array<String>, line: LineUpView): LUUnitList {
            val ulist = LUUnitList()
            val bundle = Bundle()
            bundle.putStringArray("Names", names)
            ulist.arguments = bundle
            ulist.setLineUp(line)

            return ulist
        }
    }
}
