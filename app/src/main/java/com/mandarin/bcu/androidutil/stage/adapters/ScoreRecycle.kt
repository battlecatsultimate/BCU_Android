package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R
import common.util.lang.MultiLangCont
import common.util.stage.Stage

class ScoreRecycle internal constructor(private val st: Stage, private val activity: Activity) : RecyclerView.Adapter<ScoreRecycle.ViewHolder>() {

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var score: TextView = row.findViewById(R.id.dropchance)
        var item: TextView = row.findViewById(R.id.dropitem)
        var amount: TextView = row.findViewById(R.id.dropamount)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.drop_info_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val data = st.info.time[i]
        viewHolder.score.text = data[0].toString()
        var reward = MultiLangCont.getStatic().RWNAME.getCont(data[1])
        if (reward == null) reward = data[1].toString()
        viewHolder.item.text = reward
        viewHolder.amount.text = data[2].toString()
    }

    override fun getItemCount(): Int {
        return st.info.time.size
    }

}