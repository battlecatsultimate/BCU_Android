package com.mandarin.bcu.androidutil.supports.adapter

import android.app.Activity
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.DataResetHandler

class DataResetAdapter(private val activity: Activity, private val data: ArrayList<DataResetHandler>) : ArrayAdapter<DataResetHandler>(activity, R.layout.data_reset_checkbox, data) {
    private class ViewHolder(view: View) {
        val text = view.findViewById<TextView>(R.id.dataresettext)!!
        val check = view.findViewById<CheckBox>(R.id.dataresetcheck)!!
    }

    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled))
    private val color = intArrayOf(StaticStore.getAttributeColor(activity, R.attr.HintPrimary), StaticStore.getAttributeColor(activity, R.attr.HintPrimary))
    private val warnColor = intArrayOf(StaticStore.getAttributeColor(activity, R.attr.ErrorPrimary), StaticStore.getAttributeColor(activity, R.attr.HintPrimary))

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)

            row = inf.inflate(R.layout.data_reset_checkbox, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = convertView
            holder = row.tag as ViewHolder
        }

        holder.text.text = data[position].text
        holder.check.isChecked = data[position].doPerform

        holder.check.setOnCheckedChangeListener { _, isChecked ->
            data[position].doPerform = isChecked

            val reset = activity.findViewById<MaterialButton>(R.id.dataresetbutton)

            reset.isEnabled = canReset()

            if(canReset()) {
                reset.setTextColor(StaticStore.getAttributeColor(context, R.attr.ErrorPrimary))
                reset.strokeColor = ColorStateList(states, warnColor)
            } else {
                reset.setTextColor(StaticStore.getAttributeColor(context, R.attr.HintPrimary))
                reset.strokeColor = ColorStateList(states, color)
            }
        }

        return row
    }

    private fun canReset() : Boolean {
        for(handler in data) {
            if(handler.doPerform)
                return true
        }

        return false
    }
}