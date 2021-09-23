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
import common.pack.Identifier
import common.pack.UserProfile
import common.util.lang.MultiLangCont
import common.util.unit.Combo

class ComboSubSchListAdapter internal constructor(private val activity: Activity, private val sch: List<String?>, private val combolist: ListView, private val defcom: List<Int>, private var comboListAdapter: ComboListAdapter?) : ArrayAdapter<String?>(activity, R.layout.spinneradapter, sch) {
    private val combocat: BooleanArray = BooleanArray(sch.size)
    private val comid = ArrayList<Int>()

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

        holder.category.setBackgroundColor(
            if (combocat[position])
                StaticStore.getAttributeColor(context, R.attr.SelectionPrimary)
            else
                context.getColor(android.R.color.transparent)
        )

        holder.category.setOnClickListener {
            combocat[position] = !combocat[position]

            holder.category.setBackgroundColor(
                if (combocat[position])
                    StaticStore.getAttributeColor(context, R.attr.SelectionPrimary)
                else
                    context.getColor(android.R.color.transparent)
            )

            if (combocat[position]) {
                comid.add(defcom[position])
            } else {
                comid.remove(defcom[position])
            }

            StaticStore.combos.clear()

            if (comid.isEmpty()) {
                StaticStore.combos.addAll(UserProfile.getBCData().combos.list)

                for(userPack in UserProfile.getUserPacks()) {
                    for(combo in userPack.combos.list) {
                        combo ?: continue

                        StaticStore.combos.add(combo)
                    }
                }

                StaticStore.combos.sortWith(Comparator.comparingInt(Combo::type).thenComparingInt(Combo::lv))

                val names = Array<String>(StaticStore.combos.size) {
                    if(StaticStore.combos[it].id.pack == Identifier.DEF) {
                        MultiLangCont.getStatic().COMNAME.getCont(StaticStore.combos[it])
                    } else {
                        StaticStore.combos[it].name
                    }
                }

                comboListAdapter = ComboListAdapter(activity, names)
                combolist.adapter = comboListAdapter
            } else {
                for(pack in UserProfile.getAllPacks()) {
                    for(c in pack.combos.list) {
                        c ?: continue

                        for(l in comid) {
                            if(c.type == l)
                                StaticStore.combos.add(c)
                        }
                    }
                }

                StaticStore.combos.sortWith(Comparator.comparingInt(Combo::type).thenComparingInt(Combo::lv))

                val names = Array<String>(StaticStore.combos.size) {
                    if(StaticStore.combos[it].id.pack == Identifier.DEF) {
                        MultiLangCont.getStatic().COMNAME.getCont(StaticStore.combos[it])
                    } else {
                        StaticStore.combos[it].name
                    }
                }

                comboListAdapter = ComboListAdapter(activity, names)
                combolist.adapter = comboListAdapter
            }
        }
        return row
    }

}