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
import java.text.DecimalFormat

class DropRecycle(private val st: Stage, private val activity: Activity) : RecyclerView.Adapter<DropRecycle.ViewHolder>() {
    private val dropData: List<String>

    init {
        dropData = handleDrops()
    }

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
        val c = when {
            dropData.isEmpty() -> {
                (i+1).toString()
            }
            i >= dropData.size -> {
                st.info.drop[i][0].toString() + "%"
            }
            else -> {
                dropData[i] + "%"
            }
        }

        val data = st.info.drop[i]
        viewHolder.chance.text = c
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

    private fun handleDrops() : List<String> {
        println(st.info.rand)

        val res = ArrayList<String>()

        val data = st.info.drop

        var sum = 0

        for(i in data) {
            sum += i[0]
        }

        val df = DecimalFormat("#.##")

        if(sum == 1000) {
            for(i in data)
                res.add(df.format(i[0].toDouble()/10))
        } else if((sum == data.size && sum != 1) || st.info.rand == -3) {
            return res
        } else if(sum == 100) {
            for(i in data)
                res.add(i[0].toString())
        } else if(sum > 100 && st.info.rand == 0) {
            var rest = 100.0

            for(i in data) {
                val filter = rest * i[0].toDouble() / 100.0
                rest -= filter

                res.add(df.format(filter))
            }
        } else {
            for(i in data)
                res.add(i[0].toString())
        }

        return res
    }

}