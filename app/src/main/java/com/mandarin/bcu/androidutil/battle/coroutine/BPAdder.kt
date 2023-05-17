package com.mandarin.bcu.androidutil.battle.coroutine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.BattlePrepare
import com.mandarin.bcu.BattleSimulation
import com.mandarin.bcu.LineUpScreen
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.battle.BasisSet
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

open class BPAdder : CoroutineTask<String> {
    private val weakReference: WeakReference<Activity>
    private val data: Identifier<Stage>
    private var selection = 0
    private var item = 0

    private val done = "1"

    constructor(activity: Activity, data: Identifier<Stage>) : super() {
        weakReference = WeakReference(activity)
        this.data = data
    }

    constructor(activity: Activity, data: Identifier<Stage>, seleciton: Int) : super() {
        weakReference = WeakReference(activity)
        this.data = data
        selection = seleciton
    }

    override fun prepare() {
        val activity = weakReference.get() ?: return
        val setname = activity.findViewById<TextView>(R.id.lineupname)
        val star = activity.findViewById<Spinner>(R.id.battlestar)
        val equip = activity.findViewById<Button>(R.id.battleequip)
        val sniper = activity.findViewById<CheckBox>(R.id.battlesniper)
        val rich = activity.findViewById<CheckBox>(R.id.battlerich)
        val start = activity.findViewById<Button>(R.id.battlestart)
        val layout = activity.findViewById<LinearLayout>(R.id.preparelineup)
        val stname = activity.findViewById<TextView>(R.id.battlestgname)
        val v = activity.findViewById<View>(R.id.view)
        val lvlim = activity.findViewById<Spinner>(R.id.battlelvlim)
        val plus = activity.findViewById<CheckBox>(R.id.battleplus)
        setDisappear(setname, star, equip, sniper, rich, start, layout, stname, lvlim, plus)
        v?.let { setDisappear(it) }
    }

    override fun doSomething() {
        val activity = weakReference.get() ?: return

        Definer.define(activity, this::updateProg, this::updateText)

        publishProgress(StaticStore.TEXT, activity.getString(R.string.lineup_reading))

        if (!StaticStore.LUread) {
            try {
                BasisSet.read()
            } catch (e: Exception) {
                publishProgress(activity.getString(R.string.lineup_file_err))
                BasisSet.list().clear()
                BasisSet()
                ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
            }

            StaticStore.LUread = true
        }

        val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        var set = preferences.getInt("equip_set", 0)
        var lu = preferences.getInt("equip_lu", 0)

        if (set >= BasisSet.list().size)
            set = if(BasisSet.list().size == 0)
                0
            else
                BasisSet.list().size - 1

        BasisSet.setCurrent(BasisSet.list()[set])

        if (lu >= BasisSet.current().lb.size)
            lu = if(BasisSet.current().lb.size == 0)
                0
            else
                BasisSet.current().lb.size - 1

        BasisSet.current().sele = BasisSet.current().lb[lu]

        publishProgress(done)

        return
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return
        val loadt = activity.findViewById<TextView>(R.id.status)
        when (data[0]) {
            StaticStore.TEXT -> {
                loadt.text = data[1]
            }
            StaticStore.PROG -> {
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                if(data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
            done -> {
                val line: LineUpView = activity.findViewById(R.id.lineupView)
                val setname = activity.findViewById<TextView>(R.id.lineupname)
                val star = activity.findViewById<Spinner>(R.id.battlestar)
                val equip = activity.findViewById<Button>(R.id.battleequip)
                val sniper = activity.findViewById<CheckBox>(R.id.battlesniper)
                val rich = activity.findViewById<CheckBox>(R.id.battlerich)
                val start = activity.findViewById<Button>(R.id.battlestart)
                val stname = activity.findViewById<TextView>(R.id.battlestgname)
                val prog = activity.findViewById<ProgressBar>(R.id.prog)
                val lvlim = activity.findViewById<Spinner>(R.id.battlelvlim)
                val plus = activity.findViewById<CheckBox>(R.id.battleplus)

                prog.isIndeterminate = true

                line.updateLineUp()
                setname.text = setLUName

                val st = Identifier.get(this.data) ?: return
                val stm = st.cont ?: return

                stname.text = MultiLangCont.get(st) ?: st.names.toString()

                if(stname.text.isBlank())
                    stname.text = getStageName(this.data.id)

                val stars = ArrayList<String>()
                var i = 0
                while (i < stm.stars.size) {
                    val s = (i + 1).toString() + " (" + stm.stars[i] + " %)"
                    stars.add(s)
                    i++
                }
                val arrayAdapter = ArrayAdapter(activity, R.layout.spinneradapter, stars)
                star.adapter = arrayAdapter
                if (selection < stars.size && selection >= 0) star.setSelection(selection)
                equip.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(activity, LineUpScreen::class.java)

                        if(activity is BattlePrepare) {
                            activity.resultLauncher.launch(intent)
                        }
                    }
                })
                sniper.isChecked = BattlePrepare.sniper

                if(BattlePrepare.sniper) {
                    item += 2
                }

                if(BattlePrepare.rich) {
                    item += 1
                }

                sniper.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        item += 2
                    } else {
                        item -= 2
                    }
                    BattlePrepare.sniper = isChecked
                }

