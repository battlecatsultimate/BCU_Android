package com.mandarin.bcu.androidutil.music.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.pack.Identifier
import common.util.Data
import common.util.stage.Music

class MusicListAdapter(context: Context, private val name: ArrayList<Identifier<Music>>, private val pid: String, private val player: Boolean) : ArrayAdapter<Identifier<Music>>(context, R.layout.map_list_layout, name.toTypedArray()) {
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

        holder.title.text = generateName(name[position])

        holder.duration.text = if(player) {
            if(position >= StaticStore.musicData.size) {
                ""
            } else {
                val info = StaticStore.musicData[position]

                val p = StaticStore.musicnames[info.pack]

                if(p == null) {
                    ""
                } else {
                    p[info.id] ?: ""
                }
            }
        } else {
            val ms = StaticStore.musicnames[pid]

            if(ms == null)
                ""
            else {
                ms[name[position].id] ?: ""
            }
        }


        return row
    }

    private fun generateName(id: Identifier<Music>) : String {
        return if(id.pack == Identifier.DEF) {
            context.getString(R.string.pack_default) +" - "+ Data.trio(id.id)
        } else {
            StaticStore.getPackName(id.pack)+" - "+ Data.trio(id.id)
        }
    }
}