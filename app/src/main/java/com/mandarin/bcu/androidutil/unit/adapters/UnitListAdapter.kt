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
    inner class ViewHolder(row: View) {
        private var initialized = false

        val id = row.findViewById<AutoMarquee>(R.id.unitID)!!
        val title = row.findViewById<TextView>(R.id.unitname)!!
        val image = row.findViewById<ImageView>(R.id.uniticon)!!
        private val fadeout = row.findViewById<View>(R.id.fadeout)!!

        fun initialize(position: Int) {
            if (initialized)
                return

            if (isEnabled(position)) {
                fadeout.visibility = View.GONE
            } else {
                fadeout.visibility = View.VISIBLE
            }

            val u = Identifier.get(name[position]) ?: return

            id.text = StaticStore.generateIdName(name[position], context)
            id.isSelected = true
            title.text = MultiLangCont.get(u.forms[0]) ?: u.forms[0].names.toString()
            title.isSelected = true

            val icon = u.forms[0].anim?.uni?.img?.bimg() ?: return

            image.setImageBitmap(StaticStore.makeIcon(context, icon as Bitmap, 48f))

            initialized = true
        }
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

        holder.initialize(position)

        return row
    }

    override fun isEnabled(position: Int): Boolean {
        val u = Identifier.get(name[position]) ?: return false

        return !u.forms.any { f -> f.anim == null }
    }
}