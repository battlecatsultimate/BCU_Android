package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.util.stage.SCDef
import java.util.*

class StageListAdapter(private val activity: Activity, private val stages: Array<String?>, private val mapcode: Int, private val stid: Int) : ArrayAdapter<String?>(activity, R.layout.stage_list_layout, stages) {

    private class ViewHolder constructor(row: View) {
        var name: TextView = row.findViewById(R.id.stagename)
        var icons: FlexboxLayout = row.findViewById(R.id.enemicon)
        var images: MutableList<ImageView?> = ArrayList()

    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.stage_list_layout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        holder.name.text = stages[position]
        holder.images.clear()
        holder.icons.removeAllViews()
        val mc = StaticStore.map[mapcode] ?: return row
        val stm = mc.maps[stid]
        val st = stm.list[position]
        val ids = getid(st.data)
        val icons = arrayOfNulls<ImageView>(ids.size)
        for (i in ids.indices) {
            icons[i] = ImageView(activity)
            icons[i]!!.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            icons[i]!!.setImageBitmap(StaticStore.eicons[ids[i]])
            icons[i]!!.setPadding(StaticStore.dptopx(12f, activity), StaticStore.dptopx(4f, activity), 0, StaticStore.dptopx(4f, activity))
            holder.icons.addView(icons[i])
            holder.images.add(icons[i])
        }
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

}