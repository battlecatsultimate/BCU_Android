package com.mandarin.bcu.androidutil.stage.adapters

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.ImageViewer
import com.mandarin.bcu.MusicPlayer
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.battle.BasisSet
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.Data
import common.util.stage.Limit
import common.util.stage.Stage
import common.util.stage.info.DefStageInfo
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class StageRecycle(private val activity: Activity, private val data: Identifier<Stage>) : RecyclerView.Adapter<StageRecycle.ViewHolder>() {
    private val s: GetStrings = GetStrings(activity)

    private var isRaw = false

    private val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
    private val color: IntArray = intArrayOf(
        StaticStore.getAttributeColor(activity, R.attr.TextPrimary)
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pack = itemView.findViewById<Button>(R.id.stginfopack)!!
        val stgpack = itemView.findViewById<TextView>(R.id.stginfopackr)!!
        val id = itemView.findViewById<TextView>(R.id.stginfoidr)!!
        val star = itemView.findViewById<Spinner>(R.id.stginfostarr)!!
        val energy = itemView.findViewById<TextView>(R.id.stginfoengr)!!
        val xp = itemView.findViewById<TextView>(R.id.stginfoxpr)!!
        val health = itemView.findViewById<TextView>(R.id.stginfobhr)!!
        val difficulty = itemView.findViewById<TextView>(R.id.stginfodifr)!!
        val continueable = itemView.findViewById<TextView>(R.id.stginfocontinr)!!
        val length = itemView.findViewById<TextView>(R.id.stginfolenr)!!
        val maxenemy = itemView.findViewById<TextView>(R.id.stginfomaxenr)!!
        val music = itemView.findViewById<Button>(R.id.stginfomusicr)!!
        val castleperc = itemView.findViewById<TextView>(R.id.stginfomusic2)!!
        val music2 = itemView.findViewById<Button>(R.id.stginfomusic2r)!!
        val background = itemView.findViewById<Button>(R.id.stginfobgr)!!
        val castle = itemView.findViewById<Button>(R.id.stginfoctr)!!
        val extitle = itemView.findViewById<TextView>(R.id.stginfoex)!!
        val ex = itemView.findViewById<RecyclerView>(R.id.exrec)!!
        val exrow = itemView.findViewById<TableRow>(R.id.ex)!!
        val exscroll = itemView.findViewById<NestedScrollView>(R.id.exscroll)!!
        val droptitle = itemView.findViewById<TextView>(R.id.stginfodrop)!!
        val drop = itemView.findViewById<RecyclerView>(R.id.droprec)!!
        val droprow = itemView.findViewById<TableRow>(R.id.drop)!!
        val dropscroll = itemView.findViewById<NestedScrollView>(R.id.dropscroll)!!
        val score = itemView.findViewById<RecyclerView>(R.id.scorerec)!!
        val scorerow = itemView.findViewById<TableRow>(R.id.score)!!
        val scorescroll = itemView.findViewById<NestedScrollView>(R.id.scorescroll)!!
        val limitNone = itemView.findViewById<TextView>(R.id.stginfononer)!!
        val limitrec = itemView.findViewById<RecyclerView>(R.id.stginfolimitrec)!!
        val limitscroll = itemView.findViewById<NestedScrollView>(R.id.limitscroll)!!
        val miscrec = itemView.findViewById<RecyclerView>(R.id.stginfomiscrec)!!
        val miscnone = itemView.findViewById<TextView>(R.id.miscnone)!!
        val miscscroll = itemView.findViewById<NestedScrollView>(R.id.miscscroll)!!
        val chanceText = itemView.findViewById<TextView>(R.id.stfinfochance)!!
        val loop = itemView.findViewById<TextView>(R.id.stginfoloopt)!!
        val loop1 = itemView.findViewById<TextView>(R.id.stginfoloop1t)!!
        val minres = itemView.findViewById<TextView>(R.id.stginfominrest)!!
        val bossGuard = itemView.findViewById<TextView>(R.id.stginfoguardval);
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.stage_info_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val t = BasisSet.current().t()

        val st = Identifier.get(data) ?: return
        val stm = st.cont ?: return

        viewHolder.id.text = s.getID(st.cont.cont.sid, stm.id.id, data.id)

        val stars: MutableList<String> = ArrayList()

        for (k in stm.stars.indices) {
            val s: String = (k + 1).toString() + " (" + stm.stars[k] + " %)"

            stars.add(s)
        }

        if(st.info != null && st.info is DefStageInfo && (st.info as DefStageInfo).drop != null) {
            val info = st.info as DefStageInfo
            if(info.drop.size >= 2 || info.rand == -3) {
                var same = true
                val d = info.drop[0][0]

                for(data in info.drop) {
                    if(d != data[0])
                        same = false
                }

                if(same) {
                    viewHolder.chanceText.text = activity.getText(R.string.stg_enem_list_num)
                }
            }
        }

        viewHolder.stgpack.text = s.getPackName(st.cont.cont.sid, isRaw)

        viewHolder.pack.setOnClickListener {
            isRaw = !isRaw

            viewHolder.stgpack.text = s.getPackName(st.cont.cont.sid, isRaw)
        }

        val arrayAdapter = ArrayAdapter(activity, R.layout.spinnerdefault, stars)

        viewHolder.star.adapter = arrayAdapter

        viewHolder.star.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val enrec = activity.findViewById<RecyclerView>(R.id.stginfoenrec)

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

        if (st.info != null && st.info is DefStageInfo) {
            val info = st.info as DefStageInfo
            if (st.cont.cont.sid == "000000" || st.cont.cont.sid == "000013")
                viewHolder.xp.text = s.getXP(info.xp, t, true)
            else
                viewHolder.xp.text = s.getXP(info.xp, t, false)
        } else {
            viewHolder.xp.text = "0"
        }

        viewHolder.energy.text = s.getEnergy(st, activity)

        viewHolder.health.text = st.health.toString()

        if (st.info != null && st.info is DefStageInfo)
            viewHolder.difficulty.text = s.getDifficulty((st.info as DefStageInfo).diff, activity)
        else
            viewHolder.difficulty.setText(R.string.unit_info_t_none)

        viewHolder.continueable.text = if (st.non_con)
            activity.getString(R.string.stg_info_impo)
        else
            activity.getString(R.string.stg_info_poss)

        viewHolder.length.text = st.len.toString()

        viewHolder.maxenemy.text = st.max.toString()

        viewHolder.music.text = if(st.mus0 == null || st.mus0.id == -1) {
            activity.getString(R.string.unit_info_t_none)
        } else {
            StaticStore.generateIdName(st.mus0, activity)
        }

        viewHolder.music.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(st.mus0 == null || st.mus0.id == -1)
                    return

                val intent = Intent(activity, MusicPlayer::class.java)

                intent.putExtra("Data", JsonEncoder.encode(st.mus0).toString())

                activity.startActivity(intent)
            }

        })

        viewHolder.music.setOnLongClickListener {
            st.mus0 ?: return@setOnLongClickListener true

            StaticStore.showShortMessage(activity, st.mus0.pack)

            true
        }

        viewHolder.castleperc.text = viewHolder.castleperc.text.toString().replace("??", st.mush.toString())

        viewHolder.music2.text = if(st.mus1 == null || st.mus1.id == -1) {
            activity.getString(R.string.unit_info_t_none)
        } else {
            StaticStore.generateIdName(st.mus1, activity)
        }

        viewHolder.music2.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(st.mus1 == null || st.mus1.id == -1)
                    return

                val intent = Intent(activity, MusicPlayer::class.java)

                intent.putExtra("Data", JsonEncoder.encode(st.mus1).toString())

                activity.startActivity(intent)
            }

        })

        viewHolder.music2.setOnLongClickListener {
            st.mus1 ?: return@setOnLongClickListener true

            StaticStore.showShortMessage(activity, st.mus1.pack)

            true
        }

        viewHolder.loop.text = convertTime(st.mus0?.get()?.loop ?: 0)

        viewHolder.loop1.text = convertTime(st.mus1?.get()?.loop ?: 0)

        viewHolder.background.text = StaticStore.generateIdName(st.bg, activity)

        viewHolder.background.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                st.bg ?: return

                val intent = Intent(activity, ImageViewer::class.java)

                intent.putExtra("Img", ImageViewer.ViewerType.BACKGROUND.name)
                intent.putExtra("Data", JsonEncoder.encode(st.bg).toString())

                activity.startActivity(intent)
            }
        })

        viewHolder.background.setOnLongClickListener {
            st.bg ?: return@setOnLongClickListener true

            StaticStore.showShortMessage(activity, st.bg.pack)

            true
        }

        viewHolder.castle.text = if(st.castle == null) {
            "None"
        } else {
            StaticStore.generateIdName(st.castle, activity)
        }

        viewHolder.castle.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                st.castle ?: return

                if (st.cont.cont.sid == "000003" && stm.id.id == 11)
                    return
                else {
                    val intent = Intent(activity, ImageViewer::class.java)

                    intent.putExtra("Img", ImageViewer.ViewerType.CASTLE.name)
                    intent.putExtra("Data", JsonEncoder.encode(st.castle).toString())

                    st.castle.get().img

                    activity.startActivity(intent)
                }
            }
        })

        viewHolder.castle.setOnLongClickListener {
            st.castle ?: return@setOnLongClickListener true

            StaticStore.showShortMessage(activity, st.castle.pack)

            true
        }

        viewHolder.minres.text = toFrame(st.minSpawn, st.maxSpawn)

        viewHolder.bossGuard.text = if (st.bossGuard)
            activity.getString(R.string.stg_info_poss)
        else
            activity.getString(R.string.stg_info_impo)

        if (st.info != null) {
            if(st.info.hasExConnection() || st.info.exStages != null) {
                val linearLayoutManager = LinearLayoutManager(activity)

                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

                viewHolder.ex.layoutManager = linearLayoutManager

                viewHolder.ex.adapter = ExRecycle(st, activity)
            } else {
                viewHolder.exrow.visibility = View.GONE
                viewHolder.exscroll.visibility = View.GONE
                viewHolder.extitle.visibility = View.GONE
            }

            if (st.info is DefStageInfo && (st.info as DefStageInfo).drop.isNotEmpty()) {
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

            if (st.info is DefStageInfo && (st.info as DefStageInfo).time.isNotEmpty()) {
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
            if (st.info is DefStageInfo && (st.info as DefStageInfo).drop.isEmpty() && (st.info as DefStageInfo).time.isEmpty()) {
                viewHolder.droptitle.visibility = View.GONE
            }
        } else {
            viewHolder.exrow.visibility = View.GONE
            viewHolder.exscroll.visibility = View.GONE
            viewHolder.extitle.visibility = View.GONE
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

        val misc = s.getMiscellaneous(st)

        if(misc.isEmpty()) {
            viewHolder.miscscroll.visibility = View.GONE
            viewHolder.miscnone.visibility = View.VISIBLE
        } else {
            viewHolder.miscscroll.visibility = View.VISIBLE
            viewHolder.miscnone.visibility = View.GONE

            viewHolder.miscrec.layoutManager = LinearLayoutManager(activity)

            ViewCompat.setNestedScrollingEnabled(viewHolder.miscrec, false)

            val miscRecycle = MiscRecycle(activity, misc)

            viewHolder.miscrec.adapter = miscRecycle
        }

        val stlev = activity.findViewById<TextInputLayout>(R.id.stlev)
        val sttrea = activity.findViewById<TextInputLayout>(R.id.sttrea)
        val sttrea2 = activity.findViewById<TextInputLayout>(R.id.sttrea2)

        val stlevt = activity.findViewById<TextInputEditText>(R.id.stlevt)
        val sttreat = activity.findViewById<TextInputEditText>(R.id.sttreat)
        val sttreat2 = activity.findViewById<TextInputEditText>(R.id.sttreat2)

        val reset = activity.findViewById<Button>(R.id.treasurereset)

        stlev.isCounterEnabled = true
        stlev.counterMaxLength = 2
        stlev.setHelperTextColor(ColorStateList(states, color))

        sttrea.isCounterEnabled = true
        sttrea.counterMaxLength = 3
        sttrea.setHelperTextColor(ColorStateList(states, color))

        sttrea2.isCounterEnabled = true
        sttrea2.counterMaxLength = 3
        sttrea2.setHelperTextColor(ColorStateList(states, color))

        stlevt.setText(t.tech[Data.LV_XP].toString())

        sttreat.setText(t.trea[Data.T_XP1].toString())

        sttreat2.setText(t.trea[Data.T_XP2].toString())

        stlevt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty()) {
                    if(s.toString().toInt() > 30 || s.toString().toInt() <= 0) {
                        if(stlev.isHelperTextEnabled) {
                            stlev.isHelperTextEnabled = false
                            stlev.isErrorEnabled = true
                            stlev.error = activity.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if(stlev.isErrorEnabled) {
                            stlev.error = null
                            stlev.isErrorEnabled = false
                            stlev.isHelperTextEnabled = true
                            stlev.setHelperTextColor(ColorStateList(states, color))
                            stlev.helperText = "1~30 Lv."
                        }
                    }
                } else {
                    if(stlev.isErrorEnabled) {
                        stlev.error = null
                        stlev.isErrorEnabled = false
                        stlev.isHelperTextEnabled = true
                        stlev.setHelperTextColor(ColorStateList(states, color))
                        stlev.helperText = "1~30 Lv."
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if(text.toString().isNotEmpty()) {
                    if(text.toString().toInt() in 1..30) {
                        val lev = text.toString().toInt()

                        t.tech[Data.LV_XP] = lev

                        if (st.info != null && st.info is DefStageInfo) {
                            val info = st.info as DefStageInfo
                            if (st.cont.cont.sid == "000000" || st.cont.cont.sid == "000013")
                                viewHolder.xp.text = s.getXP(info.xp, t, true)
                            else
                                viewHolder.xp.text = s.getXP(info.xp, t, false)
                        } else {
                            viewHolder.xp.text = "0"
                        }
                    }
                }
            }

        })

        sttreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty()) {
                    if(s.toString().toInt() > 300 || s.toString().toInt() < 0) {
                        if(sttrea.isHelperTextEnabled) {
                            sttrea.isHelperTextEnabled = false
                            sttrea.isErrorEnabled = true
                            sttrea.error = activity.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if(sttrea.isErrorEnabled) {
                            sttrea.error = null
                            sttrea.isErrorEnabled = false
                            sttrea.isHelperTextEnabled = true
                            sttrea.setHelperTextColor(ColorStateList(states, color))
                            sttrea.helperText = "0~300 %"
                        }
                    }
                } else {
                    if(sttrea.isErrorEnabled) {
                        sttrea.error = null
                        sttrea.isErrorEnabled = false
                        sttrea.isHelperTextEnabled = true
                        sttrea.setHelperTextColor(ColorStateList(states, color))
                        sttrea.helperText = "0~300 %"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if(text.toString().isNotEmpty()) {
                    if(text.toString().toInt() in 0..300) {
                        val lev = text.toString().toInt()

                        t.trea[Data.T_XP1] = lev

                        if (st.info != null && st.info is DefStageInfo) {
                            val info = st.info as DefStageInfo
                            if (st.cont.cont.sid == "000000" || st.cont.cont.sid == "000013")
                                viewHolder.xp.text = s.getXP(info.xp, t, true)
                            else
                                viewHolder.xp.text = s.getXP(info.xp, t, false)
                        } else {
                            viewHolder.xp.text = "0"
                        }
                    }
                }
            }
        })

        sttreat2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty()) {
                    if(s.toString().toInt() > 300 || s.toString().toInt() < 0) {
                        if(sttrea2.isHelperTextEnabled) {
                            sttrea2.isHelperTextEnabled = false
                            sttrea2.isErrorEnabled = true
                            sttrea2.error = activity.getString(R.string.treasure_invalid)
                        }
                    } else {
                        if(sttrea2.isErrorEnabled) {
                            sttrea2.error = null
                            sttrea2.isErrorEnabled = false
                            sttrea2.isHelperTextEnabled = true
                            sttrea2.setHelperTextColor(ColorStateList(states, color))
                            sttrea2.helperText = "0~300 %"
                        }
                    }
                } else {
                    if(sttrea2.isErrorEnabled) {
                        sttrea2.error = null
                        sttrea2.isErrorEnabled = false
                        sttrea2.isHelperTextEnabled = true
                        sttrea2.setHelperTextColor(ColorStateList(states, color))
                        sttrea2.helperText = "0~300 %"
                    }
                }
            }

            override fun afterTextChanged(text: Editable) {
                if(text.toString().isNotEmpty()) {
                    if(text.toString().toInt() in 0..300) {
                        val lev = text.toString().toInt()

                        t.trea[Data.T_XP2] = lev

                        if (st.info != null && st.info is DefStageInfo) {
                            val info = st.info as DefStageInfo
                            if (st.cont.cont.sid == "000000" || st.cont.cont.sid == "000013")
                                viewHolder.xp.text = s.getXP(info.xp, t, true)
                            else
                                viewHolder.xp.text = s.getXP(info.xp, t, false)
                        } else {
                            viewHolder.xp.text = "0"
                        }
                    }
                }
            }
        })

        reset.setOnClickListener {
            t.tech[Data.LV_XP] = 30
            t.trea[Data.T_XP1] = 300
            t.trea[Data.T_XP2] = 300

            stlevt.setText(t.tech[Data.LV_XP].toString())
            sttreat.setText(t.trea[Data.T_XP1].toString())
            sttreat2.setText(t.trea[Data.T_XP2].toString())

            if (st.info != null && st.info is DefStageInfo) {
                val info = st.info as DefStageInfo
                if (st.cont.cont.sid == "000000" || st.cont.cont.sid == "000013")
                    viewHolder.xp.text = s.getXP(info.xp, t, true)
                else
                    viewHolder.xp.text = s.getXP(info.xp, t, false)
            } else {
                viewHolder.xp.text = "0"
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

        val df = NumberFormat.getInstance(Locale.US) as DecimalFormat
        df.applyPattern("#.###")

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

    private fun toFrame(min: Int, max: Int) : String {
        return if(min == max) {
            "${min}f"
        } else {
            "${min}f ~ ${max}f"
        }
    }
}