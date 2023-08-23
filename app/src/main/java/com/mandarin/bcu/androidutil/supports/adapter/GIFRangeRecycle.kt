package com.mandarin.bcu.androidutil.supports.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.RangeSlider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticJava
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.AnimationCView
import common.CommonStatic
import common.pack.Identifier
import common.util.anim.EAnimD
import common.util.pack.DemonSoul
import common.util.pack.EffAnim
import common.util.pack.NyCastle
import common.util.pack.Soul
import common.util.unit.Enemy
import common.util.unit.Unit

class GIFRangeRecycle(private val name: ArrayList<String>, private val ac: Activity, private val type: AnimationCView.AnimationType, private val content: Any, form: Int) : RecyclerView.Adapter<GIFRangeRecycle.ViewHolder>() {
    private val data = ArrayList<Array<Int>>()
    private val enables = Array(name.size) {
        true
    }

    private val form: Int

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val range: RangeSlider = row.findViewById(R.id.gifrange)
        val switch: SwitchMaterial = row.findViewById(R.id.gifswitch)
    }

    init {
        for(i in name.indices) {
            this.data.add(Array(2) {
                0
            })
        }

        this.form = if (type == AnimationCView.AnimationType.UNIT) {
            form
        } else {
            -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val row = LayoutInflater.from(ac).inflate(R.layout.gif_frame_decision_layout, parent, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.range.valueFrom = 0f

        val anim = getEAnimD(position)

        holder.range.valueTo =  (anim.len() - 1).toFloat()
        holder.range.setLabelFormatter { it.toInt().toString() }
        holder.range.stepSize = if (CommonStatic.getConfig().performanceMode) {
            0.5f
        } else {
            1f
        }

        holder.range.setMinSeparationValue(if (CommonStatic.getConfig().performanceMode) {
            0.5f
        } else {
            1f
        })

        holder.range.values = listOf(holder.range.valueFrom, holder.range.valueTo)

        holder.range.isTickVisible = false

        data[position][0] = if(holder.range.values.size > 0)
            if (CommonStatic.getConfig().performanceMode)
                (holder.range.values[0] * 2f).toInt()
            else
                holder.range.values[0].toInt()
        else
            0

        data[position][1] = if(holder.range.values.size > 1)
            if (CommonStatic.getConfig().performanceMode)
                (holder.range.values[1] * 2f).toInt()
            else
                holder.range.values[1].toInt()
        else
            data[position][0]

        holder.range.addOnChangeListener { slider, _, fromUser ->
            if(fromUser) {
                holder.switch.text = generateRangeName(holder.adapterPosition, holder.range)

                data[holder.adapterPosition][0] = if (CommonStatic.getConfig().performanceMode)
                    (holder.range.values[0] * 2f).toInt()
                else
                    holder.range.values[0].toInt()

                data[holder.adapterPosition][1] = if (CommonStatic.getConfig().performanceMode)
                    (holder.range.values[1] * 2f).toInt()
                else
                    holder.range.values[1].toInt()
            }
        }

        holder.switch.setOnCheckedChangeListener { _, b ->
            holder.range.isEnabled = b
            enables[holder.adapterPosition] = b
        }

        holder.switch.isChecked = enables[position]
        holder.switch.text = generateRangeName(position, holder.range)
    }

    override fun getItemCount(): Int {
        return name.size
    }

    private fun generateRangeName(ind: Int, range: RangeSlider) : String {
        val values = range.values

        return "${name[ind]} : ${values[0].toInt()} ~ ${values[1].toInt()}"
    }

    private fun getEAnimD(ind: Int) : EAnimD<*> {
        when(type) {
            AnimationCView.AnimationType.UNIT -> {
                if(content !is Identifier<*>)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                val u = Identifier.get(content)

                if(u !is Unit)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                return u.forms[form].getEAnim(StaticStore.getAnimType(ind, u.forms[form].anim.anims.size))
            }
            AnimationCView.AnimationType.ENEMY -> {
                if(content !is Identifier<*>)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                val e = Identifier.get(content)

                if(e !is Enemy)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                return e.getEAnim(StaticStore.getAnimType(ind, e.anim.anims.size))
            }
            AnimationCView.AnimationType.EFFECT -> {
                if(content !is EffAnim<*>)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                return StaticJava.generateEAnimD(content, ind)
            }
            AnimationCView.AnimationType.SOUL -> {
                if(content !is Soul)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                return StaticJava.generateEAnimD(content, ind)
            }
            AnimationCView.AnimationType.CANNON -> {
                if(content !is NyCastle)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                return StaticJava.generateEAnimD(content, ind)
            }
            AnimationCView.AnimationType.DEMON_SOUL -> {
                if(content !is DemonSoul)
                    throw IllegalStateException("Invalid content ${content::class.java.name} with type $type")

                return StaticJava.generateEAnimD(content, ind)
            }
            else -> {
                throw IllegalStateException("Invalid type $type")
            }
        }
    }

    fun getData() : ArrayList<Array<Int>> {
        return data
    }

    fun getEnables() : Array<Boolean> {
        return enables
    }
}