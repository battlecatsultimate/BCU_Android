package com.mandarin.bcu.androidutil.lineup.adapters

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.util.pack.Pack

class LUUnitListAdapter(context: Context, private val names: ArrayList<String>, private val numbers: ArrayList<Int>) : ArrayAdapter<String?>(context, R.layout.listlayout, names.toTypedArray()) {
    private class ViewHolder constructor(row: View) {
        var id: TextView = row.findViewById(R.id.unitID)
        var title: TextView = row.findViewById(R.id.unitname)
        var image: ImageView = row.findViewById(R.id.uniticon)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.listlayout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = convertView
            holder =  row.tag as ViewHolder
        }

        if(position < 0 || position >= numbers.size) {
            holder.title.visibility = View.GONE
            holder.image.visibility = View.GONE

            return row
        }

        if(numbers[position] < 0 || numbers[position] >= StaticStore.ludata.size) {
            holder.title.visibility = View.GONE
            holder.image.visibility = View.GONE

            return row
        }

        val info = StaticStore.ludata[numbers[position]].split("-")

        if(info.size < 2) {
            holder.title.visibility = View.GONE
            holder.image.visibility = View.GONE

            return row
        }

        val pid = info[0].toInt()

        val p = Pack.map[pid]

        if(p == null) {
            holder.title.visibility = View.GONE
            holder.image.visibility = View.GONE

            return row
        }

        val id = info[1].toInt()

        if(id < 0 || id >= p.us.ulist.list.size) {
            holder.title.visibility = View.GONE
            holder.image.visibility = View.GONE

            return row
        }

        val u = p.us.ulist.list[id]

        val icon = u.forms[0].anim.uni?.img?.bimg()

        val nameInfo = names[position].split("/")

        if(info.size != 2) {
            Log.w("ListAdapter","Invalid Format : "+ names[position])

            holder.id.visibility = View.GONE
            holder.title.text = names[position]
        } else {
            holder.id.text = nameInfo[0]
            holder.title.text = nameInfo[1]
        }

        if(icon != null) {
            holder.image.setImageBitmap(StaticStore.MakeIcon(context, icon as Bitmap , 48f))
        } else {
            holder.image.setImageBitmap(StaticStore.MakeIcon(context, null , 48f))
        }

        return row
    }
}