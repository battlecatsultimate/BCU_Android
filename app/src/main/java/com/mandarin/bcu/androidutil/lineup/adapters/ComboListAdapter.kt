package com.mandarin.bcu.androidutil.lineup.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.battle.BasisSet
import common.pack.UserProfile
import common.util.lang.MultiLangCont
import common.util.unit.Combo
import java.text.DecimalFormat
import java.util.*

class ComboListAdapter internal constructor(activity: Activity, names: Array<String?>) : ArrayAdapter<String?>(activity, R.layout.combo_list_layout, names) {
    private class ViewHolder constructor(view: View) {
        var comboname: TextView = view.findViewById(R.id.comboname)
        var combodesc: TextView = view.findViewById(R.id.combodesc)
        var comboocc: TextView = view.findViewById(R.id.comboocc)
        var comimglayout: LinearLayout = view.findViewById(R.id.iconlayout)
        var icons: MutableList<ImageView> = ArrayList()

    }

    private val comnames = intArrayOf(R.string.combo_atk, R.string.combo_hp, R.string.combo_spd, R.string.combo_caninch, R.string.combo_work, R.string.combo_initmon, R.string.combo_canatk, R.string.combo_canchtime, 0, R.string.combo_wal, R.string.combo_bsh, R.string.combo_cd, R.string.combo_ac, R.string.combo_xp, R.string.combo_strag, R.string.combo_md, R.string.combo_res, R.string.combo_kbdis, R.string.combo_sl, R.string.combo_st, R.string.combo_wea, R.string.combo_inc, R.string.combo_wit, R.string.combo_eva, R.string.combo_crit)
    override fun getView(position: Int, view: View?, group: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.combo_list_layout,group,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        try {
            holder.comboname.text = MultiLangCont.getStatic().COMNAME.getCont(StaticStore.combos[position].name)
            val occ = context.getString(R.string.combo_occu) + " : " + BasisSet.current().sele.lu.occupance(StaticStore.combos[position])
            holder.comboocc.text = occ
            holder.combodesc.text = getDescription(StaticStore.combos[position])
            holder.comimglayout.removeAllViews()
            holder.icons.clear()
            for (i in 0..4) {
                if (StaticStore.combos[position].units.size <= i) {
                    val icon = ImageView(context)
                    icon.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
                    icon.setImageBitmap(StaticStore.empty(context, 24f, 24f))
                    icon.background = ContextCompat.getDrawable(context, R.drawable.cell_shape)
                    holder.comimglayout.addView(icon)
                    holder.icons.add(icon)
                } else {
                    val icon = ImageView(context)
                    val u = UserProfile.getBCData().units[StaticStore.combos[position].units[i][0]] ?: return row
                    icon.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
                    icon.setImageBitmap(u.forms[StaticStore.combos[position].units[i][1]].anim.uni.img.bimg() as Bitmap)
                    icon.background = ContextCompat.getDrawable(context, R.drawable.cell_shape)
                    holder.comimglayout.addView(icon)
                    holder.icons.add(icon)
                }
            }
        } catch (e: NullPointerException) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, context)

            return row
        } catch (e: IndexOutOfBoundsException) {
            return row
        }
        return row
    }

    private fun getDescription(c: Combo): String {
        val type = c.type
        var multi = ""
        when (type) {
            0, 2 -> multi = " ( +" + (10 + 5 * c.lv) + "% )"
            1, 20, 19, 18, 17, 16, 15, 14, 13, 12, 9 -> multi = " ( +" + (10 + 10 * c.lv) + "% )"
            3 -> multi = " ( +" + (20 + 20 * c.lv) + "% )"
            4 -> multi = " ( + Lv. " + (1 + c.lv) + " )"
            5 -> multi = if (c.lv == 0) " ( +" + 300 + " )" else if (c.lv == 1) " ( +" + 500 + " )" else " ( +" + 1000 + " )"
            6, 10 -> multi = " ( +" + (20 + 30 * c.lv) + "% )"
            7 -> multi = " ( -" + (150 + 150 * c.lv) + "f / -" + (5 + 5 * c.lv) + "s )"
            11 -> multi = " ( -" + (26 + 26 * c.lv) + "f / -" + DecimalFormat("#.##").format((26 + 26 * c.lv).toDouble() / 30) + "s )"
            21 -> multi = " ( +" + (20 + 10 * c.lv) + "% )"
            22, 23 -> multi = " ( +" + (100 + 100 * c.lv) + "% )"
            24 -> multi = " ( +" + (1 + c.lv) + "% )"
        }
        return context.getString(comnames[c.type]) + " Lv. " + c.lv + multi
    }
}