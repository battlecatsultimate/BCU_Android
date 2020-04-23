package com.mandarin.bcu.androidutil.battle.asynchs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.BattlePrepare
import com.mandarin.bcu.BattleSimulation
import com.mandarin.bcu.LineUpScreen
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.fakeandroid.FIBM
import com.mandarin.bcu.androidutil.io.AImageWriter
import com.mandarin.bcu.androidutil.io.DefferedLoader
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.unit.Definer
import common.battle.BasisSet
import common.io.InStream
import common.system.MultiLangCont
import common.system.fake.FakeImage
import common.util.Data
import common.util.pack.Pack
import common.util.stage.MapColc
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.security.NoSuchAlgorithmException
import java.util.*

open class BPAdder : AsyncTask<Void?, String?, Void?> {
    private val weakReference: WeakReference<Activity>
    private val mapcode: Int
    private val stid: Int
    private val posit: Int
    private var selection = 0
    private var item = 0

    private val lu = "0"
    private val done = "1"
    private val pack = "2"
    private val image = "3"
    private val castle = "4"
    private val bg = "5"
    private val packext = "6"

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
                            BasisSet.list.clear()
                            BasisSet()
                            ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                        }
                    } catch (e: Exception) {
                        ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                    }
                }
            }
            StaticStore.LUread = true
        }
        StaticStore.sets = BasisSet.list
        val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        var set = preferences.getInt("equip_set", 0)
        var lu = preferences.getInt("equip_lu", 0)
        if (set >= BasisSet.list.size) set = if(BasisSet.list.size == 0) 0 else BasisSet.list.size - 1
        BasisSet.current = StaticStore.sets[set]
        if (lu >= BasisSet.current.lb.size) lu = if(BasisSet.current.lb.size == 0) 0 else BasisSet.current.lb.size - 1
        BasisSet.current.sele = BasisSet.current.lb[lu]
        publishProgress(done)
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg results: String?) {
        val activity = weakReference.get() ?: return
        val loadt = activity.findViewById<TextView>(R.id.preparet)
        when (results[0]) {
            lu -> loadt.setText(R.string.lineup_reading)

            pack -> {
                loadt.setText(R.string.main_pack)
            }

            image -> {
                val name = activity.getString(R.string.main_pack_img)+ (results[1] ?: "")

                loadt.text = name
            }

            bg -> {
                val name = activity.getString(R.string.main_pack_bg) + (results[1] ?: "")

                loadt.text = name
            }

            castle -> {
                val name = activity.getString(R.string.main_pack_castle) + (results[1] ?: "")

                loadt.text = name
            }

            packext -> {
                val name = activity.getString(R.string.main_pack_ext)+ (results[1] ?: "")

                loadt.text = name
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
                line.updateLineUp()
                setname.text = setLUName

                val index = StaticStore.mapcode.indexOf(mapcode)

                val mc = if(index < StaticStore.BCmaps) {
                    MapColc.MAPS[mapcode] ?: return
                } else {
                    val p = Pack.map[mapcode]

                    if(p != null) {
                        p.mc
                    } else {
                        return
                    }
                }

                if (stid >= mc.maps.size)
                    return

                val stm = mc.maps[stid] ?: return

                if (posit >= stm.list.size) return
                val st = stm.list[posit]
                stname.text = MultiLangCont.STNAME.getCont(st) ?: st.name ?: getStageName(posit)
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
                    println(item)
                }
                rich.isChecked = BattlePrepare.rich
                rich.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        item += 1
                    } else {
                        item -= 1
                    }
                    BattlePrepare.rich = isChecked
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
                bck.setOnClickListener {
                    BattlePrepare.rich = false
                    BattlePrepare.sniper = false
                    activity.finish()
                }
            }
            else -> StaticStore.showShortMessage(activity, results[0] ?: "BCU")
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