package com.mandarin.bcu.androidutil.battle.asynchs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.BattleSimulation
import com.mandarin.bcu.LineUpScreen
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.unit.Definer
import common.battle.BasisSet
import common.io.InStream
import common.system.MultiLangCont
import common.util.pack.Pack
import common.util.stage.MapColc
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.ref.WeakReference
import java.util.*

open class BPAdder : AsyncTask<Void?, Int?, Void?> {
    private val weakReference: WeakReference<Activity>
    private val mapcode: Int
    private val stid: Int
    private val posit: Int
    private var selection = 0
    private var item = 0

    constructor(activity: Activity, mapcode: Int, stid: Int, posit: Int) {
        weakReference = WeakReference(activity)
        this.mapcode = mapcode
        this.stid = stid
        this.posit = posit
    }

    constructor(activity: Activity, mapcode: Int, stid: Int, posit: Int, seleciton: Int) {
        weakReference = WeakReference(activity)
        this.mapcode = mapcode
        this.stid = stid
        this.posit = posit
        selection = seleciton
    }

    public override fun onPreExecute() {
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
        setDisappear(setname, star, equip, sniper, rich, start, layout, stname)
        v?.let { setDisappear(it) }
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)
        if (StaticStore.LUnames == null) {
            StaticStore.LUnames = arrayOfNulls(StaticStore.unitnumber)
            for (i in StaticStore.LUnames.indices) {
                StaticStore.LUnames[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist[i].forms[0]) ?: "")
            }
        }
        publishProgress(0)
        if (!StaticStore.LUread) {
            val path = Environment.getExternalStorageDirectory().path + "/BCU/user/basis.v"
            val f = File(path)
            val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
            if (f.exists()) {
                if (f.length() != 0L) {
                    val buff = ByteArray(f.length().toInt())
                    try {
                        val bis = BufferedInputStream(FileInputStream(f))
                        bis.read(buff, 0, buff.size)
                        bis.close()
                        val `is` = InStream.getIns(buff)
                        try {
                            BasisSet.read(`is`)
                        } catch (e: Exception) {
                            publishProgress(R.string.lineup_file_err)
                            BasisSet.list.clear()
                            BasisSet()
                            ErrorLogWriter.writeLog(e, preferences.getBoolean("upload", false) || preferences.getBoolean("ask_upload", true))
                        }
                    } catch (e: Exception) {
                        ErrorLogWriter.writeLog(e, preferences.getBoolean("upload", false) || preferences.getBoolean("ask_upload", true))
                    }
                }
            }
            StaticStore.LUread = true
        }
        StaticStore.sets = BasisSet.list
        val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        var set = preferences.getInt("equip_set", 0)
        var lu = preferences.getInt("equip_lu", 0)
        if (set >= BasisSet.list.size) set = BasisSet.list.size - 1
        BasisSet.current = StaticStore.sets[set]
        if (lu >= BasisSet.current.lb.size) lu = BasisSet.current.lb.size - 1
        BasisSet.current.sele = BasisSet.current.lb[lu]
        publishProgress(1)
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg results: Int?) {
        val activity = weakReference.get() ?: return
        val loadt = activity.findViewById<TextView>(R.id.preparet)
        when (results[0]) {
            0 -> loadt.setText(R.string.lineup_reading)
            1 -> {
                val line: LineUpView = activity.findViewById(R.id.lineupView)
                val setname = activity.findViewById<TextView>(R.id.lineupname)
                val star = activity.findViewById<Spinner>(R.id.battlestar)
                val equip = activity.findViewById<Button>(R.id.battleequip)
                val sniper = activity.findViewById<CheckBox>(R.id.battlesniper)
                val rich = activity.findViewById<CheckBox>(R.id.battlerich)
                val start = activity.findViewById<Button>(R.id.battlestart)
                val stname = activity.findViewById<TextView>(R.id.battlestgname)
                line.updateLineUp()
                setname.text = setLUName
                val mc = MapColc.MAPS[mapcode] ?: return
                if (stid >= mc.maps.size) return
                val stm = mc.maps[stid] ?: return
                if (posit >= stm.list.size) return
                val st = stm.list[posit]
                stname.text = MultiLangCont.STNAME.getCont(st)
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
                        activity.startActivityForResult(intent, 0)
                    }
                })
                sniper.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        item += 2
                    } else {
                        item -= 2
                    }
                    println(item)
                }
                rich.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        item += 1
                    } else {
                        item -= 1
                    }
                    println(item)
                }
                start.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(activity, BattleSimulation::class.java)
                        intent.putExtra("mapcode", mapcode)
                        intent.putExtra("stid", stid)
                        intent.putExtra("stage", posit)
                        intent.putExtra("star", star.selectedItemPosition)
                        intent.putExtra("item", item)
                        activity.startActivity(intent)
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
                                    StaticStore.updateForm = true
                                } else {
                                    StaticStore.position = deleted
                                    StaticStore.updateForm = true
                                }
                            }
                            line.drawFloating = false
                            line.touched = false
                        }
                    }
                    true
                }
                val bck: FloatingActionButton = activity.findViewById(R.id.battlebck)
                bck.setOnClickListener { activity.finish() }
            }
            else -> StaticStore.showShortMessage(activity, results[0] ?: R.string.app_name)
        }
    }

    public override fun onPostExecute(result: Void?) {
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
        val prog = activity.findViewById<ProgressBar>(R.id.prepareprog)
        val t = activity.findViewById<TextView>(R.id.preparet)
        setAppear(line, setname, star, equip, sniper, rich, start, layout, stname)
        setDisappear(prog, t)
        val v = activity.findViewById<View>(R.id.view)
        v?.let { setAppear(it) }
    }

    private val setLUName: String
        get() = BasisSet.current.name + " - " + BasisSet.current.sele.name

    private fun setDisappear(vararg views: View) {
        for (v in views) v.visibility = View.GONE
    }

    private fun setAppear(vararg views: View) {
        for (v in views) v.visibility = View.VISIBLE
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

    private fun withID(id: Int, name: String): String {
        return if (name == "") {
            number(id)
        } else {
            number(id) + " - " + name
        }
    }
}