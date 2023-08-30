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

class ComboSchListAdapter internal constructor(private val activity: Activity, private val sch: Array<String?>, private val schlst2: ListView, private val combolist: ListView, private var comboListAdapter: ComboListAdapter?) : ArrayAdapter<String?>(activity, R.layout.spinneradapter, sch) {
    private val combocat = BooleanArray(5)
    private val locater = arrayOf(intArrayOf(0, 1, 2), intArrayOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), intArrayOf(3, 6, 7, 10), intArrayOf(5, 4, 9), intArrayOf(11, 12, 13))
    private val locateid = arrayOf(intArrayOf(R.string.combo_atk, R.string.combo_hp, R.string.combo_spd), intArrayOf(R.string.combo_strag, R.string.combo_md, R.string.combo_res, R.string.combo_kbdis, R.string.combo_sl, R.string.combo_st, R.string.combo_wea, R.string.combo_inc, R.string.combo_wit, R.string.combo_eva, R.string.combo_crit), intArrayOf(R.string.combo_caninch, R.string.combo_canatk, R.string.combo_canchtime, R.string.combo_bsh), intArrayOf(R.string.combo_initmon, R.string.combo_work, R.string.combo_wal), intArrayOf(R.string.combo_cd, R.string.combo_ac, R.string.combo_xp))
    private val comid: MutableList<String> = ArrayList()

    private class ViewHolder(view: View) {
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
                comid.add(position.toString())
            } else {
                comid.remove(position.toString())
            }
            StaticStore.combos.clear()
            if (comid.isEmpty()) {
                val locates: MutableList<Int> = ArrayList()

                for (ints1 in locater) {
                    for (j in ints1) {
                        locates.add(j)
                    }
                }

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

                val subsch: MutableList<String> = ArrayList()

                for (ints in locateid) {
                    for (anInt in ints) {
                        subsch.add(context.getString(anInt))
                    }
                }

                comboListAdapter = ComboListAdapter(activity, names)

                combolist.adapter = comboListAdapter

                val adapter = ComboSubSchListAdapter(activity, subsch, combolist, locates, comboListAdapter)

                schlst2.adapter = adapter
            } else {
                val locates: MutableList<Int> = ArrayList()
                val subsch: MutableList<String> = ArrayList()

                for (i in comid.indices) {
                    for (j in locater[comid[i].toInt()]) {
                        locates.add(j)
                    }
                }

                for (i in comid.indices) {
                    for (j in locateid[comid[i].toInt()]) {
                        subsch.add(context.getString(j))
                    }
                }

                for (i in locates.indices) {
                    for(pack in UserProfile.getAllPacks()) {
                        for(c in pack.combos.list) {
                            c ?: continue

                            for(l in locates) {
                                if(c.type == l)
                                    StaticStore.combos.add(c)
                            }
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

                val adapter = ComboSubSchListAdapter(activity, subsch, combolist, locates, comboListAdapter)

                combolist.adapter = comboListAdapter
                schlst2.adapter = adapter
            }
        }
        return row
    }

}