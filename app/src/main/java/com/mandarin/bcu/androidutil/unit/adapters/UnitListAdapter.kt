package com.mandarin.bcu.androidutil.unit.adapters

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
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class UnitListAdapter(context: Context, private val name: ArrayList<String>, private val locate: ArrayList<Int>, private val pid: Int) : ArrayAdapter<String?>(context, R.layout.listlayout, name.toTypedArray()) {

    private class ViewHolder constructor(row: View) {
        var id: TextView = row.findViewById(R.id.unitID)
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

        val p = Pack.map[pid] ?: return row

        if(position < 0 || position >= name.size || position >= locate.size)
            return row

        val info = name[position].split("/")

        if(info.size != 2) {
            Log.w("ListAdapter","Invalid Format : "+ name[position])

            holder.id.visibility = View.GONE
            holder.title.text = name[position]
        } else {
            holder.id.text = info[0]
            holder.title.text = info[1]
        }

        holder.image.setImageBitmap(StaticStore.MakeIcon(context, p.us.ulist.list[locate[position]].forms[0].anim.uni.img.bimg() as Bitmap, 48f))

        return row
    }

}