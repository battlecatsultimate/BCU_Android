package com.mandarin.bcu.androidutil.unit.adapters

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
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.Unit

class UnitListAdapter(context: Context, private val name: ArrayList<Identifier<Unit>>) : ArrayAdapter<Identifier<Unit>>(context, R.layout.listlayout, name.toTypedArray()) {

    private class ViewHolder constructor(row: View) {
        var id: AutoMarquee = row.findViewById(R.id.unitID)
        var title: TextView = row.findViewById(R.id.unitname)
        var image: ImageView = row.findViewById(R.id.uniticon)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.listlayout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val u = Identifier.get(name[position]) ?: return row

        holder.id.text = StaticStore.generateIdName(name[position], context)
        holder.id.isSelected = true
        holder.title.text = MultiLangCont.get(u.forms[0]) ?: u.forms[0].name ?: ""
        holder.title.isSelected = true

        holder.image.setImageBitmap(StaticStore.makeIcon(context, u.forms[0].anim.uni.img.bimg() as Bitmap, 48f))

        return row
    }

}