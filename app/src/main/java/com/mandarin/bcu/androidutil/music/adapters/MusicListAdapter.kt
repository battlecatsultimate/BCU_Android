package com.mandarin.bcu.androidutil.music.adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.util.pack.Pack

class MusicListAdapter(context: Context, private val name: Array<String?>, private val locate: ArrayList<Int>) : ArrayAdapter<String?>(context, R.layout.map_list_layout, name) {
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
        holder.duration.text =
                if(position >= StaticStore.musicnames.size)
                    getDuration(locate[position])
                else
                    StaticStore.musicnames[position]

        return row
    }

    private fun getDuration(posit: Int) : String {
        var duration = ""

        val f = Pack.def.ms.get(posit)

        if(f != null) {
            val uri  = Uri.parse(f.absolutePath)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context,uri)

            var time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toFloat()/1000f

            val min = (time/60f).toInt()

            time -= min.toFloat()*60f

            val sec = time.toInt()

            val mins = if(min < 10) "0$min"
                else min.toString()

            val secs = if(sec < 10) "0$sec"
                else sec.toString()

            duration = "$mins:$secs"

            StaticStore.musicnames.add(duration)
        }

        return duration
    }
}