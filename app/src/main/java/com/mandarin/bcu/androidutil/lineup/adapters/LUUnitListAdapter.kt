package com.mandarin.bcu.androidutil.lineup.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.Unit

class LUUnitListAdapter(context: Context, private val numbers: ArrayList<Identifier<Unit>>) : ArrayAdapter<Identifier<Unit>>(context, R.layout.listlayout, numbers.toTypedArray()) {
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

        val u = Identifier.get(numbers[position]) ?: return row

        val icon = u.forms[0].anim.uni?.img?.bimg()

        holder.id.text = generateID(numbers[position])

        holder.title.text = MultiLangCont.get(u.forms[0]) ?: u.forms[0].names.toString()

        if(icon != null) {
            holder.image.setImageBitmap(StaticStore.makeIcon(context, icon as Bitmap , 48f))
        } else {
            holder.image.setImageBitmap(StaticStore.makeIcon(context, null , 48f))
        }

        return row
    }

    private fun generateID(id: Identifier<Unit>) : String {
        return if(id.pack == Identifier.DEF) {
            context.getString(R.string.pack_default)+" - "+ Data.trio(id.id)
        } else {
            StaticStore.getPackName(id.pack) + " - " + Data.trio(id.id)
        }
    }
}