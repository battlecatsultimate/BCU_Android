package com.mandarin.bcu.androidutil.lineup.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.filter.FilterEntity
import com.mandarin.bcu.androidutil.lineup.LineUpView
import common.battle.BasisSet
import common.pack.Identifier
import common.util.stage.Stage
import common.util.unit.Form
import common.util.unit.Unit

class LUUnitList : Fragment() {
    private lateinit var line: LineUpView
    private var stage: Stage? = null
    private var star = 0
    private var numbers = ArrayList<Identifier<Unit>>()

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.lineup_unit_list, group, false)

        numbers = FilterEntity.setLuFilter()

        val adapter = LUUnitListAdapter(requireActivity(), numbers, stage)

        val ulist = view.findViewById<ListView>(R.id.lineupunitlist)

        ulist.adapter = adapter

        ulist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if(position < 0 || position >= numbers.size)
                return@OnItemClickListener

            val u = Identifier.get(numbers[position]) ?: return@OnItemClickListener

            val f = u.forms[u.forms.size - 1]

            if (alreadyExist(f))
                return@OnItemClickListener

            val posit = StaticStore.getPossiblePosition(BasisSet.current().sele.lu.fs)

            if (posit[0] != 100)
                BasisSet.current().sele.lu.fs[posit[0]][posit[1]] = f
            else
                line.repform = f

            line.updateLineUp()
            line.syncLineUp()
            line.invalidate()

            sync()
        }

        if (this::line.isInitialized) {
            adapter.lineup = line
        }

        return view
    }

    fun update() {
        val v = view ?: return

        val unitList = v.findViewById<ListView>(R.id.lineupunitlist)

        numbers.clear()
        numbers = FilterEntity.setLuFilter()

        val adapter1 = LUUnitListAdapter(requireActivity(), numbers, stage)

        unitList.adapter = adapter1
    }

    fun sync() {
        val v = view ?: return

        val unitList = v.findViewById<ListView>(R.id.lineupunitlist)
        val adapter = unitList.adapter ?: return

        if (adapter !is LUUnitListAdapter)
            return

        var index = unitList.firstVisiblePosition

        for (child in unitList.children) {
            val tag = child.tag

            if (tag is LUUnitListAdapter.ViewHolder) {
                tag.updateComponents(index++)
            }
        }
    }

    private fun alreadyExist(form: Form): Boolean {
        val u = form.unit

        for (i in BasisSet.current().sele.lu.fs.indices) {
            for (j in BasisSet.current().sele.lu.fs[i].indices) {

                if (BasisSet.current().sele.lu.fs[i][j] == null) {
                    return if (line.repform == null)
                        false
                    else
                        u == line.repform!!.unit

                }

                val u2 = BasisSet.current().sele.lu.fs[i][j].unit

                if (u == u2)
                    return true
            }
        }

        return false
    }

    fun setArguments(line: LineUpView, stage: Stage? = null, star: Int = 0) {
        this.line = line
        this.stage = stage
        this.star = star

        val v = view ?: return

        val unitList = v.findViewById<ListView>(R.id.lineupunitlist)
        val adapter = unitList.adapter ?: return

        if(adapter is LUUnitListAdapter) {
            adapter.lineup = line
        }
    }

    companion object {

        fun newInstance(line: LineUpView, stage: Stage? = null, star: Int = 0): LUUnitList {
            val ulist = LUUnitList()
            ulist.setArguments(line, stage, star)

            return ulist
        }
    }
}
