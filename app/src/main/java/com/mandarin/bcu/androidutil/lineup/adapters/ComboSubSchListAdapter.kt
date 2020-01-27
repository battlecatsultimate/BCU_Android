package com.mandarin.bcu.androidutil.lineup.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.system.MultiLangCont
import common.util.unit.Combo
import java.util.*

class ComboSubSchListAdapter internal constructor(private val activity: Activity, private val sch: List<String?>, private val combolist: ListView, private val defcom: List<Int>, private var comboListAdapter: ComboListAdapter?) : ArrayAdapter<String?>(activity, R.layout.spinneradapter, sch) {
    private val combocat: BooleanArray = BooleanArray(sch.size)
    private val comid: MutableList<String> = ArrayList()

    private class ViewHolder constructor(view: View) {
        var category: TextView = view.findViewById(R.id.spinnertext)

    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.spinneradapter,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        holder.category.text = sch[position]
        holder.category.setBackgroundColor(if (combocat[position]) StaticStore.getAttributeColor(context, R.attr.SelectionPrimary) else context.getColor(android.R.color.transparent))
        holder.category.setOnClickListener {
            combocat[position] = !combocat[position]
            holder.category.setBackgroundColor(if (combocat[position]) StaticStore.getAttributeColor(context, R.attr.SelectionPrimary) else context.getColor(android.R.color.transparent))
            if (combocat[position]) {
                comid.add(defcom[position].toString())
            } else {
                comid.remove(defcom[position].toString())
            }
            StaticStore.combos.clear()
            if (comid.isEmpty()) {
                for (i in defcom.indices) {
                    StaticStore.combos.addAll(listOf(*Combo.combos[defcom[i]]))
                }
                val names = arrayOfNulls<String>(StaticStore.combos.size)
                for (i in StaticStore.combos.indices) {
                    names[i] = MultiLangCont.COMNAME.getCont(StaticStore.combos[i].name)
                }
                comboListAdapter = ComboListAdapter(activity, names)
                combolist.adapter = comboListAdapter
            } else {
                for (i in comid.indices) {
                    StaticStore.combos.addAll(listOf(*Combo.combos[comid[i].toInt()]))
                }
                val names = arrayOfNulls<String>(StaticStore.combos.size)
                for (i in StaticStore.combos.indices) {
                    names[i] = MultiLangCont.COMNAME.getCont(StaticStore.combos[i].name)
                }
                comboListAdapter = ComboListAdapter(activity, names)
                combolist.adapter = comboListAdapter
            }
        }
        return row
    }

}