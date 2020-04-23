package com.mandarin.bcu.androidutil.music.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore

class MusicListAdapter(context: Context, private val name: ArrayList<String>, private val pid: Int, private val player: Boolean) : ArrayAdapter<String?>(context, R.layout.map_list_layout, name.toTypedArray()) {
    private class ViewHolder constructor(row: View) {
        val title: TextView = row.findViewById(R.id.map_list_name)
        val duration: TextView = row.findViewById(R.id.map_list_coutns)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row:View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.map_list_layout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = convertView
            holder = row.tag as ViewHolder
        }

        holder.title.text = name[position]

        holder.duration.text = if(player) {
            if(position >= StaticStore.musicData.size) {
                ""
            } else {
                val info = StaticStore.musicData[position].split("\\")

                if(info.size != 2) {
                    Log.e("MusicListAdapter", "Invalid String Format : "+StaticStore.musicData[position])
                    ""
                } else {
                    val p = StaticStore.musicnames[info[0].toInt()]

                    if(p == null) {
                        ""
                    } else {
                        if(info[1].toInt() >= p.size) {
                            ""
                        } else {
                            p[info[1].toInt()]
                        }
                    }
                }
            }
        } else {
            val ms = StaticStore.musicnames[pid]

            if(ms == null)
                ""
            else {
                if(position >= ms.size)
                    ""
                else
                    ms[position] ?: "00:00"
            }
        }


        return row
    }
}