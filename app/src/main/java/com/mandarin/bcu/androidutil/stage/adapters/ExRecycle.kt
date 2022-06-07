package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R
import com.mandarin.bcu.StageInfo
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.io.json.JsonEncoder
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.MapColc.DefMapColc
import common.util.stage.Stage
import common.util.stage.info.DefStageInfo
import java.text.DecimalFormat

class ExRecycle(private val st: Stage, private val activity: Activity) : RecyclerView.Adapter<ExRecycle.ViewHolder>() {
    private val chanceData: List<String>
    private val stageData: List<Stage>

    init {
        chanceData = handleChance()
        stageData = handleStage()
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val chance: TextView = row.findViewById(R.id.exchance)
        val mapst = row.findViewById<TextView>(R.id.exmapst)
        val info = row.findViewById<ImageButton>(R.id.exinfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.ex_info_layout, parent, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.chance.text = chanceData[holder.adapterPosition]
        holder.mapst.text = getMapStageName(stageData[holder.adapterPosition])
        holder.info.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(activity, StageInfo::class.java)

                intent.putExtra("Data", JsonEncoder.encode(stageData[holder.adapterPosition].id).toString())
                intent.putExtra("custom", false)

                activity.startActivity(intent)
            }
        })
    }

    override fun getItemCount(): Int {
        return stageData.size
    }

    private fun handleChance() : List<String> {
        val res = ArrayList<String>()

        if(st.info.exConnection()) {
            val info = st.info as DefStageInfo
            val min = info.exStageIDMin
            val max = info.exStageIDMax

            val n = max - min + 1

            val df = DecimalFormat("#.##")

            for(i in 0 until n) {
                res.add(df.format(info.exChance * 1.0 / n)+"%")
            }
        } else {
            val df = DecimalFormat("#.##")

            for(c in st.info.exChances) {
                res.add(df.format(c) + "%")
            }
        }

        return res
    }

    private fun handleStage() : List<Stage> {
        val res = ArrayList<Stage>()

        if(st.info.exConnection()) {
            val info = st.info as DefStageInfo
            val min = info.exStageIDMin
            val max = info.exStageIDMax

            val map = DefMapColc.getMap(4000 + info.exMapID) ?: return res

            for(i in min..max) {
                val st = map.list.list[i] ?: return res

                res.add(st)
            }
        } else {
            res.addAll(st.info.exStages)
        }

        return res
    }

    private fun getMapStageName(st: Stage) : String {
        var mapName = MultiLangCont.get(st.cont)

        if(mapName == null || mapName.isBlank()) {
            mapName = st.cont.names.toString()

            if(mapName.isBlank())
                mapName = Data.hex(400000 + st.cont.id.id)
        }

        var stageName = MultiLangCont.get(st)

        if(stageName == null || stageName.isBlank()) {
            stageName = st.names.toString()

            if(stageName.isBlank())
                stageName = Data.trio(st.id.id)
        }

        return "$mapName - $stageName"
    }
}