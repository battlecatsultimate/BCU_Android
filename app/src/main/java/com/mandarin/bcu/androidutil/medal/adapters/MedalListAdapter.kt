package com.mandarin.bcu.androidutil.medal.adapters

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import kotlin.collections.ArrayList

class MedalListAdapter(private val activity: Activity, private val num: Int, width: Int, private val imgwh: Float, lines: Array<String?>, private val order: ArrayList<Int>) : ArrayAdapter<String?>(activity, R.layout.medal_layout, lines) {
    private val height: Int = width / num - StaticStore.dptopx(4f, activity)

    private class ViewHolder constructor(view: View) {
        var layout: LinearLayout = view.findViewById(R.id.medallinear)
        var icons: MutableList<ImageView> = ArrayList()

    }

    override fun getView(position: Int, view: View?, group: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.medal_layout, group, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }


        val posit = num * position

        holder.layout.removeAllViews()
        holder.icons.clear()

        for (j in 0 until num) {
            if (posit + j < StaticStore.medals.size) {
                val b = StaticStore.getResizeb(StaticStore.medals[posit + j], activity, imgwh)
                val icon = ImageButton(activity)
                icon.background = ContextCompat.getDrawable(activity, R.drawable.image_button_circular)
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
                params.height = height
                params.marginStart = StaticStore.dptopx(2f, activity)
                params.marginEnd = StaticStore.dptopx(2f, activity)
                icon.layoutParams = params
                icon.setImageBitmap(b)
                icon.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val dialog = Dialog(activity)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.layout_medal_desc)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                ?: return
                        val v1 = dialog.window?.decorView ?: return
                        v1.background = ContextCompat.getDrawable(activity, R.drawable.dialog_box)
                        val icon1 = dialog.findViewById<ImageView>(R.id.medalimg)
                        val name = dialog.findViewById<TextView>(R.id.medalname)
                        val desc = dialog.findViewById<TextView>(R.id.medaldesc)
                        icon1.setImageBitmap(StaticStore.getResizeb(StaticStore.medals[posit + j], activity, imgwh))
                        name.text = StaticStore.MEDNAME.getCont(order[posit + j])
                        desc.text = StaticStore.MEDEXP.getCont(order[posit + j])
                        val lp = WindowManager.LayoutParams()
                        lp.copyFrom(dialog.window?.attributes)
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                        dialog.window?.attributes = lp
                        dialog.show()
                    }
                })
                holder.layout.addView(icon)
                holder.icons.add(icon)
            } else {
                val b = StaticStore.empty(activity, imgwh, imgwh)
                val icon = ImageView(activity)
                icon.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
                icon.setImageBitmap(b)
                holder.layout.addView(icon)
                holder.icons.add(icon)
            }
        }
        return row
    }

}