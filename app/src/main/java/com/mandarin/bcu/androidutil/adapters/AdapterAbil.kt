package com.mandarin.bcu.androidutil.adapters

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore

class AdapterAbil(private val ability: List<String>, private val procs: List<String>, private val abilicon: List<Int>, private val procicon: List<Int>, private val context: Context) : RecyclerView.Adapter<AdapterAbil.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(context).inflate(R.layout.ability_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if (viewHolder.adapterPosition < ability.size) {
            viewHolder.abiltext.text = ability[viewHolder.adapterPosition]
            if (abilicon[viewHolder.adapterPosition] != 15 && abilicon[viewHolder.adapterPosition] != 19) {
                val resized: Bitmap = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    StaticStore.getResizeb(StaticStore.icons[abilicon[viewHolder.adapterPosition]], context, 28f)
                } else {
                    StaticStore.getResizeb(StaticStore.icons[abilicon[viewHolder.adapterPosition]], context, 24f)
                }
                viewHolder.abilicon.setImageBitmap(resized)
            } else {
                viewHolder.abilicon.setImageBitmap(empty())
            }
        } else {
            val location = viewHolder.adapterPosition - ability.size
            viewHolder.abiltext.text = procs[location]
            val resized: Bitmap
            resized = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                StaticStore.getResizeb(StaticStore.picons[procicon[location]], context, 28f)
            } else {
                StaticStore.getResizeb(StaticStore.picons[procicon[location]], context, 24f)
            }
            viewHolder.abilicon.setImageBitmap(resized)
        }
    }

    override fun getItemCount(): Int {
        return ability.size + procs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var abilicon: ImageView = itemView.findViewById(R.id.abilicon)
        var abiltext: TextView = itemView.findViewById(R.id.ability)

    }

    private fun empty(): Bitmap {
        var dp = 28f
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) dp = 24f
        val r = context.resources
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
        val conf = Bitmap.Config.ARGB_8888
        return Bitmap.createBitmap(px.toInt(), px.toInt(), conf)
    }

}