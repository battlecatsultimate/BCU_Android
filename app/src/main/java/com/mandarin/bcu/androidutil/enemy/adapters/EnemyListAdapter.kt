package com.mandarin.bcu.androidutil.enemy.adapters

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
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Enemy

class EnemyListAdapter(context: Context, private val name: ArrayList<Identifier<AbEnemy>>) : ArrayAdapter<Identifier<AbEnemy>>(context, R.layout.listlayout, name.toTypedArray()) {
    inner class ViewHolder(row: View) {
        private var initialized = false

        val id = row.findViewById<TextView>(R.id.unitID)!!
        val title = row.findViewById<TextView>(R.id.unitname)!!
        val image = row.findViewById<ImageView>(R.id.uniticon)!!
        private val fadeout = row.findViewById<View>(R.id.fadeout)!!

        fun initialize(position: Int) {
            if (initialized)
                return

            val e = name[position].get() ?: return

            if(e !is Enemy) {
                Log.e("ENL", "TYPE : "+e.javaClass.name)

                return
            }

            fadeout.visibility = View.GONE

            id.text = generateName(name[position])

            title.text = MultiLangCont.get(e) ?: e.names.toString()

            val icon = e.anim?.edi?.img?.bimg()

            if (icon != null)
                image.setImageBitmap(StaticStore.getResizeb(icon as Bitmap, context, 85f, 32f))
            else
                image.setImageBitmap(StaticStore.empty(context, 85f, 32f))

            initialized = true
        }
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.listlayout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        holder.initialize(position)

        return row
    }

    private fun generateName(id: Identifier<AbEnemy>) : String {
        return if(id.pack == Identifier.DEF) {
            context.getString(R.string.pack_default) + " - " + Data.trio(id.id)
        } else {
            StaticStore.getPackName(id.pack) + " - " + Data.trio(id.id)
        }
    }

    override fun isEnabled(position: Int): Boolean {
        val e = Identifier.get(name[position]) ?: return false

        if (e !is Enemy)
            return false

        return e.anim != null
    }
}