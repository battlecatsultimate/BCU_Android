package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.lineup.adapters.LUCastleSetting
import com.mandarin.bcu.androidutil.lineup.adapters.LUCatCombo
import com.mandarin.bcu.androidutil.lineup.adapters.LUConstruction
import com.mandarin.bcu.androidutil.lineup.adapters.LUFoundationDecoration
import com.mandarin.bcu.androidutil.lineup.adapters.LUOrbSetting
import com.mandarin.bcu.androidutil.lineup.adapters.LUTreasureSetting
import com.mandarin.bcu.androidutil.lineup.adapters.LUUnitList
import com.mandarin.bcu.androidutil.lineup.adapters.LUUnitSetting
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.battle.BasisSet
import common.pack.UserProfile
import common.util.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class LineUpScreen : AppCompatActivity() {
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val line = findViewById<LineUpView>(R.id.lineupView)

        line.updateUnitList()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.clear()

        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
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

        setContentView(R.layout.activity_line_up_screen)

        val result = intent
        val extra = result.extras

        val stage = if (extra != null) {
            if (extra.containsKey("stage")) {
                val data = StaticStore.transformIdentifier<Stage>(extra.getString("stage"))

                data?.get()
            } else {
                null
            }
        } else {
            null
        }

        val star = extra?.getInt("star", 0) ?: 0

        lifecycleScope.launch {
            //Prepare
            StaticStore.LULoading = true

            val tabLayout = findViewById<TabLayout>(R.id.lineuptab)
            val lineupPager = findViewById<ViewPager2>(R.id.lineuppager)
            val row = findViewById<TableRow>(R.id.lineupsetrow)
            val searchBar = findViewById<TextInputEditText>(R.id.animschname)
            val searchLayout = findViewById<TextInputLayout>(R.id.animschnamel)
            val layout = findViewById<LinearLayout>(R.id.lineuplayout)
            val view = findViewById<View>(R.id.view)
            val st = findViewById<TextView>(R.id.status)
            val progression = findViewById<ProgressBar>(R.id.prog)
            val bck = findViewById<FloatingActionButton>(R.id.lineupbck)
            val sch = findViewById<FloatingActionButton>(R.id.linesch)
            val option = findViewById<FloatingActionButton>(R.id.lineupsetting)

            var setIndex = max(0, shared.getInt("equip_set", 0))
            var lineupIndex = max(0, shared.getInt("equip_lu", 0))

            val popupMenu = PopupMenu(this@LineUpScreen, option)
            val menu = popupMenu.menu

            val ids = intArrayOf(R.string.lineup_list, R.string.lineup_unit, R.string.lineup_orb, R.string.lineup_castle, R.string.lineup_treasure, R.string.lineup_construction, R.string.lineup_base, R.string.lineup_decoration, R.string.lineup_combo)
            val names = Array(ids.size) {
                getString(ids[it])
            }

            if (view == null)
                StaticStore.setDisappear(tabLayout, lineupPager, row, searchBar, searchLayout)
            else
                StaticStore.setDisappear(tabLayout, lineupPager, row, view, searchBar, searchLayout)

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@LineUpScreen, { _ -> }, { t -> runOnUiThread { st.text = t }})

                if (StaticStore.ludata.isEmpty()) {
                    StaticStore.ludata.clear()

                    for(p in UserProfile.getAllPacks()) {
                        for(i in p.units.list.indices) {
                            val unit = p.units.list[i]

                            StaticStore.ludata.add(unit.id)
                        }
                    }
                }
            }

            st.setText(R.string.lineup_reading)

            withContext(Dispatchers.IO) {
                if (!StaticStore.LUread) {
                    val path = StaticStore.getExternalUser(this@LineUpScreen)+"/basis.json"
                    val f = File(path)

                    if (f.exists()) {
                        if (f.length() != 0L) {
                            try {
                                BasisSet.read()
                            } catch (e: Exception) {
                                runOnUiThread {
                                    StaticStore.showShortMessage(this@LineUpScreen, R.string.lineup_file_err)
                                }

                                val def = BasisSet.list()[0]

                                BasisSet.list().clear()
                                BasisSet.list().add(def)

                                ErrorLogWriter.writeLog(e, StaticStore.upload, this@LineUpScreen)
                            }
                        }
                    }
                    StaticStore.LUread = true
                }
            }

            val line = LineUpView(this@LineUpScreen, lineupPager)

            if (stage != null) {
                line.attachStageLimit(stage, star)
            }

            line.id = R.id.lineupView

            val w = StaticStore.getScreenWidth(this@LineUpScreen, false)
            val h = w / 5.0f * 3

            line.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h.toInt())

            layout.addView(line)

            StaticStore.setDisappear(line)
            
            //Load UI
            progression.isIndeterminate = true

            popupMenu.menuInflater.inflate(R.menu.lineup_menu, menu)

            StaticStore.setDisappear(progression, st)

            bck.setOnClickListener {
                StaticStore.saveLineUp(this@LineUpScreen, true)
                StaticStore.filterReset()
                StaticStore.entityname = ""

                StaticStore.set = null
                StaticStore.lu = null
                StaticStore.combos.clear()

                finish()
            }

            sch.setOnClickListener {
                val intent = Intent(this@LineUpScreen, SearchFilter::class.java)

                resultLauncher.launch(intent)
            }

            val tab = LUTab(line, stage, star)

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
                            if (deleted[0] == LineUpView.REMOVE) {
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

            val setMenu = findViewById<Spinner>(R.id.setspin)
            val lineupMenu = findViewById<Spinner>(R.id.luspin)

            if (setIndex >= BasisSet.list().size)
                setIndex = max(0, BasisSet.list().size - 1)

            BasisSet.setCurrent(BasisSet.list()[setIndex])

            val setNameData: MutableList<String> = ArrayList()

            for (i in BasisSet.list().indices)
                setNameData.add(BasisSet.list()[i].name)

            var initialized = false

            val adapter = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, setNameData)

            setMenu.adapter = adapter

            setMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (!initialized) {
                        initialized = true
                        return
                    }

                    BasisSet.setCurrent(BasisSet.list()[position])

                    val preferences = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE)
                    val editor = preferences.edit()

                    editor.putInt("equip_set", position)
                    editor.apply()

                    val lineupNameData: MutableList<String> = ArrayList()

                    for (i in BasisSet.current().lb.indices) {
                        lineupNameData.add(BasisSet.current().lb[i].name)
                    }

                    val adapter1 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, lineupNameData)

                    lineupMenu.adapter = adapter1
                    line.updateUnitSetting()
                    line.updateTreasureSetting()
                    line.updateConstructionSetting()
                    line.updateCastleSetting()
                    line.updateUnitOrb()
                    line.updateFoundationSetting()
                    line.updateDecorationSetting()

                    if (position == 0) {
                        menu.getItem(5).subMenu?.getItem(0)?.isEnabled = false
                        menu.getItem(3).subMenu?.getItem(0)?.isEnabled = false
                    } else {
                        menu.getItem(5).subMenu?.getItem(0)?.isEnabled = true
                        menu.getItem(3).subMenu?.getItem(0)?.isEnabled = true
                    }

                    menu.getItem(5).isEnabled = !(!(menu.getItem(5).subMenu?.getItem(0)?.isEnabled ?: false) && !(menu.getItem(5).subMenu?.getItem(1)?.isEnabled ?: false))

                    line.invalidate()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            val lineupNameData: MutableList<String> = ArrayList()

            if (lineupIndex >= BasisSet.current().lb.size)
                lineupIndex = BasisSet.current().lb.size - 1

            BasisSet.current().sele = BasisSet.current().lb[lineupIndex]

            for (i in BasisSet.current().lb.indices) {
                lineupNameData.add(BasisSet.current().lb[i].name)
            }

            val adapter1 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, lineupNameData)

            lineupMenu.adapter = adapter1
            lineupMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (!initialized)
                        return

                    if (BasisSet.current().lb.isEmpty())
                        return

                    BasisSet.current().sele = BasisSet.current().lb[max(0, min(position, BasisSet.current().lb.size - 1))]

                    val preferences = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE)
                    val editor = preferences.edit()

                    editor.putInt("equip_lu", position)
                    editor.apply()

                    line.changeForms(BasisSet.current().sele.lu)

                    line.updateUnitSetting()
                    line.updateUnitOrb()

                    menu.getItem(5).subMenu?.getItem(1)?.isEnabled = BasisSet.current().lb.size != 1
                    menu.getItem(5).isEnabled = !(!(menu.getItem(5).subMenu?.getItem(0)?.isEnabled ?: false) && !(menu.getItem(5).subMenu?.getItem(1)?.isEnabled ?: false))

                    line.invalidate()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            if (StaticStore.set == null && StaticStore.lu == null) {
                menu.getItem(2).isEnabled = false
                menu.getItem(2).subMenu?.getItem(0)?.isEnabled = false
                menu.getItem(2).subMenu?.getItem(1)?.isEnabled = false
            }

            searchBar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    StaticStore.entityname = s.toString()
                    line.updateUnitList()
                }
            })

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.lineup_create_set -> {
                        val dialog = Dialog(this@LineUpScreen)

                        dialog.setContentView(R.layout.create_setlu_dialog)

                        val edit = dialog.findViewById<EditText>(R.id.setluedit)
                        val done = dialog.findViewById<Button>(R.id.setludone)
                        val cancel = dialog.findViewById<Button>(R.id.setlucancel)

                        edit.hint = "set " + BasisSet.list().size

                        val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(this@LineUpScreen, R.attr.TextPrimary))

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))

                        done.setOnClickListener {
                            if (edit.text.toString().isEmpty())
                                BasisSet()
                            else {
                                BasisSet()

                                BasisSet.list()[BasisSet.list().size - 1].name = edit.text.toString()
                            }

                            val setNames: MutableList<String> = ArrayList()

                            var i = 0

                            while (i < BasisSet.list().size) {
                                setNames.add(BasisSet.list()[i].name)
                                i++
                            }

                            val adapter2 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, setNames)

                            setMenu.adapter = adapter2
                            setMenu.setSelection(setMenu.count - 1)

                            line.updateUnitSetting()
                            line.updateTreasureSetting()
                            line.updateConstructionSetting()
                            line.updateCastleSetting()
                            line.updateUnitOrb()
                            line.updateFoundationSetting()
                            line.updateDecorationSetting()

                            StaticStore.saveLineUp(this@LineUpScreen, true)

                            dialog.dismiss()
                        }

                        cancel.setOnClickListener { dialog.dismiss() }

                        if (!isDestroyed && !isFinishing) {
                            dialog.show()
                        }

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_create_lineup -> {
                        val dialog = Dialog(this@LineUpScreen)

                        dialog.setContentView(R.layout.create_setlu_dialog)

                        val edit = dialog.findViewById<EditText>(R.id.setluedit)
                        val done = dialog.findViewById<Button>(R.id.setludone)
                        val cancel = dialog.findViewById<Button>(R.id.setlucancel)

                        edit.hint = "lineup " + BasisSet.current().lb.size

                        val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(this@LineUpScreen, R.attr.TextPrimary))

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))

                        done.setOnClickListener {
                            if (edit.text.toString().isEmpty())
                                BasisSet.current().add()
                            else {
                                BasisSet.current().add()
                                BasisSet.current().lb[BasisSet.current().lb.size - 1].name = edit.text.toString()
                            }

                            val lineupNames: MutableList<String> = ArrayList()

                            var i = 0

                            while (i < BasisSet.current().lb.size) {
                                lineupNames.add(BasisSet.current().lb[i].name)
                                i++
                            }

                            val adapter2 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, lineupNames)

                            lineupMenu.adapter = adapter2
                            lineupMenu.setSelection(lineupMenu.count - 1)

                            line.updateUnitSetting()
                            line.updateUnitOrb()
                            StaticStore.saveLineUp(this@LineUpScreen, true)

                            dialog.dismiss()
                        }

                        cancel.setOnClickListener { dialog.dismiss() }

                        if (!isDestroyed && !isFinishing) {
                            dialog.show()
                        }

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_copy_set -> {
                        StaticStore.set = BasisSet.current().copy()
                        BasisSet.setCurrent(BasisSet.list()[setMenu.selectedItemPosition])

                        for(i in BasisSet.current().lb.indices) {
                            BasisSet.current().lb[i].name = BasisSet.current().lb[i].name
                        }

                        BasisSet.list().removeAt(BasisSet.list().size - 1)

                        StaticStore.showShortMessage(this@LineUpScreen, R.string.lineup_set_copied)

                        menu.getItem(2).isEnabled = true
                        menu.getItem(2).subMenu?.getItem(0)?.isEnabled = true

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_copy_lineup -> {
                        StaticStore.lu = BasisSet.current().sele.copy()
                        StaticStore.showShortMessage(this@LineUpScreen, R.string.lineup_lu_copied)
                        menu.getItem(2).isEnabled = true
                        menu.getItem(2).subMenu?.getItem(1)?.isEnabled = true
                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_paste_set -> {
                        val builder = AlertDialog.Builder(this@LineUpScreen)

                        val s = StaticStore.set ?: return@setOnMenuItemClickListener true

                        builder.setTitle(R.string.lineup_pasting_set)
                        builder.setMessage(R.string.lineup_paste_set_msg)
                        builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                            val name = BasisSet.current().name

                            if(setMenu.selectedItemPosition != 0) {
                                BasisSet.list().removeAt(setMenu.selectedItemPosition)
                                BasisSet.list().add(setMenu.selectedItemPosition, s.copy())
                                BasisSet.list().removeAt(BasisSet.list().size - 1)
                                BasisSet.setCurrent(BasisSet.list()[setMenu.selectedItemPosition])
                                BasisSet.current().name = name
                            } else {
                                BasisSet.def().lb.clear()

                                for(i in s.lb.indices) {
                                    val lb = s.lb[i].copy()
                                    lb.name = s.lb[i].name

                                    BasisSet.def().lb.add(lb)
                                }
                            }

                            line.updateLineUp()
                            line.invalidate()

                            val lineupNames: MutableList<String> = ArrayList()

                            var i = 0

                            while (i < BasisSet.current().lb.size) {
                                lineupNames.add(BasisSet.current().lb[i].name)
                                i++
                            }
                            val adapter11 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, lineupNames)
                            lineupMenu.adapter = adapter11
                            StaticStore.showShortMessage(this@LineUpScreen, R.string.lineup_paste_set_done)
                            StaticStore.saveLineUp(this@LineUpScreen, true)
                        }
                        builder.setNegativeButton(R.string.main_file_cancel) { _: DialogInterface?, _: Int -> }
                        builder.show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_paste_lineup -> {
                        val builder = AlertDialog.Builder(this@LineUpScreen)

                        val l = StaticStore.lu ?: return@setOnMenuItemClickListener true

                        builder.setTitle(R.string.lineup_pasting_lu)
                        builder.setMessage(R.string.lineup_paste_lu_msg)
                        builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                            val name = BasisSet.current().sele.name
                            BasisSet.current().lb.removeAt(lineupMenu.selectedItemPosition)
                            BasisSet.current().lb.add(lineupMenu.selectedItemPosition, l.copy())
                            BasisSet.current().sele = BasisSet.current().lb[lineupMenu.selectedItemPosition]
                            BasisSet.current().sele.name = name
                            line.updateLineUp()
                            line.invalidate()
                            StaticStore.showShortMessage(this@LineUpScreen, R.string.lineup_paste_lu_done)
                            StaticStore.saveLineUp(this@LineUpScreen, true)
                        }
                        builder.setNegativeButton(R.string.main_file_cancel) { _: DialogInterface?, _: Int -> }
                        builder.show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_rename_set -> {
                        val dialog = Dialog(this@LineUpScreen)

                        dialog.setContentView(R.layout.create_setlu_dialog)

                        val edit = dialog.findViewById<EditText>(R.id.setluedit)
                        val done = dialog.findViewById<Button>(R.id.setludone)
                        val cancel = dialog.findViewById<Button>(R.id.setlucancel)
                        val lineupNameTextBar = dialog.findViewById<TextView>(R.id.setluname)

                        lineupNameTextBar.setText(R.string.lineup_renaming_set)

                        edit.hint = BasisSet.current().name

                        val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(this@LineUpScreen, R.attr.TextPrimary))

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))

                        done.setOnClickListener {
                            if (edit.text.toString().isNotEmpty()) {
                                BasisSet.current().name = edit.text.toString()

                                val setNames: MutableList<String> = ArrayList()
                                var i = 0

                                while (i < BasisSet.list().size) {
                                    setNames.add(BasisSet.list()[i].name)
                                    i++
                                }

                                val adapter22 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, setNames)
                                val pos = setMenu.selectedItemPosition

                                setMenu.adapter = adapter22

                                setMenu.setSelection(pos)

                                StaticStore.saveLineUp(this@LineUpScreen, true)
                            }

                            dialog.dismiss()
                        }

                        cancel.setOnClickListener { dialog.dismiss() }

                        if (!isDestroyed && !isFinishing) {
                            dialog.show()
                        }

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_rename_lineup -> {
                        val dialog = Dialog(this@LineUpScreen)

                        dialog.setContentView(R.layout.create_setlu_dialog)

                        val edit = dialog.findViewById<EditText>(R.id.setluedit)
                        val done = dialog.findViewById<Button>(R.id.setludone)
                        val cancel = dialog.findViewById<Button>(R.id.setlucancel)
                        val lineupNameTextBar = dialog.findViewById<TextView>(R.id.setluname)

                        lineupNameTextBar.setText(R.string.lineup_renaming_lu)

                        edit.hint = BasisSet.current().sele.name

                        val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(this@LineUpScreen, R.attr.TextPrimary))

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))

                        done.setOnClickListener {
                            if (edit.text.toString().isNotEmpty()) {
                                BasisSet.current().sele.name = edit.text.toString()

                                val lineupNames: MutableList<String> = ArrayList()
                                var i = 0

                                while (i < BasisSet.current().lb.size) {
                                    lineupNames.add(BasisSet.current().lb[i].name)
                                    i++
                                }

                                val adapter11 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, lineupNames)

                                lineupMenu.adapter = adapter11
                                lineupMenu.setSelection(BasisSet.current().lb.size - 1)

                                StaticStore.saveLineUp(this@LineUpScreen, true)
                            }

                            dialog.dismiss()
                        }

                        cancel.setOnClickListener { dialog.dismiss() }

                        if (!isDestroyed && !isFinishing) {
                            dialog.show()
                        }

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_clone_set -> {
                        val origin = BasisSet.current().name

                        BasisSet.current().copy()
                        BasisSet.list()[BasisSet.list().size - 1].name = "$origin`"

                        val setNames: MutableList<String> = ArrayList()

                        var i = 0

                        while (i < BasisSet.list().size) {
                            setNames.add(BasisSet.list()[i].name)
                            i++
                        }
                        val adapter22 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, setNames)

                        setMenu.adapter = adapter22
                        setMenu.setSelection(BasisSet.list().size - 1)

                        StaticStore.saveLineUp(this@LineUpScreen, true)
                        StaticStore.showShortMessage(this@LineUpScreen, R.string.lineup_cloned_set)

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_clone_lineup -> {
                        val lu = BasisSet.current().sele.copy()

                        lu.name = BasisSet.current().sele.name + "`"

                        BasisSet.current().lb.add(lu)

                        val lineupNames: MutableList<String> = ArrayList()

                        var i = 0

                        while (i < BasisSet.current().lb.size) {
                            lineupNames.add(BasisSet.current().lb[i].name)
                            i++
                        }

                        val adapter11 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, lineupNames)

                        lineupMenu.adapter = adapter11
                        lineupMenu.setSelection(BasisSet.current().lb.size - 1)

                        StaticStore.saveLineUp(this@LineUpScreen, true)

                        StaticStore.showShortMessage(this@LineUpScreen, R.string.lineup_cloned_lineup)

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_remove_set -> {
                        val builder = AlertDialog.Builder(this@LineUpScreen)

                        builder.setTitle(R.string.lineup_removing_set)
                        builder.setMessage(R.string.lineup_remove_set_msg)

                        builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                            if (setMenu.selectedItemPosition >= BasisSet.list().size)
                                return@setPositiveButton

                            BasisSet.list().removeAt(setMenu.selectedItemPosition)

                            val pos = setMenu.selectedItemPosition

                            val setNames: MutableList<String> = ArrayList()

                            var i = 0

                            while (i < BasisSet.list().size) {
                                setNames.add(BasisSet.list()[i].name)
                                i++
                            }

                            val adapter23 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, setNames)

                            setMenu.adapter = adapter23

                            if (pos >= BasisSet.list().size)
                                setMenu.setSelection(BasisSet.list().size - 1)
                            else
                                setMenu.setSelection(pos)

                            try {
                                StaticStore.saveLineUp(this@LineUpScreen, true)
                            } catch(e: Exception) {
                                ErrorLogWriter.writeLog(e, StaticStore.upload, this@LineUpScreen)
                                StaticStore.showShortMessage(this@LineUpScreen, R.string.err_lusave_fail)
                            }
                        }

                        builder.setNegativeButton(R.string.main_file_cancel) { _: DialogInterface?, _: Int -> }
                        builder.show()

                        return@setOnMenuItemClickListener true
                    }
                    R.id.lineup_remove_lineup -> {
                        val builder = AlertDialog.Builder(this@LineUpScreen)
                        builder.setTitle(R.string.lineup_removing_lu)
                        builder.setMessage(R.string.lineup_remove_lu_msg)
                        builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                            BasisSet.current().lb.removeAt(lineupMenu.selectedItemPosition)
                            val pos = lineupMenu.selectedItemPosition
                            val lineupNames: MutableList<String> = ArrayList()
                            var i = 0
                            while (i < BasisSet.current().lb.size) {
                                lineupNames.add(BasisSet.current().lb[i].name)
                                i++
                            }
                            val adapter12 = ArrayAdapter(this@LineUpScreen, R.layout.spinneradapter, lineupNames)
                            lineupMenu.adapter = adapter12
                            if (pos >= BasisSet.current().lb.size) lineupMenu.setSelection(BasisSet.current().lb.size - 1) else lineupMenu.setSelection(pos)

                            try {
                                StaticStore.saveLineUp(this@LineUpScreen, true)
                            } catch(e: Exception) {
                                ErrorLogWriter.writeLog(e, StaticStore.upload, this@LineUpScreen)
                                StaticStore.showShortMessage(this@LineUpScreen, R.string.err_lusave_fail)
                            }
                        }
                        builder.setNegativeButton(R.string.main_file_cancel) { _: DialogInterface?, _: Int -> }
                        builder.show()
                        return@setOnMenuItemClickListener true
                    }
                }
                false
            }

            option.setOnClickListener { popupMenu.show() }

            lineupPager.isSaveEnabled = false
            lineupPager.isSaveFromParentEnabled = false

            lineupPager.adapter = tab
            lineupPager.offscreenPageLimit = 1

            TabLayoutMediator(tabLayout, lineupPager) { t, position ->
                t.text = names[position]
            }.attach()

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    lineupPager.currentItem = tab.position
                    StaticStore.LUtabPosition = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            val currentTab = tabLayout.getTabAt(StaticStore.LUtabPosition)

            if (currentTab != null) {
                tabLayout.post {
                    currentTab.select()
                    lineupPager.setCurrentItem(currentTab.position, false)
                }
            }

            setMenu.setSelection(setIndex)
            lineupMenu.setSelection(lineupIndex)

            tab.reassignVariables(line)

            onBackPressedDispatcher.addCallback(this@LineUpScreen, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        StaticStore.saveLineUp(this@LineUpScreen, true)
                    } catch(e: Exception) {
                        ErrorLogWriter.writeLog(e, StaticStore.upload, this@LineUpScreen)
                        StaticStore.showShortMessage(this@LineUpScreen, R.string.err_lusave_fail)
                    }

                    StaticStore.filterReset()
                    StaticStore.entityname = ""
                    StaticStore.set = null
                    StaticStore.lu = null

                    StaticStore.combos.clear()

                    finish()
                }
            })

            if (view == null)
                StaticStore.setAppear(tabLayout, lineupPager, line, row, searchBar, searchLayout)
            else
                StaticStore.setAppear(tabLayout, lineupPager, line, row, view, searchBar, searchLayout)
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

    inner class LUTab(private val lineup: LineUpView, private val stage: Stage?, private val star: Int) : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        fun updateFragment(i: Int) {
            if(i >= 8) {
                Log.w("LUAdder::LUTab", "Fragment updating index must not exceed 5!")
                return
            }

            val frag = supportFragmentManager.findFragmentByTag("f$i")

            if(frag == null) {
                Log.e("LUAdder:LUTab", "Failed to get fragment : $i")
            } else {
                when(i) {
                    0 -> (frag as LUUnitList).update()
                    1 -> (frag as LUUnitSetting).update()
                    2 -> (frag as LUOrbSetting).update()
                    3 -> (frag as LUCastleSetting).update()
                    4 -> (frag as LUTreasureSetting).update()
                    5 -> (frag as LUConstruction).update()
                    6 -> (frag as LUFoundationDecoration).update()
                    7 -> (frag as LUFoundationDecoration).update()
                }
            }
        }

        fun syncFragment(i: Int) {
            if(i >= 8) {
                Log.w("LUAdder::LUTab", "Fragment updating index must not exceed 5!")
                return
            }

            val frag = supportFragmentManager.findFragmentByTag("f$i")

            if(frag == null) {
                Log.e("LUAdder:LUTab", "Failed to get fragment : $i")
            } else {
                when(i) {
                    0 -> (frag as LUUnitList).sync()
                    1 -> (frag as LUUnitSetting).update()
                    2 -> (frag as LUOrbSetting).update()
                    3 -> (frag as LUCastleSetting).update()
                    4 -> (frag as LUTreasureSetting).update()
                    5 -> (frag as LUConstruction).update()
                    6 -> (frag as LUFoundationDecoration).update()
                    7 -> (frag as LUFoundationDecoration).update()
                }
            }
        }

        fun reassignVariables(line: LineUpView) {
            val required = intArrayOf(0, 1, 2, 4, 8)

            for(i in required) {
                val frag = supportFragmentManager.findFragmentByTag("f$i")

                if(frag == null) {
                    Log.e("LUAdder:LUTab", "Failed to get fragment : $i")
                } else {
                    when(i) {
                        0 -> (frag as LUUnitList).setArguments(line)
                        1 -> (frag as LUUnitSetting).setVariable(line)
                        2 -> (frag as LUOrbSetting).setLineup(line)
                        4 -> (frag as LUTreasureSetting).setVariable(line)
                        else -> (frag as LUCatCombo).setVariables(line)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return 9
        }

        override fun createFragment(i: Int): Fragment {
            return when(i) {
                0 -> LUUnitList.newInstance(lineup, stage = stage, star = star)
                1 -> LUUnitSetting.newInstance(lineup)
                2 -> LUOrbSetting.newInstance(lineup)
                3 -> LUCastleSetting.newInstance()
                4 -> LUTreasureSetting.newInstance(lineup)
                5 -> LUConstruction.newInstance()
                6 -> LUFoundationDecoration.newInstances(true)
                7 -> LUFoundationDecoration.newInstances(false)
                else -> LUCatCombo.newInstance(lineup)
            }
        }
    }
}