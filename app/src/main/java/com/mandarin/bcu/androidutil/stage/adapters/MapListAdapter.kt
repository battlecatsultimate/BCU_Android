package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mandarin.bcu.R
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.StageMap

class MapListAdapter(private val activity: Activity, private val maps: ArrayList<Identifier<StageMap>>) : ArrayAdapter<Identifier<StageMap>>(activity, R.layout.map_list_layout, maps.toTypedArray()) {

    private class ViewHolder constructor(row: View) {
        var name: TextView = row.findViewById(R.id.map_list_name)
        var count: TextView = row.findViewById(R.id.map_list_coutns)
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

        val stm = Identifier.get(maps[position]) ?: return row

        holder.name.text = withID(maps[position])

        val numbers: String
        numbers =
                if (stm.list.size() == 1)
                    stm.list.size().toString() + activity.getString(R.string.map_list_stage)
                else
                    stm.list.size().toString() + activity.getString(R.string.map_list_stages)
        holder.count.text = numbers
        return row
    }

    private fun withID(name: Identifier<StageMap>): String {
        val stm = Identifier.get(name) ?: return Data.trio(name.id)

        val n = MultiLangCont.get(stm) ?: stm.name ?: ""

        return if (n == "") {
            Data.trio(name.id)
        } else {
            Data.trio(name.id) + " - " + n
        }
    }

}