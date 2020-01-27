package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mandarin.bcu.R
import common.util.stage.MapColc

class MapListAdapter(private val activity: Activity, private val maps: Array<String>, private val mapcode: Int) : ArrayAdapter<String?>(activity, R.layout.map_list_layout, maps) {

    private class ViewHolder constructor(row: View) {
        var name: TextView = row.findViewById(R.id.map_list_name)
        var count: TextView = row.findViewById(R.id.map_list_coutns)

    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val mc = MapColc.MAPS[mapcode]
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

        holder.name.text = withID(position, maps[position])
        val numbers: String
        numbers = if (mc != null) if (mc.maps[position].list.size == 1) mc.maps[position].list.size.toString() + activity.getString(R.string.map_list_stage) else mc.maps[position].list.size.toString() + activity.getString(R.string.map_list_stages) else 0.toString() + activity.getString(R.string.map_list_stages)
        holder.count.text = numbers
        return row
    }

    private fun number(num: Int): String {
        return if (num in 0..9) "00$num" else if (num in 10..99) "0$num" else "" + num
    }

    private fun withID(id: Int, name: String): String {
        return if (name == "") {
            number(id)
        } else {
            number(id) + " - " + name
        }
    }

}