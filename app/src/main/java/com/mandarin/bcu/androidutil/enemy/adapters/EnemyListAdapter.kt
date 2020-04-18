package com.mandarin.bcu.androidutil.enemy.adapters

import android.app.Activity
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
import java.util.*

class EnemyListAdapter(activity: Activity, private val name: Array<String>, private val location: ArrayList<Int>) : ArrayAdapter<String?>(activity, R.layout.listlayout, name) {

    private class ViewHolder constructor(row: View) {
        val id: TextView = row.findViewById(R.id.unitID)
        val title: TextView = row.findViewById(R.id.unitname)
        val img: ImageView = row.findViewById(R.id.uniticon)

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

        val info = name[position].split("/")

        if(info.size != 2) {
            Log.w("EnemyListAdapter","Invalid format : "+name[position])

            holder.title.text = name[position]
        } else {
            holder.id.text = info[0]
            holder.title.text = info[1]
        }

        if (StaticStore.enemies[location[position]].anim?.edi?.img != null)
            holder.img.setImageBitmap(StaticStore.getResizeb(StaticStore.enemies[location[position]].anim.edi.img.bimg() as Bitmap, context, 85f, 32f))
        else
            holder.img.setImageBitmap(StaticStore.empty(context, 85f, 32f))

        holder.img.setPadding(StaticStore.dptopx(8f, context), StaticStore.dptopx(12f, context), 0, StaticStore.dptopx(12f, context))
        return row
    }

}