package com.mandarin.bcu.androidutil.lineup.asynchs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.SearchFilter
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager
import com.mandarin.bcu.androidutil.fakeandroid.FIBM
import com.mandarin.bcu.androidutil.io.AImageWriter
import com.mandarin.bcu.androidutil.io.DefferedLoader
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.lineup.adapters.*
import com.mandarin.bcu.androidutil.lineup.adapters.LUCatCombo.Companion.newInstance
import com.mandarin.bcu.androidutil.unit.Definer
import common.battle.BasisSet
import common.io.InStream
import common.system.MultiLangCont
import common.system.fake.FakeImage
import common.util.Data
import common.util.pack.Pack
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.security.NoSuchAlgorithmException
import java.util.*

class LUAdder(activity: Activity, private val manager: FragmentManager) : AsyncTask<Void?, String?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private var prePosit = 0
    private var initialized = false
    private var tab: LUTab? = null
    private val ids = intArrayOf(R.string.lineup_list, R.string.lineup_unit, R.string.lineup_castle, R.string.lineup_treasure, R.string.lineup_construction, R.string.lineup_combo)
    private val names = arrayOfNulls<String>(ids.size)
    
    private val lu = "0"
    private val done = "1"
    private val pack = "2"
    private val image = "3"
    private val castle = "4"
    private val bg = "5"
    private val packext = "6"
    
    override fun onPreExecute() {
        StaticStore.LULoading = true
        val activity = weakReference.get() ?: return
        val tabLayout: TabLayout = activity.findViewById(R.id.lineuptab)
        val measureViewPager: MeasureViewPager = activity.findViewById(R.id.lineuppager)
        val line: LineUpView = activity.findViewById(R.id.lineupView)
        val row = activity.findViewById<TableRow>(R.id.lineupsetrow)
        val schname: TextInputEditText = activity.findViewById(R.id.animschname)
        val layout: TextInputLayout = activity.findViewById(R.id.animschnamel)
        val view = activity.findViewById<View>(R.id.view)
        for (i in ids.indices) names[i] = activity.getString(ids[i])
        if (view == null) setDisappear(tabLayout, measureViewPager, line, row, schname, layout) else setDisappear(tabLayout, measureViewPager, line, row, view, schname, layout)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)

        publishProgress(pack)

        checkValidPack()
        handlePack()
        removeIfDifferent()

        try {
            if(!StaticStore.packread && Pack.map.size == 1) {
                Pack.read()
                StaticStore.packread = true
            }
            DefferedLoader.clearPending("Context", activity)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
        }

        for(path in DefineItf.packPath) {
            val f = File(path)

            val fname = f.name

            if (fname.endsWith(".bcupack")) {
                if (!checkPack(f)) {
                    val p = findPack(f)

                    val name = f.name.replace(".bcupack", "")

                    val shared = activity.getSharedPreferences(name, Context.MODE_PRIVATE)
                    val ed = shared.edit()

                    ed.putString(name, StaticStore.fileToMD5(f))
                    ed.apply()

                    val resimg = StaticStore.getExternalRes(activity) + "img/$name/"

                    val g = File(resimg)

                    val glit = g.listFiles() ?: continue

                    for (gs in glit) {
                        val pngname = gs.name

                        publishProgress(image, pngname.replace(".png", ""))

                        if (pngname.endsWith(".png")) {
                            val md5 = StaticStore.fileToMD5(gs)

                            StaticStore.encryptPNG(gs.absolutePath, md5, StaticStore.IV, true)

                            ed.putString(gs.absolutePath.replace(".png", ".bcuimg"), md5)
                            ed.apply()
                        }
                    }

                    p ?: continue

                    val bpathList = ArrayList<String>()

                    for (i in p.bg.list) {
                        val img = i.img?.bimg ?: continue

                        val bpath = StaticStore.getExternalRes(activity) + "img/$name/"
                        val bname = findBgName(bpath)

                        val info = extractImage(activity, img, bpath, bname, false)

                        if (info.size != 2)
                            continue

                        val result = info[0] + "\\" + info[1]

                        (i.img.bimg as FIBM).reference = result

                        bpathList.add(result)
                    }

                    for (i in bpathList) {
                        val info = i.split("\\")

                        val bf = File(info[0].replace(".bcuimg", ".png"))

                        if (!bf.exists())
                            continue

                        publishProgress(bg, bf.name.replace(".png", ""))

                        if (info.size != 2)
                            continue

                        StaticStore.encryptPNG(info[0].replace(".bcuimg", ".png"), info[1], StaticStore.IV, true)
                    }

                    val cpathList = ArrayList<String>()

                    for (i in p.cs.list) {
                        val img = i.img ?: continue

                        val cpath = StaticStore.getExternalRes(activity) + "img/$name/"
                        val cname = findCsName(cpath)

                        val info = extractImage(activity, img, cpath, cname, false)

                        if (info.size != 2)
                            continue

                        val result = info[0] + "\\" + info[1]

                        (i.img as FIBM).reference = result

                        cpathList.add(result)
                    }

                    for (i in cpathList) {
                        val info = i.split("\\")

                        val cf = File(info[0].replace(".bcuimg", ".png"))

                        if (!cf.exists())
                            continue

                        publishProgress(castle, cf.name.replace(".png", ""))

                        if (info.size != 2)
                            continue

                        StaticStore.encryptPNG(info[0].replace(".bcuimg", ".png"), info[1], StaticStore.IV, true)
                    }

                    publishProgress(packext, f.name.replace(".bcupack", ""))

                    p.packData(AImageWriter())
                }
            }
        }

        DefineItf.packPath.clear()

        StaticStore.filterEntityList = BooleanArray(Pack.map.size)

        if (StaticStore.lunames.isEmpty() || StaticStore.ludata.isEmpty()) {
            StaticStore.lunames.clear()
            StaticStore.ludata.clear()

            for(m in Pack.map) {
                val p = m.value ?: continue

                val pid = p.id

                for(i in p.us.ulist.list.indices) {
                    val unit = p.us.ulist.list[i]

                    val name = MultiLangCont.FNAME.getCont(unit.forms[0]) ?: unit.forms[0].name ?: ""

                    val id = if(p.id != 0) {
                        StaticStore.getID(p.us.ulist.list[i].id)
                    } else {
                        i
                    }

                    val fullName = if(name != "") {
                        Data.hex(pid)+" - "+number(id)+"/"+name
                    } else {
                        Data.hex(pid)+" - "+number(id)+"/"
                    }

                    StaticStore.lunames.add(fullName)
                    StaticStore.ludata.add("$pid-$i")
                }
            }
        }

        publishProgress(lu)
        if (!StaticStore.LUread) {
            val path = StaticStore.getExternalPath(activity)+"user/basis.v"
            val f = File(path)

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
                            publishProgress(activity.getString(R.string.lineup_file_err))
                            val def = BasisSet.list[0]
                            BasisSet.list.clear()
                            BasisSet.list.add(def)
                            ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                        }
                    } catch (e: Exception) {
                        publishProgress(activity.getString(R.string.lineup_file_err))
                        val def = BasisSet.list[0]
                        BasisSet.list.clear()
                        BasisSet.list.add(def)
                        ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                    }
                }
            }
            StaticStore.LUread = true
        }
        StaticStore.sets = BasisSet.list
        publishProgress(done)
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg results: String?) {
        val activity = weakReference.get() ?: return
        val st = activity.findViewById<TextView>(R.id.lineupst)
        when(results[0]) {
            pack -> {
                st.setText(R.string.main_pack)
            }

            image -> {
                val name = activity.getString(R.string.main_pack_img)+ (results[1] ?: "")

                st.text = name
            }

            bg -> {
                val name = activity.getString(R.string.main_pack_bg) + (results[1] ?: "")

                st.text = name
            }

            castle -> {
                val name = activity.getString(R.string.main_pack_castle) + (results[1] ?: "")

                st.text = name
            }

            packext -> {
                val name = activity.getString(R.string.main_pack_ext)+ (results[1] ?: "")

                st.text = name
            }
            done -> {
                val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                var setn = shared.getInt("equip_set", 0)
                var lun = shared.getInt("equip_lu", 0)
                val prog = activity.findViewById<ProgressBar>(R.id.lineupprog)
                val pager: MeasureViewPager = activity.findViewById(R.id.lineuppager)
                val tabs: TabLayout = activity.findViewById(R.id.lineuptab)
                val bck: FloatingActionButton = activity.findViewById(R.id.lineupbck)
                val sch: FloatingActionButton = activity.findViewById(R.id.linesch)
                val line: LineUpView = activity.findViewById(R.id.lineupView)
                val option: FloatingActionButton = activity.findViewById(R.id.lineupsetting)
                val schname: TextInputEditText = activity.findViewById(R.id.animschname)
                val popupMenu = PopupMenu(activity, option)
                val menu = popupMenu.menu

                popupMenu.menuInflater.inflate(R.menu.lineup_menu, menu)

                setDisappear(prog, st)

                bck.setOnClickListener {
                    StaticStore.SaveLineUp(activity)
                    StaticStore.filterReset()

                    StaticStore.set = null
                    StaticStore.lu = null
                    StaticStore.combos.clear()

                    activity.finish()
                }

                sch.setOnClickListener {
                    val intent = Intent(activity, SearchFilter::class.java)

                    activity.startActivity(intent)
                }

                tab = LUTab(manager, line)

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

                val setspin = activity.findViewById<Spinner>(R.id.setspin)
                val luspin = activity.findViewById<Spinner>(R.id.luspin)

                if (setn >= BasisSet.list.size)
                    setn = BasisSet.list.size - 1

                BasisSet.current = BasisSet.list[setn]

                val setname: MutableList<String> = ArrayList()

                for (i in StaticStore.sets.indices)
                    setname.add(StaticStore.sets[i].name)

                val adapter = ArrayAdapter(activity, R.layout.spinneradapter, setname)

                setspin.adapter = adapter

                setspin.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (!initialized) {
                            initialized = true
                            return
                        }

                        BasisSet.current = StaticStore.sets[position]

                        val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                        val editor = preferences.edit()

                        editor.putInt("equip_set", position)
                        editor.apply()

                        val luname: MutableList<String> = ArrayList()

                        for (i in BasisSet.current.lb.indices) {
                            luname.add(BasisSet.current.lb[i].name)
                        }

                        val adapter1 = ArrayAdapter(activity, R.layout.spinneradapter, luname)

                        luspin.adapter = adapter1
                        StaticStore.updateForm = true
                        StaticStore.updateTreasure = true
                        StaticStore.updateConst = true
                        StaticStore.updateCastle = true

                        if (position == 0) {
                            menu.getItem(5).subMenu.getItem(0).isEnabled = false
                            menu.getItem(3).subMenu.getItem(0).isEnabled = false
                        } else {
                            menu.getItem(5).subMenu.getItem(0).isEnabled = true
                            menu.getItem(3).subMenu.getItem(0).isEnabled = true
                        }

                        menu.getItem(5).isEnabled = !(!menu.getItem(5).subMenu.getItem(0).isEnabled && !menu.getItem(5).subMenu.getItem(1).isEnabled)

                        line.invalidate()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                val luname: MutableList<String> = ArrayList()

                if (lun >= BasisSet.current.lb.size)
                    lun = BasisSet.current.lb.size - 1

                BasisSet.current.sele = BasisSet.current.lb[lun]

                for (i in BasisSet.current.lb.indices) {
                    luname.add(BasisSet.current.lb[i].name)
                }

                val adapter1 = ArrayAdapter(activity, R.layout.spinneradapter, luname)

                luspin.adapter = adapter1
                luspin.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (!initialized)
                            return

                        BasisSet.current.sele = BasisSet.current.lb[position]

                        val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                        val editor = preferences.edit()

                        editor.putInt("equip_lu", position)
                        editor.apply()

                        line.changeFroms(BasisSet.current.sele.lu)

                        StaticStore.updateForm = true

                        menu.getItem(5).subMenu.getItem(1).isEnabled = BasisSet.current.lb.size != 1
                        menu.getItem(5).isEnabled = !(!menu.getItem(5).subMenu.getItem(0).isEnabled && !menu.getItem(5).subMenu.getItem(1).isEnabled)

                        line.invalidate()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                if (StaticStore.set == null && StaticStore.lu == null) {
                    menu.getItem(2).isEnabled = false
                    menu.getItem(2).subMenu.getItem(0).isEnabled = false
                    menu.getItem(2).subMenu.getItem(1).isEnabled = false
                }

                schname.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable) {
                        StaticStore.entityname = s.toString()
                        StaticStore.updateList = true
                    }
                })

                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.lineup_create_set -> {
                            val dialog = Dialog(activity)

                            dialog.setContentView(R.layout.create_setlu_dialog)

                            val edit = dialog.findViewById<EditText>(R.id.setluedit)
                            val setdone = dialog.findViewById<Button>(R.id.setludone)
                            val cancel = dialog.findViewById<Button>(R.id.setlucancel)

                            edit.hint = "set " + BasisSet.list.size

                            val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))

                            edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))

                            setdone.setOnClickListener {
                                if (edit.text.toString().isEmpty())
                                    BasisSet()
                                else {
                                    BasisSet()

                                    BasisSet.list[BasisSet.list.size - 1].name = edit.text.toString()
                                }

                                val names: MutableList<String> = ArrayList()

                                var i = 0

                                while (i < BasisSet.list.size) {
                                    names.add(BasisSet.list[i].name)
                                    i++
                                }

                                val adapter2 = ArrayAdapter(activity, R.layout.spinneradapter, names)

                                setspin.adapter = adapter2
                                setspin.setSelection(setspin.count - 1)

                                StaticStore.updateForm = true
                                StaticStore.updateTreasure = true
                                StaticStore.updateConst = true
                                StaticStore.updateCastle = true

                                StaticStore.SaveLineUp(activity)

                                dialog.dismiss()
                            }

                            cancel.setOnClickListener { dialog.dismiss() }

                            dialog.show()

                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_create_lineup -> {
                            val dialog = Dialog(activity)

                            dialog.setContentView(R.layout.create_setlu_dialog)

                            val edit: EditText = dialog.findViewById(R.id.setluedit)
                            val setdone: Button = dialog.findViewById(R.id.setludone)
                            val cancel: Button = dialog.findViewById(R.id.setlucancel)

                            edit.hint = "lineup " + BasisSet.current.lb.size

                            val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))

                            edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))

                            setdone.setOnClickListener {
                                if (edit.text.toString().isEmpty())
                                    BasisSet.current.add()
                                else {
                                    BasisSet.current.add()
                                    BasisSet.current.lb[BasisSet.current.lb.size - 1].name = edit.text.toString()
                                }

                                val names: MutableList<String> = ArrayList()

                                var i = 0

                                while (i < BasisSet.current.lb.size) {
                                    names.add(BasisSet.current.lb[i].name)
                                    i++
                                }

                                val adapter2 = ArrayAdapter(activity, R.layout.spinneradapter, names)

                                luspin.adapter = adapter2
                                luspin.setSelection(luspin.count - 1)

                                StaticStore.updateForm = true
                                StaticStore.SaveLineUp(activity)

                                dialog.dismiss()
                            }

                            cancel.setOnClickListener { dialog.dismiss() }

                            dialog.show()

                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_copy_set -> {
                            StaticStore.set = BasisSet.current.copy()
                            BasisSet.current = BasisSet.list[setspin.selectedItemPosition]

                            for(i in BasisSet.current.lb.indices) {
                                StaticStore.set.lb[i].name = BasisSet.current.lb[i].name
                            }

                            BasisSet.list.removeAt(BasisSet.list.size - 1)

                            StaticStore.showShortMessage(activity, R.string.lineup_set_copied)

                            menu.getItem(2).isEnabled = true
                            menu.getItem(2).subMenu.getItem(0).isEnabled = true

                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_copy_lineup -> {
                            StaticStore.lu = BasisSet.current.sele.copy()
                            StaticStore.showShortMessage(activity, R.string.lineup_lu_copied)
                            menu.getItem(2).isEnabled = true
                            menu.getItem(2).subMenu.getItem(1).isEnabled = true
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_paste_set -> {
                            val builder = AlertDialog.Builder(activity)
                            builder.setTitle(R.string.lineup_pasting_set)
                            builder.setMessage(R.string.lineup_paste_set_msg)
                            builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                                val name = BasisSet.current.name

                                if(setspin.selectedItemPosition != 0) {
                                    BasisSet.list.removeAt(setspin.selectedItemPosition)
                                    BasisSet.list.add(setspin.selectedItemPosition, StaticStore.set.copy())
                                    BasisSet.list.removeAt(BasisSet.list.size - 1)
                                    BasisSet.current = BasisSet.list[setspin.selectedItemPosition]
                                    BasisSet.current.name = name
                                } else {
                                    BasisSet.def.lb.clear()

                                    for(i in StaticStore.set.lb.indices) {
                                        val lb = StaticStore.set.lb[i].copy()
                                        lb.name = StaticStore.set.lb[i].name

                                        BasisSet.def.lb.add(lb)
                                    }
                                }

                                line.updateLineUp()
                                line.invalidate()
                                val luname1: MutableList<String> = ArrayList()
                                var i = 0
                                while (i < BasisSet.current.lb.size) {
                                    luname1.add(BasisSet.current.lb[i].name)
                                    i++
                                }
                                val adapter11 = ArrayAdapter(activity, R.layout.spinneradapter, luname1)
                                luspin.adapter = adapter11
                                StaticStore.showShortMessage(activity, R.string.lineup_paste_set_done)
                                StaticStore.SaveLineUp(activity)
                            }
                            builder.setNegativeButton(R.string.main_file_cancel) { _: DialogInterface?, _: Int -> }
                            builder.show()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_paste_lineup -> {
                            val builder = AlertDialog.Builder(activity)
                            builder.setTitle(R.string.lineup_pasting_lu)
                            builder.setMessage(R.string.lineup_paste_lu_msg)
                            builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                                val name = BasisSet.current.sele.name
                                BasisSet.current.lb.removeAt(luspin.selectedItemPosition)
                                BasisSet.current.lb.add(luspin.selectedItemPosition, StaticStore.lu.copy())
                                BasisSet.current.sele = BasisSet.current.lb[luspin.selectedItemPosition]
                                BasisSet.current.sele.name = name
                                line.updateLineUp()
                                line.invalidate()
                                StaticStore.showShortMessage(activity, R.string.lineup_paste_lu_done)
                                StaticStore.SaveLineUp(activity)
                            }
                            builder.setNegativeButton(R.string.main_file_cancel) { _: DialogInterface?, _: Int -> }
                            builder.show()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_rename_set -> {
                            val dialog = Dialog(activity)
                            dialog.setContentView(R.layout.create_setlu_dialog)
                            val edit: EditText = dialog.findViewById(R.id.setluedit)
                            val setdone: Button = dialog.findViewById(R.id.setludone)
                            val cancel: Button = dialog.findViewById(R.id.setlucancel)
                            val setluname: TextView = dialog.findViewById(R.id.setluname)
                            setluname.setText(R.string.lineup_renaming_set)
                            edit.hint = BasisSet.current.name
                            val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))
                            edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))
                            setdone.setOnClickListener {
                                if (edit.text.toString().isNotEmpty()) {
                                    BasisSet.current.name = edit.text.toString()
                                    val setname1: MutableList<String> = ArrayList()
                                    var i = 0
                                    while (i < StaticStore.sets.size) {
                                        setname1.add(StaticStore.sets[i].name)
                                        i++
                                    }
                                    val adapter22 = ArrayAdapter(activity, R.layout.spinneradapter, setname1)
                                    val pos = setspin.selectedItemPosition
                                    setspin.adapter = adapter22
                                    setspin.setSelection(pos)
                                    StaticStore.SaveLineUp(activity)
                                }
                                dialog.dismiss()
                            }
                            cancel.setOnClickListener { dialog.dismiss() }
                            dialog.show()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_rename_lineup -> {
                            val dialog = Dialog(activity)
                            dialog.setContentView(R.layout.create_setlu_dialog)
                            val edit: EditText = dialog.findViewById(R.id.setluedit)
                            val setdone: Button = dialog.findViewById(R.id.setludone)
                            val cancel: Button = dialog.findViewById(R.id.setlucancel)
                            val setluname:TextView = dialog.findViewById(R.id.setluname)
                            setluname.setText(R.string.lineup_renaming_lu)
                            edit.hint = BasisSet.current.sele.name
                            val rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))
                            edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]))
                            setdone.setOnClickListener {
                                if (edit.text.toString().isNotEmpty()) {
                                    BasisSet.current.sele.name = edit.text.toString()
                                    val luname1: MutableList<String> = ArrayList()
                                    var i = 0
                                    while (i < BasisSet.current.lb.size) {
                                        luname1.add(BasisSet.current.lb[i].name)
                                        i++
                                    }
                                    val adapter11 = ArrayAdapter(activity, R.layout.spinneradapter, luname1)
                                    luspin.adapter = adapter11
                                    luspin.setSelection(BasisSet.current.lb.size - 1)
                                    StaticStore.SaveLineUp(activity)
                                }
                                dialog.dismiss()
                            }
                            cancel.setOnClickListener { dialog.dismiss() }
                            dialog.show()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_clone_set -> {
                            val origin = BasisSet.current.name
                            BasisSet.current.copy()
                            BasisSet.list[BasisSet.list.size - 1].name = "$origin`"
                            val setname1: MutableList<String> = ArrayList()
                            var i = 0
                            while (i < StaticStore.sets.size) {
                                setname1.add(StaticStore.sets[i].name)
                                i++
                            }
                            val adapter22 = ArrayAdapter(activity, R.layout.spinneradapter, setname1)
                            setspin.adapter = adapter22
                            setspin.setSelection(BasisSet.list.size - 1)
                            StaticStore.SaveLineUp(activity)
                            StaticStore.showShortMessage(activity, R.string.lineup_cloned_set)
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_clone_lineup -> {
                            val lu = BasisSet.current.sele.copy()
                            lu.name = BasisSet.current.sele.name + "`"
                            BasisSet.current.lb.add(lu)
                            val luname1: MutableList<String> = ArrayList()
                            var i = 0
                            while (i < BasisSet.current.lb.size) {
                                luname1.add(BasisSet.current.lb[i].name)
                                i++
                            }
                            val adapter11 = ArrayAdapter(activity, R.layout.spinneradapter, luname1)
                            luspin.adapter = adapter11
                            luspin.setSelection(BasisSet.current.lb.size - 1)
                            StaticStore.SaveLineUp(activity)
                            StaticStore.showShortMessage(activity, R.string.lineup_cloned_lineup)
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_remove_set -> {
                            val builder = AlertDialog.Builder(activity)
                            builder.setTitle(R.string.lineup_removing_set)
                            builder.setMessage(R.string.lineup_remove_set_msg)
                            builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                                BasisSet.list.removeAt(setspin.selectedItemPosition)
                                val pos = setspin.selectedItemPosition
                                val setname2: MutableList<String> = ArrayList()
                                var i = 0
                                while (i < StaticStore.sets.size) {
                                    setname2.add(StaticStore.sets[i].name)
                                    i++
                                }
                                val adapter23 = ArrayAdapter(activity, R.layout.spinneradapter, setname2)
                                setspin.adapter = adapter23
                                if (pos >= BasisSet.list.size) setspin.setSelection(BasisSet.list.size - 1) else setspin.setSelection(pos)

                                try {
                                    StaticStore.SaveLineUp(activity)
                                } catch(e: Exception) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                    StaticStore.showShortMessage(activity, R.string.err_lusave_fail)
                                }
                            }
                            builder.setNegativeButton(R.string.main_file_cancel) { _: DialogInterface?, _: Int -> }
                            builder.show()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.lineup_remove_lineup -> {
                            val builder = AlertDialog.Builder(activity)
                            builder.setTitle(R.string.lineup_removing_lu)
                            builder.setMessage(R.string.lineup_remove_lu_msg)
                            builder.setPositiveButton(R.string.main_file_ok) { _: DialogInterface?, _: Int ->
                                BasisSet.current.lb.removeAt(luspin.selectedItemPosition)
                                val pos = luspin.selectedItemPosition
                                val luname2: MutableList<String> = ArrayList()
                                var i = 0
                                while (i < BasisSet.current.lb.size) {
                                    luname2.add(BasisSet.current.lb[i].name)
                                    i++
                                }
                                val adapter12 = ArrayAdapter(activity, R.layout.spinneradapter, luname2)
                                luspin.adapter = adapter12
                                if (pos >= BasisSet.current.lb.size) luspin.setSelection(BasisSet.current.lb.size - 1) else luspin.setSelection(pos)

                                try {
                                    StaticStore.SaveLineUp(activity)
                                } catch(e: Exception) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                    StaticStore.showShortMessage(activity, R.string.err_lusave_fail)
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

                pager.removeAllViewsInLayout()
                pager.adapter = tab
                pager.offscreenPageLimit = 5
                pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))
                tabs.setupWithViewPager(pager)
                tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        pager.currentItem = tab.position
                        StaticStore.LUtabPosition = tab.position
                        prePosit = tab.position
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
                tabs.getTabAt(StaticStore.LUtabPosition)?.select()
                setspin.setSelection(setn)
                luspin.setSelection(lun)
            }
            lu -> {
                st.setText(R.string.lineup_reading)
            }
            else -> {
                StaticStore.showShortMessage(activity, R.string.lineup_file_err)
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        val tabLayout: TabLayout = activity.findViewById(R.id.lineuptab)
        val measureViewPager: MeasureViewPager = activity.findViewById(R.id.lineuppager)
        val line: LineUpView = activity.findViewById(R.id.lineupView)
        val row = activity.findViewById<TableRow>(R.id.lineupsetrow)
        val schname: TextInputEditText = activity.findViewById(R.id.animschname)
        val layout: TextInputLayout = activity.findViewById(R.id.animschnamel)
        val view = activity.findViewById<View>(R.id.view)
        if (view == null) setAppear(tabLayout, measureViewPager, line, row, schname, layout) else setAppear(tabLayout, measureViewPager, line, row, view, schname, layout)
    }

    private fun setDisappear(vararg view: View) {
        for (v in view) v.visibility = View.GONE
    }

    private fun setAppear(vararg view: View) {
        for (v in view) v.visibility = View.VISIBLE
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

    private inner class LUTab internal constructor(fm: FragmentManager?, private val lineup: LineUpView) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        init {
            val lit = fm?.fragments
            val trans = fm?.beginTransaction()

            if(lit != null) {
                for(f in lit) {
                    trans?.remove(f)
                }

                trans?.commitAllowingStateLoss()
            }
        }

        override fun getItem(i: Int): Fragment {
            when (i) {
                0 -> return LUUnitList.newInstance(StaticStore.lunames, lineup)
                1 -> return LUUnitSetting.newInstance(lineup)
                2 -> return LUCastleSetting.newInstance()
                3 -> return LUTreasureSetting.newInstance()
                4 -> return LUConstruction.newInstance()
                5 -> return newInstance(lineup)
            }

            return LUUnitList.newInstance(StaticStore.lunames, lineup)
        }

        override fun getCount(): Int {
            return 6
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return names[position]
        }

        override fun saveState(): Parcelable? {
            return null
        }
    }

    private fun checkPack(f: File) : Boolean {
        val ac = weakReference.get() ?: return false

        val name = f.name.replace(".bcupack","").replace(".bcudata","")

        val shared = ac.getSharedPreferences(name, Context.MODE_PRIVATE)

        return if(shared.contains(name)) {
            val md5 = shared.getString(name, "")

            val fmd5 = StaticStore.fileToMD5(f)

            val g = File(StaticStore.getExternalRes(ac)+"data/"+f.name.replace(".bcupack",".bcudata"))

            md5 == fmd5 && g.exists()
        } else {
            false
        }
    }

    private fun findBgName(path: String) : String {
        var i = 0

        while(true) {
            val name = "bg-"+ Data.trio(i)+".png"

            val f = File(path, name)

            if(f.exists()) {
                i++
            } else {
                println(name)
                return name
            }
        }
    }

    private fun findCsName(path: String) : String {
        var i = 0

        while(true) {
            val name = "castle-"+ Data.trio(i)+".png"

            val f = File(path, name)

            if(f.exists()) {
                i++
            } else {
                return name
            }
        }
    }

    private fun findPack(f: File) : Pack? {
        val path = f.absolutePath

        for(p in Pack.map) {
            if(p.value.id == 0)
                continue

            val ppath = p.value.file?.absolutePath ?: ""

            if(ppath == path)
                return p.value
        }

        return null
    }

    private fun extractImage(c: Context, img: FakeImage, path: String, name: String, unload: Boolean) : Array<String> {
        val f = File(path)
        val result = arrayOf("", "")

        if(!f.exists()) {
            if(!f.mkdirs()) {
                Log.e("PackExtract", "Failed to create directory "+f.absolutePath)
                return result
            }
        }

        val g = File(path, name)

        if(!g.exists()) {
            if(!g.createNewFile()) {
                Log.e("PackExtract", "Failed to create file "+g.absolutePath)
                return result
            }
        }

        img.bimg() ?: return result

        (img.bimg() as Bitmap).compress(Bitmap.CompressFormat.PNG, 0, FileOutputStream(g))

        if(unload) {
            img.unload()
        }

        return try {
            arrayOf(g.absolutePath.replace(".png",".bcuimg"), StaticStore.fileToMD5(g))
        } catch (e: NoSuchAlgorithmException) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, c)
            arrayOf(g.absolutePath.replace(".png", ".bcuimg"),"")
        }
    }

    private fun removeIfDifferent() {
        val ac = weakReference.get() ?: return

        val path = File(StaticStore.getExternalPack(ac))

        val lit = path.listFiles() ?: return

        for(f in lit) {
            if(!f.name.endsWith(".bcupack"))
                continue

            val name = f.name.replace(".bcupack", "").replace(".bcuata", "")

            val shared = ac.getSharedPreferences(name, Context.MODE_PRIVATE)

            if (shared.contains(name)) {
                val omd5 = shared.getString(name, "")

                val cmd5 = StaticStore.fileToMD5(f)

                if (omd5 != cmd5) {
                    val g = File(StaticStore.getExternalRes(ac) + "data/" + f.name.replace(".bcupack", ".bcudata"))

                    if (g.exists()) {
                        if (!g.delete()) {
                            Log.e("PackExtract", "Failed to remove file " + g.absolutePath)
                        }
                    }
                }
            }
        }
    }

    /**
     * Compare bcupack files with shared preferences data, and remove other pack data automatically
     */
    private fun handlePack() {
        val ac = weakReference.get() ?: return

        val sharedDir = StaticStore.getDataPath()+"shared_prefs/"

        val f = File(sharedDir)

        if(!f.exists())
            return

        val lit = f.listFiles() ?: return

        val handler = listOf<String>().toMutableList()

        for(fs in lit) {
            if(fs.name == "configuration.xml")
                continue

            val name = fs.name.replace(".xml",".bcupack")

            val g = File(StaticStore.getExternalPack(ac), name)

            if(!g.exists()) {
                handler.add(name.replace(".bcupack",""))
            }
        }

        for(name in handler) {
            removeRelatedPackData(name)
        }
    }

    /**
     * Remove all pack data with specified name
     *
     * @param name Name of pack file, must not contain extension
     */
    private fun removeRelatedPackData(name: String) {
        val ac = weakReference.get() ?: return

        if(name == "configuration")
            return

        val sharedPath = StaticStore.getDataPath()+"shared_prefs/$name.xml"

        var f = File(sharedPath)

        if(f.exists()) {
            if(!f.delete()) {
                Log.e("PackExtract","Failed to remove file "+f.absolutePath)
            }
        }

        val resDataPath = StaticStore.getExternalRes(ac)+"data/$name.bcudata"

        f = File(resDataPath)

        if(f.exists()) {
            if(!f.delete()) {
                Log.e("PackExtract", "Failed to remove file "+f.absolutePath)
            }
        }

        val resImgPath = StaticStore.getExternalRes(ac)+"img/$name/"

        f = File(resImgPath)

        StaticStore.removeAllFiles(f)
    }

    /**
     * Check if there are invalid bcupack files, so it won't affect application data shared preferences
     */
    private fun checkValidPack() {
        val ac = weakReference.get() ?: return

        val invalid = listOf("configuration")

        val packDir = StaticStore.getExternalPack(ac)

        val f = File(packDir)

        if(!f.exists())
            return

        val lit = f.listFiles() ?: return

        for(fs in lit) {
            if(invalid.contains(fs.name.replace(".bcupack","").replace(".bcudata","").toLowerCase(Locale.ROOT))) {
                if(!fs.delete()) {
                    Log.e("Adder", "Failed to delete file "+ f.absolutePath)
                }
            }
        }
    }
}