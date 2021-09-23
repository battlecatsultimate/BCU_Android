package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.unit.AbEnemy

class StageListAdapter(private val activity: Activity, private val stages: Array<Identifier<Stage>>) : ArrayAdapter<Identifier<Stage>>(activity, R.layout.stage_list_layout, stages) {

    private class ViewHolder constructor(row: View) {
        var name: TextView = row.findViewById(R.id.stagename)
        var icons: FlexboxLayout = row.findViewById(R.id.enemicon)
        var images: MutableList<ImageView?> = ArrayList()

    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.stage_list_layout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val st = Identifier.get(stages[position]) ?: return row

        println(MultiLangCont.get(st))

        holder.name.text = MultiLangCont.get(st) ?: st.name ?: getStageName(stages[position].id)
        holder.images.clear()
        holder.icons.removeAllViews()

        val ids = getid(st.data)

        val icons = arrayOfNulls<ImageView>(ids.size)

        for (i in ids.indices) {
            icons[i] = ImageView(activity)
            icons[i]?.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if(ids[i].id < StaticStore.eicons?.size ?: 0) {
                val ic = StaticStore.eicons?.get(ids[i].id) ?: StaticStore.empty(1,1)
                icons[i]?.setImageBitmap(ic)
            } else {
                icons[i]?.setImageBitmap(StaticStore.empty(context, 18f, 18f))
            }
            icons[i]?.setPadding(StaticStore.dptopx(12f, activity), StaticStore.dptopx(4f, activity), 0, StaticStore.dptopx(4f, activity))
            holder.icons.addView(icons[i])
            holder.images.add(icons[i])
        }

        return row
    }

    private fun getid(stage: SCDef): List<Identifier<AbEnemy>> {
        val result: MutableList<SCDef.Line?> = ArrayList()
        val data = reverse(stage.datas)
        for (datas in data) {
            if (result.isEmpty()) {
                result.add(datas)
                continue
            }
            val id = datas!!.enemy
            if (haveSame(id, result)) {
                result.add(datas)
            }
        }
        val ids: MutableList<Identifier<AbEnemy>> = ArrayList()
        for (datas in result) {
            datas ?: continue
            ids.add(datas.enemy)
        }
        return ids
    }

    private fun haveSame(id: Identifier<AbEnemy>, result: List<SCDef.Line?>): Boolean {
        if (id.pack == Identifier.DEF && (id.id == 19 || id.id == 20 || id.id == 21))
            return false

        for (data in result) {
            data ?: continue
            if (id.equals(data.enemy)) return false
        }
        return true
    }

    private fun reverse(data: Array<SCDef.Line>): Array<SCDef.Line?> {
        val result = arrayOfNulls<SCDef.Line>(data.size)
        for (i in data.indices) {
            result[i] = data[data.size - 1 - i]
        }
        return result
    }

    private fun getStageName(num: Int) : String {
        return "Stage"+number(num)
    }

    private fun number(num: Int): String {
        return if (num in 0..9) "00$num" else if (num in 10..99) "0$num" else "" + num
    }

}