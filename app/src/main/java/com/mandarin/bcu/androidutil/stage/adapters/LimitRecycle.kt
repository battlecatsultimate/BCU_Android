package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import common.util.stage.Limit

class LimitRecycle(private val activity: Activity, l: Limit?) : RecyclerView.Adapter<LimitRecycle.ViewHolder>() {
    private val limits: Array<String>

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var limit: TextView = itemView.findViewById(R.id.limitst)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stg_limit_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.limit.text = limits[viewHolder.adapterPosition]
    }

    override fun getItemCount(): Int {
        return limits.size
    }

    init {
        val s = GetStrings(activity)
        limits = s.getLimit(l)
    }
}