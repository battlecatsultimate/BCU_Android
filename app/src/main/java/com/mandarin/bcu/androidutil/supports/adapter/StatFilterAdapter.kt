package com.mandarin.bcu.androidutil.supports.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StatFilterElement

/**
 *
 * @param context Context of activity
 * @param unit True for unit, false for enemy
 */
class StatFilterAdapter(private val context: Context, private val unit: Boolean) : RecyclerView.Adapter<StatFilterAdapter.ViewHolder>() {
    companion object {
        val unitType = intArrayOf(R.string.stat_sch_hp, R.string.stat_sch_atk, R.string.stat_sch_hb, R.string.unit_info_rang, R.string.unit_info_cost, R.string.unit_info_spd, R.string.stat_sch_cd, R.string.enem_info_barrier, R.string.unit_info_pre, R.string.stat_sch_atkcount)
        val unitData = intArrayOf(StatFilterElement.HP,StatFilterElement.ATK, StatFilterElement.HB,StatFilterElement.RANGE,StatFilterElement.COSTDROP,StatFilterElement.SPEED,
                StatFilterElement.CD, StatFilterElement.BARRIER, StatFilterElement.PREATK,StatFilterElement.ATKCOUNT)
        val enemyType = intArrayOf(R.string.stat_sch_hp, R.string.stat_sch_atk, R.string.stat_sch_hb, R.string.unit_info_rang, R.string.enem_info_drop, R.string.unit_info_spd, R.string.unit_info_cd, R.string.enem_info_barrier, R.string.unit_info_pre, R.string.stat_sch_atkcount)
        val enemyData = intArrayOf(StatFilterElement.HP,StatFilterElement.ATK, StatFilterElement.HB,StatFilterElement.RANGE,StatFilterElement.COSTDROP,StatFilterElement.SPEED,
                StatFilterElement.CD, StatFilterElement.BARRIER, StatFilterElement.PREATK, StatFilterElement.ATKCOUNT)
    }

    class ViewHolder constructor(row: View) : RecyclerView.ViewHolder(row) {
        var delete: CheckBox = row.findViewById(R.id.statschdelete)
        var layout: TextInputLayout = row.findViewById(R.id.statschmulti)
        var edit: TextInputEditText = row.findViewById(R.id.statschmultiedit)
    }

    private val time: Int = context.resources.getInteger(android.R.integer.config_shortAnimTime)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inf = LayoutInflater.from(context)
        val row = inf.inflate(R.layout.stat_sch_list_layout, parent, false)

        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return StatFilterElement.statFilter.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = StatFilterElement.statFilter[position]

        holder.layout.prefixText = when (stat.option) {
            StatFilterElement.OPTION_LESS -> "< "
            StatFilterElement.OPTION_EQUAL -> "= "
            StatFilterElement.OPTION_GREAT -> "> "
            else -> ""
        }

        holder.layout.suffixText = if(unit) {
            (stat.lev).toString() + " lv."
        } else {
            (stat.lev).toString() + "%"
        }

        holder.layout.hint = if(unit) {
            context.getString(unitType[unitData.indexOf(stat.type)])
        } else {
            context.getString(enemyType[enemyData.indexOf(stat.type)])
        }

        holder.edit.setText(stat.data.toString())

        holder.edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val res = s.toString()

                if(res.isEmpty()) {
                    stat.data = 0
                } else {
                    stat.data = try {
                        res.toInt()
                    } catch (e: NumberFormatException) {
                        holder.edit.setText(Int.MAX_VALUE.toString())
                        holder.edit.setSelection(holder.edit.text.toString().length)

                        Int.MAX_VALUE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        holder.delete.setOnCheckedChangeListener { _, isChecked ->
            stat.delete = isChecked
        }

        holder.delete.isChecked = stat.delete

        fade(holder.delete)
    }

    private fun fade(v: View) {
        if(!StatFilterElement.started) {
            if(StatFilterElement.show) {
                v.visibility = View.VISIBLE
            } else {
                v.visibility = View.GONE
            }

            return
        }

        if(v.visibility == View.GONE && StatFilterElement.show) {
            v.visibility = View.VISIBLE

            v.animate().alpha(1f).setDuration(time.toLong()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    v.visibility = View.VISIBLE
                }
            })
        } else if(v.visibility == View.VISIBLE && !StatFilterElement.show) {
            v.animate()
                    .alpha(0f)
                    .setDuration(time.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            v.visibility = View.GONE
                        }
                    })
        }
    }
}