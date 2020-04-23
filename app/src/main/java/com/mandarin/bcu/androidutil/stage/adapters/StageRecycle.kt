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
import common.util.pack.Pack
import common.util.stage.Limit
import java.util.*

class StageRecycle(private val activity: Activity, private val mapcode: Int, private val stid: Int, private val posit: Int, private val custom: Boolean) : RecyclerView.Adapter<StageRecycle.ViewHolder>() {
    private val s: GetStrings = GetStrings(activity)
    private val castles = intArrayOf(45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 32, 31, 30, 29, 28, 27, 26, 25, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 46, 47, 45, 47, 47, 45, 45)
    private val wc = listOf(3, 4, 5, 10, 12)
    private val ec = listOf(0, 1, 2, 9)
    private val sc = listOf(6, 7, 8)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var id: TextView = itemView.findViewById(R.id.stginfoidr)
        var star: Spinner = itemView.findViewById(R.id.stginfostarr)
        var energy: TextView = itemView.findViewById(R.id.stginfoengr)
        var xp: TextView = itemView.findViewById(R.id.stginfoxpr)
        var health: TextView = itemView.findViewById(R.id.stginfobhr)
        var difficulty: TextView = itemView.findViewById(R.id.stginfodifr)
        var continueable: TextView = itemView.findViewById(R.id.stginfocontinr)
        var length: TextView = itemView.findViewById(R.id.stginfolenr)
        var maxenemy: TextView = itemView.findViewById(R.id.stginfomaxenr)
        var music: Button = itemView.findViewById(R.id.stginfomusicr)
        var castleperc: TextView = itemView.findViewById(R.id.stginfomusic2)
        var music2: Button = itemView.findViewById(R.id.stginfomusic2r)
        var background: Button = itemView.findViewById(R.id.stginfobgr)
        var castle: Button = itemView.findViewById(R.id.stginfoctr)
        var droptitle: TextView = itemView.findViewById(R.id.stginfodrop)
        var drop: RecyclerView = itemView.findViewById(R.id.droprec)
        var droprow: TableRow = itemView.findViewById(R.id.drop)
        var dropscroll: NestedScrollView = itemView.findViewById(R.id.dropscroll)
        var score: RecyclerView = itemView.findViewById(R.id.scorerec)
        var scorerow: TableRow = itemView.findViewById(R.id.score)
        var scorescroll: NestedScrollView = itemView.findViewById(R.id.scorescroll)
        var limitNone: TextView = itemView.findViewById(R.id.stginfononer)
        var limitrec: RecyclerView = itemView.findViewById(R.id.stginfolimitrec)
        var limitscroll: NestedScrollView = itemView.findViewById(R.id.limitscroll)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stage_info_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val t = StaticStore.t
        val mc = StaticStore.map[mapcode] ?: return
        if (stid >= mc.maps.size || stid < 0) return
        val stm = mc.maps[stid] ?: return
        if (posit >= stm.list.size || posit < 0) return
        val st = stm.list[posit]
        viewHolder.id.text = s.getID(mapcode, stid, posit)
        val stars: MutableList<String> = ArrayList()
        for (k in stm.stars.indices) {
            val s: String = (k + 1).toString() + " (" + stm.stars[k] + " %)"
            stars.add(s)
        }
        val arrayAdapter = ArrayAdapter(activity, R.layout.spinneradapter, stars)
        viewHolder.star.adapter = arrayAdapter
        viewHolder.star.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val enrec: RecyclerView = activity.findViewById(R.id.stginfoenrec)
                enrec.layoutManager = LinearLayoutManager(activity)
                ViewCompat.setNestedScrollingEnabled(enrec, false)
                val listRecycle = EnemyListRecycle(activity, st, stm.stars[position], mapcode, custom)
                enrec.adapter = listRecycle
                StaticStore.stageSpinner = position
                val l = st.getLim(position)
                if (none(l)) {
                    viewHolder.limitNone.visibility = View.VISIBLE
                    viewHolder.limitscroll.visibility = View.GONE
                } else {
                    viewHolder.limitscroll.visibility = View.VISIBLE
                    viewHolder.limitNone.visibility = View.GONE
                    if (posit == l.sid || l.sid == -1) {
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
            if (mapcode == 0 || mapcode == 13) viewHolder.xp.text = s.getXP(st.info.xp, t, true) else viewHolder.xp.text = s.getXP(st.info.xp, t, false)
        } else {
            viewHolder.xp.text = "0"
        }
        if (st.info != null) viewHolder.energy.text = st.info.energy.toString() else viewHolder.energy.text = "0"
        viewHolder.health.text = st.health.toString()
        if (st.info != null) viewHolder.difficulty.text = s.getDifficulty(st.info.diff) else viewHolder.difficulty.setText(R.string.unit_info_t_none)
        viewHolder.continueable.text = if (st.non_con) activity.getString(R.string.stg_info_impo) else activity.getString(R.string.stg_info_poss)
        viewHolder.length.text = st.len.toString()
        viewHolder.maxenemy.text = st.max.toString()
        viewHolder.music.text = st.mus0.toString()

        viewHolder.music.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(st.mus0 < 0)
                    return

                val intent = Intent(activity, MusicPlayer::class.java)

                if(st.mus0 >= 1000) {
                    intent.putExtra("PID", StaticStore.getPID(st.mus0))
                    intent.putExtra("Music", StaticStore.getMusicIndex(st.mus0))
                } else {
                    intent.putExtra("Music", st.mus0)
                }

                activity.startActivity(intent)
            }

        })

        viewHolder.castleperc.text = viewHolder.castleperc.text.toString().replace("??", st.mush.toString())
        viewHolder.music2.text = st.mus1.toString()

        viewHolder.music2.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(st.mus1 < 0)
                    return

                val intent = Intent(activity, MusicPlayer::class.java)

                if(st.mus1 >= 1000) {
                    println(StaticStore.getMusicIndex(st.mus1))

                    intent.putExtra("PID", StaticStore.getPID(st.mus1))
                    intent.putExtra("Music", StaticStore.getMusicIndex(st.mus1))
                } else {
                    intent.putExtra("Music", st.mus1)
                }

                activity.startActivity(intent)
            }

        })

        viewHolder.background.text = st.bg.toString()
        viewHolder.background.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                val intent = Intent(activity, ImageViewer::class.java)

                if(st.bg < 1000) {
                    intent.putExtra("Path", StaticStore.getExternalPath(activity)+"org/img/bg/bg" + number(st.bg) + ".png")
                    intent.putExtra("Img", 0)
                    intent.putExtra("BGNum", st.bg)
                } else {
                    val pid = StaticStore.getPID(st.bg)
                    val p = Pack.map[pid]

                    if(p != null) {
                        intent.putExtra("PID", pid)
                        intent.putExtra("Img", 0)
                        intent.putExtra("BGNum", StaticStore.getID(st.bg))
                    }
                }
                activity.startActivity(intent)
            }
        })
        viewHolder.castle.text = st.castle.toString()
        viewHolder.castle.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if (mapcode == 3 && stid == 11) return
                if (mapcode == 3) {
                    when {
                        ec.contains(stid) -> {
                            val path = "./org/img/ec/ec" + number(castles[posit]) + ".png"
                            val intent = Intent(activity, ImageViewer::class.java)
                            intent.putExtra("Path", path)
                            intent.putExtra("Img", 1)
                            activity.startActivity(intent)
                        }
                        wc.contains(stid) -> {
                            val path = "./org/img/wc/wc" + number(castles[posit]) + ".png"
                            val intent = Intent(activity, ImageViewer::class.java)
                            intent.putExtra("Path", path)
                            intent.putExtra("Img", 1)
                            activity.startActivity(intent)
                        }
                        sc.contains(stid) -> {
                            val path = "./org/img/sc/sc" + number(castles[posit]) + ".png"
                            val intent = Intent(activity, ImageViewer::class.java)
                            intent.putExtra("Path", path)
                            intent.putExtra("Img", 1)
                            activity.startActivity(intent)
                        }
                    }
                } else {
                    val intent = Intent(activity, ImageViewer::class.java)

                    if(st.castle < 1000) {
                        val path = "./org/img/rc/rc" + number(st.castle) + ".png"

                        intent.putExtra("Path", path)
                        intent.putExtra("Img", 1)
                    } else {
                        intent.putExtra("Img",1)
                        intent.putExtra("PID",StaticStore.getPID(st.castle))
                        intent.putExtra("BGNum", StaticStore.getID(st.castle))
                    }

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
            if (posit == l.sid || l.sid == -1) {
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

    private fun number(n: Int): String {
        return when (n) {
            in 0..9 -> {
                "00$n"
            }
            in 10..98 -> {
                "0$n"
            }
            else -> {
                n.toString()
            }
        }
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

}