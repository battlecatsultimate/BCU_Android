package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.util.pack.Pack
import common.util.stage.SCDef
import java.util.*
import kotlin.collections.ArrayList

class CStageListAdapter(activity: Activity, private val stages: Array<String?>, private val mapcode: Int, private val stid: Int, private val positions: ArrayList<Int>, private val custom: Boolean) : ArrayAdapter<String?>(activity, R.layout.stage_list_layout, stages) {

    private class ViewHolder constructor(row: View) {
        var name: TextView = row.findViewById(R.id.map_list_name)
        var enemy: TextView = row.findViewById(R.id.map_list_coutns)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.map_list_layout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        holder.name.text = stages[position] ?: getStageName(position)

        val mc = if(custom) {
            Pack.map[mapcode]?.mc ?: return row
        } else {
            StaticStore.map[mapcode] ?: return row
        }

        val stm = mc.maps[stid] ?: return row

        val st = stm.list[positions[position]] ?: return row

        val ids = getid(st.data)

        val lang = Locale.getDefault().language

        val enemies = if(lang == "en" || lang == "ru" || lang == "fr") {
            getEnemyText(ids.size, lang)
        } else {
            context.getString(R.string.stg_enem_num).replace("_", ids.size.toString())
        }

        holder.enemy.text = enemies

        return row
    }

    private fun getid(stage: SCDef): List<Int> {
        val result: MutableList<IntArray?> = ArrayList()
        val data = reverse(stage.datas)
        for (datas in data) {
            if (result.isEmpty()) {
                result.add(datas)
                continue
            }
            val id = datas!![SCDef.E]
            if (haveSame(id, result)) {
                result.add(datas)
            }
        }
        val ids: MutableList<Int> = ArrayList()
        for (datas in result) {
            ids.add(datas!![SCDef.E])
        }
        return ids
    }

    private fun haveSame(id: Int, result: List<IntArray?>): Boolean {
        if (id == 19 || id == 20 || id == 21) return false
        for (data in result) {
            if (id == data!![SCDef.E]) return false
        }
        return true
    }

    private fun reverse(data: Array<IntArray>): Array<IntArray?> {
        val result = arrayOfNulls<IntArray>(data.size)
        for (i in data.indices) {
            result[i] = data[data.size - 1 - i]
        }
        return result
    }

    private fun getStageName(num: Int) : String {
        return "Stage"+number(num)
    }

    private fun number(num: Int): String {
        return if (num in 0..9) "00$num" else if (num in 10..99) "0$num" else "" + num
    }

    private fun getEnemyText(num: Int, lang: String) : String {
        return when(lang) {
            "en" -> {
                when(num) {
                    1 -> "$num Enemy"
                    else -> "$num Enemies"
                }
            }
            "ru" -> {
                when(num) {
                    1 -> "$num враг"
                    else -> "$num враги"
                }
            }
            "fr" -> {
                when(num) {
                    1 -> "$num Enemmi"
                    else -> "$num Ennemis"
                }
            }
            else -> {
                "$num"
            }
        }
    }
}