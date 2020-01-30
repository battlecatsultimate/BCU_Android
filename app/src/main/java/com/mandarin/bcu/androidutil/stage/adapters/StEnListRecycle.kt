package com.mandarin.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.EnemyInfo
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import common.util.stage.SCDef
import common.util.stage.Stage

class StEnListRecycle(private val activity: Activity, private val st: Stage, private var multi: Int, private var frse: Boolean) : RecyclerView.Adapter<StEnListRecycle.ViewHolder>() {

    init {
        if (StaticStore.infoOpened == null) {
            StaticStore.infoOpened = BooleanArray(st.data.datas.size)
            for (i in st.data.datas.indices) {
                StaticStore.infoOpened[i] = false
            }
        } else if (StaticStore.infoOpened.size < st.data.datas.size) {
            StaticStore.infoOpened = BooleanArray(st.data.datas.size)
            for (i in st.data.datas.indices) {
                StaticStore.infoOpened[i] = false
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stage_enemy_list_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val s = GetStrings(activity)
        val data = reverse(st.data.datas)
        viewHolder.expand.setOnClickListener(View.OnClickListener {
            if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL) return@OnClickListener
            StaticStore.infoClick = SystemClock.elapsedRealtime()
            if (viewHolder.moreinfo.height == 0) {
                viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = viewHolder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(0, height)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = viewHolder.moreinfo.layoutParams
                    layout.height = `val`
                    viewHolder.moreinfo.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                viewHolder.expand.setImageDrawable(activity.getDrawable(R.drawable.ic_expand_more_black_24dp))
                StaticStore.infoOpened[viewHolder.adapterPosition] = true
            } else {
                viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = viewHolder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(height, 0)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = viewHolder.moreinfo.layoutParams
                    layout.height = `val`
                    viewHolder.moreinfo.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                viewHolder.expand.setImageDrawable(activity.getDrawable(R.drawable.ic_expand_less_black_24dp))
                StaticStore.infoOpened[viewHolder.adapterPosition] = false
            }
        })
        if (StaticStore.infoOpened[viewHolder.adapterPosition]) {
            viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val layout = viewHolder.moreinfo.layoutParams
            layout.height = viewHolder.moreinfo.measuredHeight
            viewHolder.moreinfo.layoutParams = layout
            viewHolder.expand.setImageDrawable(activity.getDrawable(R.drawable.ic_expand_more_black_24dp))
        }
        viewHolder.icon.setImageBitmap(StaticStore.getResizeb(StaticStore.enemies[data[viewHolder.adapterPosition]?.get(SCDef.E) ?: 0].anim.edi.img.bimg() as Bitmap,activity, 85f, 32f))
        viewHolder.number.text = s.getNumber(data[viewHolder.adapterPosition]!!)
        viewHolder.info.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val en = StaticStore.enemies[data[viewHolder.adapterPosition]!![SCDef.E]]
                val intent = Intent(activity, EnemyInfo::class.java)
                intent.putExtra("ID", en.id)
                intent.putExtra("Multiply", (data[viewHolder.adapterPosition]!![SCDef.M].toFloat() * multi.toFloat() / 100.toFloat()).toInt())
                activity.startActivity(intent)
            }
        })
        viewHolder.multiply.text = s.getMultiply(data[viewHolder.adapterPosition]!!, multi)
        viewHolder.bh.text = s.getBaseHealth(data[viewHolder.adapterPosition]!!)
        if (data[viewHolder.adapterPosition]!![SCDef.B] == 0) viewHolder.isboss.text = activity.getString(R.string.unit_info_false) else viewHolder.isboss.text = activity.getString(R.string.unit_info_true)
        viewHolder.layer.text = s.getLayer(data[viewHolder.adapterPosition]!!)
        viewHolder.startb.setOnClickListener { if (viewHolder.start.text.toString().endsWith("f")) viewHolder.start.text = s.getStart(data[viewHolder.adapterPosition]!!, false) else viewHolder.start.text = s.getStart(data[viewHolder.adapterPosition]!!, true) }
        viewHolder.start.text = s.getStart(data[viewHolder.adapterPosition]!!, frse)
        viewHolder.respawnb.setOnClickListener { if (viewHolder.respawn.text.toString().endsWith("f")) viewHolder.respawn.text = s.getRespawn(data[viewHolder.adapterPosition]!!, false) else viewHolder.respawn.text = s.getRespawn(data[viewHolder.adapterPosition]!!, true) }
        viewHolder.respawn.text = s.getRespawn(data[viewHolder.adapterPosition]!!, frse)
    }

    override fun getItemCount(): Int {
        return st.data.datas.size
    }

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var expand: ImageButton = row.findViewById(R.id.stgenlistexp)
        var icon: ImageView = row.findViewById(R.id.stgenlisticon)
        var multiply: TextView = row.findViewById(R.id.stgenlistmultir)
        var number: TextView = row.findViewById(R.id.stgenlistnumr)
        var info: ImageButton = row.findViewById(R.id.stgenlistinfo)
        var bh: TextView = row.findViewById(R.id.enemlistbhr)
        var isboss: TextView = row.findViewById(R.id.enemlistibr)
        var layer: TextView = row.findViewById(R.id.enemlistlayr)
        var startb: Button = row.findViewById(R.id.enemlistst)
        var start: TextView = row.findViewById(R.id.enemliststr)
        var respawnb: Button = row.findViewById(R.id.enemlistres)
        var respawn: TextView = row.findViewById(R.id.enemlistresr)
        var moreinfo: TableLayout = row.findViewById(R.id.stgenlistmi)

    }

    private fun reverse(data: Array<IntArray>): Array<IntArray?> {
        val result = arrayOfNulls<IntArray>(data.size)
        for (i in data.indices) {
            result[i] = data[data.size - 1 - i]
        }
        return result
    }
}