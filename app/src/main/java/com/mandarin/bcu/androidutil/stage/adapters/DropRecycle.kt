package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.system.MultiLangCont
import common.util.stage.Stage

class DropRecycle(private val st: Stage, private val activity: Activity) : RecyclerView.Adapter<DropRecycle.ViewHolder>() {

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var chance: TextView = row.findViewById(R.id.dropchance)
        var item: TextView = row.findViewById(R.id.dropitem)
        var amount: TextView = row.findViewById(R.id.dropamount)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.drop_info_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val data = st.info.drop[i]
        val chance = data[0].toString() + " %"
        viewHolder.chance.text = chance
        var reward = MultiLangCont.RWNAME.getCont(data[1])
        if (reward == null) reward = data[1].toString()
        if (i == 0) {
            if (data[0] != 100) {
                val bd = BitmapDrawable(activity.resources, StaticStore.getResizeb(StaticStore.treasure, activity, 24f))
                bd.isFilterBitmap = true
                bd.setAntiAlias(true)
                viewHolder.item.setCompoundDrawablesWithIntrinsicBounds(null, null, bd, null)
            }
            if (st.info.rand == 1 || data[1] >= 1000) {
                reward += activity.getString(R.string.stg_info_once)
                viewHolder.item.text = reward
            } else {
                viewHolder.item.text = reward
            }
        } else {
            viewHolder.item.text = reward
        }
        viewHolder.amount.text = data[2].toString()
    }

    override fun getItemCount(): Int {
        return st.info.drop.size
    }

}