                rich.isChecked = BattlePrepare.rich
                rich.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        item += 1
                    } else {
                        item -= 1
                    }
                    BattlePrepare.rich = isChecked
                }

                start.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val restricted = restrictLevel(st)

                        val intent = Intent(activity, BattleSimulation::class.java)

                        intent.putExtra("Data", JsonEncoder.encode(this@BPAdder.data).toString())
                        intent.putExtra("star", star.selectedItemPosition)
                        intent.putExtra("item", item)
                        intent.putExtra("restricted", restricted)

                        activity.startActivity(intent)
                        BattlePrepare.rich = false
                        BattlePrepare.sniper = false
                        activity.finish()
                    }
                })

                line.setOnTouchListener { _: View?, event: MotionEvent ->
                    val posit: IntArray?
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            line.posx = event.x
                            line.posy = event.y
                            line.touched = true
                            line.invalidate()
                            if (!line.drawFloating) {
                                posit = line.getTouchedUnit(event.x, event.y)
                                if (posit != null) {
                                    line.prePosit = posit
                                }
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            line.posx = event.x
                            line.posy = event.y
                            if (!line.drawFloating) {
                                line.floatB = line.getUnitImage(line.prePosit[0], line.prePosit[1])
                            }
                            line.drawFloating = true
                        }
                        MotionEvent.ACTION_UP -> {
                            line.checkChange()
                            val deleted = line.getTouchedUnit(event.x, event.y)
                            if (deleted != null) {
                                if (deleted[0] == -100) {
                                    StaticStore.position = intArrayOf(-1, -1)
                                    line.updateUnitSetting()
                                    line.updateUnitOrb()
                                } else {
                                    StaticStore.position = deleted
                                    line.updateUnitSetting()
                                    line.updateUnitOrb()
                                }
                            }
                            line.drawFloating = false
                            line.touched = false
                        }
                    }
                    true
                }

                val bck: FloatingActionButton = activity.findViewById(R.id.battlebck)
                bck.setOnClickListener {
                    BattlePrepare.rich = false
                    BattlePrepare.sniper = false
                    activity.finish()
                }

                val lvlimText = ArrayList<String>()

                for(n in 0..50) {
                    if(n == 0) {
                        lvlimText.add(activity.getString(R.string.battle_lvlimoff))
                    } else {
                        lvlimText.add(n.toString())
                    }
                }

                if(st.isAkuStage) {
                    val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                    val ed = shared.edit()

                    val lvLimAdapter = object : ArrayAdapter<String>(activity, R.layout.spinneradapter, lvlimText) {

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v = super.getDropDownView(position, convertView, parent)

                            v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                            lvlim.dropDownWidth = max(lvlim.dropDownWidth, v.measuredWidth)

                            return v
                        }
                    }

                    lvlim.adapter = lvLimAdapter

                    lvlim.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                            CommonStatic.getConfig().levelLimit = position
                            plus.isEnabled = CommonStatic.getConfig().levelLimit > 0

                            ed.putInt("levelLimit", position)
                            ed.apply()
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }

                    plus.setOnCheckedChangeListener { _, isChecked ->
                        CommonStatic.getConfig().plus = isChecked

                        ed.putBoolean("unlockPlus", isChecked)
                        ed.apply()
                    }

                    lvlim.setSelection(CommonStatic.getConfig().levelLimit)
                    plus.isChecked = CommonStatic.getConfig().plus

                    plus.isEnabled = CommonStatic.getConfig().levelLimit > 0
                }
            }
            else -> StaticStore.showShortMessage(activity, data[0])
        }
    }

    override fun finish() {
        val activity = weakReference.get() ?: return
        val line: LineUpView = activity.findViewById(R.id.lineupView)
        val setname = activity.findViewById<TextView>(R.id.lineupname)
        val star = activity.findViewById<Spinner>(R.id.battlestar)
        val equip = activity.findViewById<Button>(R.id.battleequip)
        val sniper = activity.findViewById<CheckBox>(R.id.battlesniper)
        val rich = activity.findViewById<CheckBox>(R.id.battlerich)
        val start = activity.findViewById<Button>(R.id.battlestart)
        val layout = activity.findViewById<LinearLayout>(R.id.preparelineup)
        val stname = activity.findViewById<TextView>(R.id.battlestgname)
        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        val t = activity.findViewById<TextView>(R.id.status)
        val lvlim = activity.findViewById<Spinner>(R.id.battlelvlim)
        val plus = activity.findViewById<CheckBox>(R.id.battleplus)
        setAppear(line, setname, star, equip, sniper, rich, start, layout, stname)
        val st = Identifier.get(this.data)

        if(st != null && st.isAkuStage) {
            setAppear(lvlim, plus)
        }

        setDisappear(prog, t)
        val v = activity.findViewById<View>(R.id.view)
        v?.let { setAppear(it) }
    }

    private val setLUName: String
        get() = BasisSet.current().name + " - " + BasisSet.current().sele.name

    private fun setDisappear(vararg views: View) {
        for (v in views) v.visibility = View.GONE
    }

    private fun setAppear(vararg views: View) {
        for (v in views) v.visibility = View.VISIBLE
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }

    private fun getStageName(posit: Int) : String {
        return "Stage"+number(posit)
    }

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }

    private fun restrictLevel(st: Stage) : Boolean {
        var changed = false

        if(st.lim != null && st.lim.lvr != null) {
            val lu = BasisSet.current().sele

            for(forms in lu.lu.fs) {
                for(form in forms) {
                    form ?: continue

                    val level = lu.lu.map[form.unit.id] ?: continue

                    var temp = level.lv

                    level.setLevel(min(level.lv, st.lim.lvr.all[0]))

                    if(!changed && temp != level.lv)
                        changed = true

                    temp = level.plusLv

                    level.setLevel(min(level.plusLv, st.lim.lvr.all[1]))

                    if(!changed && temp != level.plusLv)
                        changed = true

                    for(i in 2 until st.lim.lvr.all.size) {
                        if (i - 2 >= level.talents.size)
                            break

                        temp = level.talents[i - 2]

                        level.talents[i - 2] = min(level.talents[i - 2], st.lim.lvr.all[i])

                        if(!changed && temp != level.talents[i - 2])
                            changed = true
                    }
                }
            }
        }

        return changed
    }
}