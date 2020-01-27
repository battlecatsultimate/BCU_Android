package com.mandarin.bcu.androidutil.unit.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import java.util.*

class UnitListAdapter(context: Activity, private val name: Array<String>, private val locate: ArrayList<Int>) : ArrayAdapter<String?>(context, R.layout.listlayout, name) {

    private class ViewHolder constructor(row: View) {
        var title: TextView = row.findViewById(R.id.unitname)
        var image: ImageView = row.findViewById(R.id.uniticon)

    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context);
            row = inf.inflate(R.layout.listlayout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }
        holder.title.text = name[position]
        holder.image.setImageBitmap(StaticStore.MakeIcon(context, StaticStore.units[locate[position]].forms[0].anim.uni.img.bimg() as Bitmap, 48f))

        return row
    }

}