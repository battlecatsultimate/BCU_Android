package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R

class MiscRecycle(private val activity: Activity, private val miscs : List<String>) : RecyclerView.Adapter<MiscRecycle.ViewHolder>() {
    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val misc: TextView = itemView.findViewById(R.id.limitst)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stg_limit_layout, parent, false)

        return ViewHolder(row)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.misc.text = miscs[holder.adapterPosition]
    }

    override fun getItemCount(): Int {
        return miscs.size
    }


}