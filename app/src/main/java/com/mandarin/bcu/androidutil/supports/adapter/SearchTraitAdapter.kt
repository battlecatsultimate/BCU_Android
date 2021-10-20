package com.mandarin.bcu.androidutil.supports.adapter

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.CommonStatic
import common.pack.Identifier
import common.util.unit.Trait
import kotlin.math.ceil

class SearchTraitAdapter(private val context: Context, private val tool: Array<String>, private val colors: Array<Identifier<Trait>>) : RecyclerView.Adapter<SearchTraitAdapter.ViewHolder>() {
    companion object {
        private val BCTrait = intArrayOf(219, 220, 221, 222, 223, 224, 225, 294, 226, 227)
    }

    private val up = ArrayList<Int>()

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val abil = if(isLandscape()) {
            arrayOf<CheckBox>(row.findViewById(R.id.abilicon1), row.findViewById(R.id.abilicon2), row.findViewById(
                R.id.abilicon3), row.findViewById(R.id.abilicon4), row.findViewById(R.id.abilicon5))
        } else {
            arrayOf(row.findViewById(R.id.abilicon1), row.findViewById(R.id.abilicon2), row.findViewById(
                R.id.abilicon3))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTraitAdapter.ViewHolder {
        val row = LayoutInflater.from(context).inflate(R.layout.search_filter_ability_layout, parent, false)

        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return if(isLandscape()) {
            ceil(colors.size/5.0).toInt()
        } else {
            ceil(colors.size/3.0).toInt()
        }
    }

    override fun onBindViewHolder(holder: SearchTraitAdapter.ViewHolder, position: Int) {
        val realPosit = if(isLandscape()) {
            5 * position
        } else {
            3 * position
        }

        val endReal = if(isLandscape()) {
            realPosit + 4
        } else {
            realPosit + 2
        }

        for(i in realPosit..endReal) {
            val ch = holder.abil[i-realPosit]

            if(i >= colors.size) {
                ch.visibility = View.INVISIBLE

                if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ch.compoundDrawablePadding = StaticStore.dptopx(16f, context)
                } else {
                    ch.compoundDrawablePadding = StaticStore.dptopx(8f, context)
                }

                continue
            }

            ch.isChecked = StaticStore.tg.contains(colors[i])

            val trait = Identifier.get(colors[i]) ?: continue

            val icon = getIcon(trait)

            if(icon == null) {
                ch.text = tool[i]
                ch.setTextColor(StaticStore.getAttributeColor(context, R.attr.TextPrimary))
            } else {
                ch.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)

                ch.setOnLongClickListener {
                    StaticStore.showShortMessage(context, tool[i])

                    true
                }

                if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ch.compoundDrawablePadding = StaticStore.dptopx(16f, context)
                } else {
                    ch.compoundDrawablePadding = StaticStore.dptopx(8f, context)
                }
            }

            ch.setOnCheckedChangeListener { _, check ->
                if(check)
                    StaticStore.tg.add(trait.id)
                else
                    StaticStore.tg.remove(trait.id)
            }
        }
    }

    fun updateList() {
        if(up.isEmpty()) {
            up.add(0)
        } else {
            up.clear()
        }
    }

    private fun getIcon(trait: Trait): Drawable? {
        return if(trait.id.pack == Identifier.DEF) {
            if(trait.id.id < BCTrait.size)
                getResizeDraw(BCTrait[trait.id.id], if(isLandscape()) 40f else 32f)
            else
                null
        } else {
            if(trait.icon != null && trait.icon.img != null)
                getDrawable(trait.icon.img.bimg() as Bitmap)
            else
                getDrawable(CommonStatic.getBCAssets().dummyTrait.img.bimg() as Bitmap)
        }
    }

    private fun getDrawable(b: Bitmap) : Drawable {
        return if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BitmapDrawable(context.resources, StaticStore.getResizeb(b, context, 40f))
        } else {
            BitmapDrawable(context.resources, StaticStore.getResizeb(b, context, 32f))
        }
    }

    private fun isLandscape() : Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun getResizeDraw(id: Int, dp: Float) : Drawable {
        val icon = StaticStore.img15?.get(id)?.bimg() ?: StaticStore.empty(context, dp, dp)
        val bd = BitmapDrawable(context.resources, StaticStore.getResizeb(icon as Bitmap, context, dp))

        bd.isFilterBitmap = true
        bd.setAntiAlias(true)

        return bd
    }
}