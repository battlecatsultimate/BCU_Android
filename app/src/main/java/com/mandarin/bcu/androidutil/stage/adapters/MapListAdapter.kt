package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import common.util.stage.StageMap

class MapListAdapter(private val activity: Activity, private val maps: ArrayList<Identifier<StageMap>>) : ArrayAdapter<Identifier<StageMap>>(activity, R.layout.map_list_layout, maps.toTypedArray()) {

    private class ViewHolder constructor(row: View) {
        var name: TextView = row.findViewById(R.id.map_list_name)
        var count: TextView = row.findViewById(R.id.map_list_coutns)
        var star: ImageView = row.findViewById(R.id.map_list_star)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.map_list_layout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val stm = Identifier.get(maps[position]) ?: return row

        holder.name.text = withID(maps[position])

        val numbers: String = if (stm.list.size() == 1)
                    stm.list.size().toString() + activity.getString(R.string.map_list_stage)
                else
                    stm.list.size().toString() + activity.getString(R.string.map_list_stages)

        holder.count.text = numbers

        if(!StaticStore.BCMapCode.contains(stm.id.pack))
            holder.star.visibility = View.GONE
        else
            generateStar(holder.star, stm)

        return row
    }

    private fun withID(name: Identifier<StageMap>): String {
        val stm = Identifier.get(name) ?: return Data.trio(name.id)

        val n = MultiLangCont.get(stm) ?: stm.name ?: ""

        return if (n == "") {
            Data.trio(name.id)
        } else {
            Data.trio(name.id) + " - " + n
        }
    }

    private fun generateStar(star: ImageView, stm: StageMap) {
        if(StaticStore.starDifficulty == null) {
            star.visibility = View.GONE
            return
        }

        val h = StaticStore.dptopx(16f, activity)

        val w = (StaticStore.getScreenWidth(activity, false) - StaticStore.dptopx(16f, activity) * 2).coerceAtMost(h * 12)

        val wh = (w / 12.0).toFloat()
        val sta = stm.starMask

        val starMap = Bitmap.createBitmap(w, wh.toInt(), Bitmap.Config.ARGB_8888)

        val cv = Canvas(starMap)
        val p = Paint()

        for(i in 0 until 12) {
            if(sta shr i and 1 != 0) {
                cv.drawBitmap(StaticStore.getResizebp(StaticStore.starDifficulty!![1], wh, wh), wh * i, 0f, p)
            } else {
                cv.drawBitmap(StaticStore.getResizebp(StaticStore.starDifficulty!![0], wh, wh), wh * i, 0f, p)
            }
        }

        star.setImageBitmap(starMap)
    }
}