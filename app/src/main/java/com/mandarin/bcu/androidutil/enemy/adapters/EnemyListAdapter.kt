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
import common.battle.data.CustomEnemy
import common.pack.Identifier
import common.pack.UserProfile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Enemy

class EnemyListAdapter(context: Context, private val name: ArrayList<Identifier<AbEnemy>>, private val pid: String) : ArrayAdapter<Identifier<AbEnemy>>(context, R.layout.listlayout, name.toTypedArray()) {

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
            row = inf.inflate(R.layout.listlayout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val e = name[position].get() ?: return row

        if(e !is Enemy) {
            Log.e("ENL", "TYPE : "+e.javaClass.name)

            return row
        }

        holder.id.text = generateName(name[position])

        holder.title.text = MultiLangCont.get(e) ?: e.name ?: ""

        val icon = e.anim?.edi?.img?.bimg()

        if (icon != null)
            holder.image.setImageBitmap(StaticStore.getResizeb(icon as Bitmap, context, 85f, 32f))
        else
            holder.image.setImageBitmap(StaticStore.empty(context, 85f, 32f))

        return row
    }

    private fun generateName(id: Identifier<AbEnemy>) : String {
        return if(id.pack == Identifier.DEF) {
            context.getString(R.string.pack_default) + " - " + Data.trio(id.id)
        } else {
            StaticStore.getPackName(id.pack) + " - " + Data.trio(id.id)
        }
    }

}