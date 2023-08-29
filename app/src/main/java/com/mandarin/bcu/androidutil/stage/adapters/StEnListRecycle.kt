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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.EnemyInfo
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.unit.Enemy

class StEnListRecycle(private val activity: Activity, private val st: Stage, private var multi: Int, private var frse: Boolean) : RecyclerView.Adapter<StEnListRecycle.ViewHolder>() {

    init {
        if (StaticStore.infoOpened == null) {
            StaticStore.infoOpened = BooleanArray(st.data.datas.size) {
                false
            }
        } else if ((StaticStore.infoOpened?.size ?: 0) < st.data.datas.size) {
            StaticStore.infoOpened = BooleanArray(st.data.datas.size) {
                false
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

        val infos = StaticStore.infoOpened ?: return

        viewHolder.expand.setOnClickListener(View.OnClickListener {
            if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                return@OnClickListener

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

                viewHolder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))

                infos[viewHolder.bindingAdapterPosition] = true
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
                viewHolder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_less_black_24dp))
                infos[viewHolder.bindingAdapterPosition] = false
            }
        })

        if (infos[viewHolder.bindingAdapterPosition]) {
            viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val layout = viewHolder.moreinfo.layoutParams
            layout.height = viewHolder.moreinfo.measuredHeight
            viewHolder.moreinfo.layoutParams = layout
            viewHolder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))
        }

        val id = data[viewHolder.bindingAdapterPosition]?.enemy ?: UserProfile.getBCData().enemies[0].id

        val em = Identifier.get(id) ?: return

        if(em !is Enemy)
            return

        val icon = em.anim?.edi?.img?.bimg()

        if(icon == null) {
            viewHolder.icon.setImageBitmap(StaticStore.empty(activity, 85f, 32f))
        } else {
            viewHolder.icon.setImageBitmap(StaticStore.getResizeb(icon as Bitmap,activity, 85f, 32f))
        }

        viewHolder.number.text = s.getNumber(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line())


        viewHolder.info.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(activity, EnemyInfo::class.java)
                intent.putExtra("Data", JsonEncoder.encode(em.id).toString())
                intent.putExtra("Multiply", ((data[viewHolder.bindingAdapterPosition]?.multiple?.toFloat() ?: 0f) * multi.toFloat() / 100.toFloat()).toInt())
                intent.putExtra("AMultiply", ((data[viewHolder.bindingAdapterPosition]?.mult_atk?.toFloat() ?: 0f) * multi.toFloat() / 100.toFloat()).toInt())
                activity.startActivity(intent)
            }
        })

        viewHolder.multiply.text = s.getMultiply(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), multi)

        viewHolder.bh.text = s.getBaseHealth(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line())

        if ((data[viewHolder.bindingAdapterPosition]?.boss ?: -1) == 0)
            viewHolder.isboss.text = activity.getString(R.string.unit_info_false)
        else
            viewHolder.isboss.text = activity.getString(R.string.unit_info_true)

        viewHolder.layer.text = s.getLayer(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line())

        viewHolder.startb.setOnClickListener {
            if (viewHolder.start.text.toString().endsWith("f"))
                viewHolder.start.text = s.getStart(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), false)
            else
                viewHolder.start.text = s.getStart(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), true)
        }

        viewHolder.start.text = s.getStart(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), frse)

        viewHolder.respawnb.setOnClickListener {
            if (viewHolder.respawn.text.toString().endsWith("f"))
                viewHolder.respawn.text = s.getRespawn(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), false)
            else
                viewHolder.respawn.text = s.getRespawn(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), true)
        }

        viewHolder.respawn.text = s.getRespawn(data[viewHolder.bindingAdapterPosition] ?: SCDef.Line(), frse)

        viewHolder.killcount.text = (data[viewHolder.bindingAdapterPosition] ?: SCDef.Line()).kill_count.toString()
    }

    override fun getItemCount(): Int {
        return st.data.datas.size
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val expand: ImageButton = row.findViewById(R.id.stgenlistexp)
        val icon: ImageView = row.findViewById(R.id.stgenlisticon)
        val multiply: TextView = row.findViewById(R.id.stgenlistmultir)
        val number: TextView = row.findViewById(R.id.stgenlistnumr)
        val info: ImageButton = row.findViewById(R.id.stgenlistinfo)
        val bh: TextView = row.findViewById(R.id.enemlistbhr)
        val isboss: TextView = row.findViewById(R.id.enemlistibr)
        val layer: TextView = row.findViewById(R.id.enemlistlayr)
        val startb: Button = row.findViewById(R.id.enemlistst)
        val start: TextView = row.findViewById(R.id.enemliststr)
        val respawnb: Button = row.findViewById(R.id.enemlistres)
        val respawn: TextView = row.findViewById(R.id.enemlistresr)
        val moreinfo: TableLayout = row.findViewById(R.id.stgenlistmi)
        val killcount: TextView = row.findViewById(R.id.enemlistkilcr)
    }

    private fun reverse(data: Array<SCDef.Line>): Array<SCDef.Line?> {
        val result = arrayOfNulls<SCDef.Line>(data.size)
        for (i in data.indices) {
            result[i] = data[data.size - 1 - i]
        }
        return result
    }
}