package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.battle.BasisSet
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max

class BattlePrepare : AppCompatActivity() {
    companion object {
        var rich = false
        var sniper = false
    }

    private var initialized = false

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                while (true) {
                    if (initialized)
                        break
                }

                while(true) {
                    val line = try {
                        findViewById<LineUpView>(R.id.lineupView) ?: continue
                    } catch (_: NullPointerException) {
                        continue
                    }

                    line.updateLineUp()

                    break
                }
            }

            val setName = findViewById<TextView>(R.id.lineupname)

            setName.text = setLUName
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed = shared.edit()

        if (!shared.contains("initial")) {
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_battle_prepare)

        val result = intent.extras

        if (result != null) {
            val data = StaticStore.transformIdentifier<Stage>(result.getString("Data")) ?: return
            val selection = result.getInt("selection", 0)
            var item = 0

            lifecycleScope.launch {
                //Prepare
                val setname = findViewById<TextView>(R.id.lineupname)
                val star = findViewById<Spinner>(R.id.battlestar)
                val equip = findViewById<Button>(R.id.battleequip)
                val sniper = findViewById<CheckBox>(R.id.battlesniper)
                val rich = findViewById<CheckBox>(R.id.battlerich)
                val start = findViewById<Button>(R.id.battlestart)
                val layout = findViewById<LinearLayout>(R.id.preparelineup)
                val stname = findViewById<TextView>(R.id.battlestgname)
                val v = findViewById<View>(R.id.view)
                val lvlim = findViewById<Spinner>(R.id.battlelvlim)
                val plus = findViewById<CheckBox>(R.id.battleplus)
                val st = findViewById<TextView>(R.id.status)
                val prog = findViewById<ProgressBar>(R.id.prog)

                StaticStore.setDisappear(setname, star, equip, sniper, rich, start, layout, stname, lvlim, plus, v)
                
                //Load Data
                withContext(Dispatchers.IO) {
                    Definer.define(this@BattlePrepare, { _ -> }, { t -> runOnUiThread { st.text = t }})
                }
                
                st.setText(R.string.lineup_reading)

                withContext(Dispatchers.IO) {
                    if (!StaticStore.LUread) {
                        try {
                            BasisSet.read()
                        } catch (e: Exception) {
                            StaticStore.showShortMessage(this@BattlePrepare, R.string.lineup_file_err)
                            BasisSet.list().clear()
                            BasisSet()
                            ErrorLogWriter.writeLog(e, StaticStore.upload, this@BattlePrepare)
                        }

                        StaticStore.LUread = true
                    }

                    var set = shared.getInt("equip_set", 0)
                    var lu = shared.getInt("equip_lu", 0)

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
                }
                
                //Load UI
                val line = LineUpView(this@BattlePrepare)

                line.id = R.id.lineupView

                val w: Float = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    StaticStore.getScreenWidth(this@BattlePrepare, false).toFloat() / 2.0f
                else
                    StaticStore.getScreenWidth(this@BattlePrepare, false).toFloat()

                val h = w / 5.0f * 3

                line.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h.toInt())

                layout.addView(line)

                prog.isIndeterminate = true

                setname.text = setLUName

                val stage = Identifier.get(data) ?: return@launch
                val stm = stage.cont ?: return@launch

                stname.text = MultiLangCont.get(stage) ?: stage.names.toString()

                if(stname.text.isBlank())
                    stname.text = getStageName(data.id)

                val stars = ArrayList<String>()
                var i = 0
                while (i < stm.stars.size) {
                    val s = (i + 1).toString() + " (" + stm.stars[i] + " %)"
                    stars.add(s)
                    i++
                }
                val arrayAdapter = ArrayAdapter(this@BattlePrepare, R.layout.spinneradapter, stars)
                star.adapter = arrayAdapter
                if (selection < stars.size && selection >= 0) star.setSelection(selection)
                equip.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(this@BattlePrepare, LineUpScreen::class.java)

                        resultLauncher.launch(intent)
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
                        val intent = Intent(this@BattlePrepare, BattleSimulation::class.java)

                        intent.putExtra("Data", JsonEncoder.encode(data).toString())
                        intent.putExtra("star", star.selectedItemPosition)
                        intent.putExtra("item", item)

                        startActivity(intent)
                        BattlePrepare.rich = false
                        BattlePrepare.sniper = false
                        finish()
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

                            line.invalidate()
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

                val bck: FloatingActionButton = findViewById(R.id.battlebck)
                bck.setOnClickListener {
                    BattlePrepare.rich = false
                    BattlePrepare.sniper = false
                    finish()
                }

                val lvlimText = ArrayList<String>()

                for(n in 0..50) {
                    if(n == 0) {
                        lvlimText.add(getString(R.string.battle_lvlimoff))
                    } else {
                        lvlimText.add(n.toString())
                    }
                }

                if(stage.isAkuStage) {
                    val lvLimAdapter = object : ArrayAdapter<String>(this@BattlePrepare, R.layout.spinneradapter, lvlimText) {

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val dropDownView = super.getDropDownView(position, convertView, parent)

                            dropDownView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                            lvlim.dropDownWidth = max(lvlim.dropDownWidth, dropDownView.measuredWidth)

                            return dropDownView
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

                StaticStore.setAppear(line, setname, star, equip, sniper, rich, start, layout, stname, v)

                if(stage.isAkuStage) {
                    StaticStore.setAppear(lvlim, plus)
                }

                StaticStore.setDisappear(prog, st)

                initialized = true
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    private val setLUName: String
        get() = BasisSet.current().name + " - " + BasisSet.current().sele.name

    override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
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
}