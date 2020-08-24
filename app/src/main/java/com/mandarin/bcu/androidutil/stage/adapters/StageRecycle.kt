package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.ImageViewer
import com.mandarin.bcu.MusicPlayer
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import common.battle.BasisSet
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.stage.Limit
import common.util.stage.Stage
import java.text.DecimalFormat
import java.util.*

class StageRecycle(private val activity: Activity, private val data: Identifier<Stage>) : RecyclerView.Adapter<StageRecycle.ViewHolder>() {
    private val s: GetStrings = GetStrings(activity)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.stginfoidr)
        val star: Spinner = itemView.findViewById(R.id.stginfostarr)
        val energy: TextView = itemView.findViewById(R.id.stginfoengr)
        val xp: TextView = itemView.findViewById(R.id.stginfoxpr)
        val health: TextView = itemView.findViewById(R.id.stginfobhr)
        val difficulty: TextView = itemView.findViewById(R.id.stginfodifr)
        val continueable: TextView = itemView.findViewById(R.id.stginfocontinr)
        val length: TextView = itemView.findViewById(R.id.stginfolenr)
        val maxenemy: TextView = itemView.findViewById(R.id.stginfomaxenr)
        val music: Button = itemView.findViewById(R.id.stginfomusicr)
        val castleperc: TextView = itemView.findViewById(R.id.stginfomusic2)
        val music2: Button = itemView.findViewById(R.id.stginfomusic2r)
        val background: Button = itemView.findViewById(R.id.stginfobgr)
        val castle: Button = itemView.findViewById(R.id.stginfoctr)
        val droptitle: TextView = itemView.findViewById(R.id.stginfodrop)
        val drop: RecyclerView = itemView.findViewById(R.id.droprec)
        val droprow: TableRow = itemView.findViewById(R.id.drop)
        val dropscroll: NestedScrollView = itemView.findViewById(R.id.dropscroll)
        val score: RecyclerView = itemView.findViewById(R.id.scorerec)
        val scorerow: TableRow = itemView.findViewById(R.id.score)
        val scorescroll: NestedScrollView = itemView.findViewById(R.id.scorescroll)
        val limitNone: TextView = itemView.findViewById(R.id.stginfononer)
        val limitrec: RecyclerView = itemView.findViewById(R.id.stginfolimitrec)
        val limitscroll: NestedScrollView = itemView.findViewById(R.id.limitscroll)
        val chanceText: TextView = itemView.findViewById(R.id.stfinfochance)
        val loop: TextView = itemView.findViewById(R.id.stginfoloopt)
        val loop1: TextView = itemView.findViewById(R.id.stginfoloop1t)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stage_info_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val t = BasisSet.current().t()

        val st = Identifier.get(data) ?: return
        val stm = st.info.map.sm ?: return

        viewHolder.id.text = s.getID(data.pack, st.info.map.sm.id.id, data.id)
        val stars: MutableList<String> = ArrayList()
        for (k in stm.stars.indices) {
            val s: String = (k + 1).toString() + " (" + stm.stars[k] + " %)"
            stars.add(s)
        }

        if(st.info != null && st.info.drop != null) {
            if(st.info.drop.size >= 2 || st.info.rand == -3) {
                var same = true
                val d = st.info.drop[0][0]

                for(data in st.info.drop) {
                    if(d != data[0])
                        same = false
                }

                if(same) {
                    viewHolder.chanceText.text = activity.getText(R.string.stg_enem_list_num)
                }
            }
        }

        val arrayAdapter = ArrayAdapter(activity, R.layout.spinneradapter, stars)
        viewHolder.star.adapter = arrayAdapter
        viewHolder.star.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val enrec: RecyclerView = activity.findViewById(R.id.stginfoenrec)
                enrec.layoutManager = LinearLayoutManager(activity)
                ViewCompat.setNestedScrollingEnabled(enrec, false)
                val listRecycle = EnemyListRecycle(activity, st, stm.stars[position])
                enrec.adapter = listRecycle
                StaticStore.stageSpinner = position
                val l = st.getLim(position)
                if (none(l)) {
                    viewHolder.limitNone.visibility = View.VISIBLE
                    viewHolder.limitscroll.visibility = View.GONE
                } else {
                    viewHolder.limitscroll.visibility = View.VISIBLE
                    viewHolder.limitNone.visibility = View.GONE
                    if (data.id == l.sid || l.sid == -1) {
                        if (viewHolder.star.selectedItemPosition == l.star || l.star == -1) {
                            viewHolder.limitrec.layoutManager = LinearLayoutManager(activity)
                            ViewCompat.setNestedScrollingEnabled(viewHolder.limitrec, false)
                            val limitRecycle = LimitRecycle(activity, l)
                            viewHolder.limitrec.adapter = limitRecycle
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if (StaticStore.stageSpinner != -1) {
            viewHolder.star.setSelection(StaticStore.stageSpinner)
        }
        if (st.info != null) {
            if (data.pack == "000000" || data.pack == "000013")
                viewHolder.xp.text = s.getXP(st.info.xp, t, true)
            else
                viewHolder.xp.text = s.getXP(st.info.xp, t, false)
        } else {
            viewHolder.xp.text = "0"
        }

        if (st.info != null)
            viewHolder.energy.text = st.info.energy.toString()
        else
            viewHolder.energy.text = "0"

        viewHolder.health.text = st.health.toString()

        if (st.info != null)
            viewHolder.difficulty.text = s.getDifficulty(st.info.diff)
        else
            viewHolder.difficulty.setText(R.string.unit_info_t_none)

        viewHolder.continueable.text = if (st.non_con)
            activity.getString(R.string.stg_info_impo)
        else
            activity.getString(R.string.stg_info_poss)

        viewHolder.length.text = st.len.toString()
        viewHolder.maxenemy.text = st.max.toString()
        viewHolder.music.text = st.mus0.toString()

        viewHolder.music.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(st.mus0 == null)
                    return

                val intent = Intent(activity, MusicPlayer::class.java)
                intent.putExtra("Data", JsonEncoder.encode(st.mus0).toString())

                activity.startActivity(intent)
            }

        })

        viewHolder.castleperc.text = viewHolder.castleperc.text.toString().replace("??", st.mush.toString())
        viewHolder.music2.text = st.mus1.toString()

        viewHolder.music2.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(st.mus1 == null)
                    return

                val intent = Intent(activity, MusicPlayer::class.java)
                intent.putExtra("Data", JsonEncoder.encode(st.mus1).toString())

                activity.startActivity(intent)
            }

        })

        viewHolder.loop.text = convertTime(st.loop0)

        viewHolder.loop1.text = convertTime(st.loop1)

        viewHolder.background.text = st.bg.toString()
        viewHolder.background.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(activity, ImageViewer::class.java)
                intent.putExtra("Data", JsonEncoder.encode(st.bg).toString())

                activity.startActivity(intent)
            }
        })
        viewHolder.castle.text = st.castle.toString()
        viewHolder.castle.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if (data.pack == "000003" && stm.id.id == 11)
                    return
                else {
                    val intent = Intent(activity, ImageViewer::class.java)
                    intent.putExtra("Data", JsonEncoder.encode(st.castle).toString())

                    st.castle.get().img

                    activity.startActivity(intent)
                }
            }
        })
        if (st.info != null) {
            if (st.info.drop.isNotEmpty()) {
                val linearLayoutManager = LinearLayoutManager(activity)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                viewHolder.drop.layoutManager = linearLayoutManager
                val dropRecycle = DropRecycle(st, activity)
                viewHolder.drop.adapter = dropRecycle
                ViewCompat.setNestedScrollingEnabled(viewHolder.drop, false)
            } else {
                viewHolder.droprow.visibility = View.GONE
                viewHolder.dropscroll.visibility = View.GONE
            }
            if (st.info.time.isNotEmpty()) {
                val linearLayoutManager = LinearLayoutManager(activity)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                viewHolder.score.layoutManager = linearLayoutManager
                val scoreRecycle = ScoreRecycle(st, activity)
                viewHolder.score.adapter = scoreRecycle
                ViewCompat.setNestedScrollingEnabled(viewHolder.score, false)
            } else {
                viewHolder.scorerow.visibility = View.GONE
                viewHolder.scorescroll.visibility = View.GONE
            }
            if (st.info.drop.isEmpty() && st.info.time.isEmpty()) {
                viewHolder.droptitle.visibility = View.GONE
            }
        } else {
            viewHolder.droprow.visibility = View.GONE
            viewHolder.dropscroll.visibility = View.GONE
            viewHolder.scorerow.visibility = View.GONE
            viewHolder.scorescroll.visibility = View.GONE
            viewHolder.droptitle.visibility = View.GONE
        }
        val l = st.getLim(viewHolder.star.selectedItemPosition)
        if (none(l)) {
            viewHolder.limitscroll.visibility = View.GONE
            viewHolder.limitNone.visibility = View.VISIBLE
        } else {
            viewHolder.limitscroll.visibility = View.VISIBLE
            viewHolder.limitNone.visibility = View.GONE
            if (data.id == l.sid || l.sid == -1) {
                if (viewHolder.star.selectedItemPosition == l.star || l.star == -1) {
                    viewHolder.limitrec.layoutManager = LinearLayoutManager(activity)
                    ViewCompat.setNestedScrollingEnabled(viewHolder.limitrec, false)
                    val limitRecycle = LimitRecycle(activity, l)
                    viewHolder.limitrec.adapter = limitRecycle
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    private fun none(l: Limit?): Boolean {
        if (l == null) return true
        val b0 = l.line == 0
        val b1 = l.min == 0
        val b2 = l.max == 0
        val b3 = l.group == null
        val b4 = l.num == 0
        val b5 = l.rare == 0
        return b0 && b1 && b2 && b3 && b4 && b5
    }

    private fun convertTime(t: Long) : String {
        var min = t / 1000 / 60

        var time = (t.toDouble() - min * 60.0 * 1000.0) / 1000.0

        val df = DecimalFormat("#.###")

        time = df.format(time).toDouble()

        if(time >= 60) {
            time -= 60
            min += 1
        }

        return if(time < 10) {
            "$min:0${df.format(time)}"
        } else {
            "$min:${df.format(time)}"
        }
    }
}