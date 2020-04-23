package com.mandarin.bcu.androidutil.battle.asynchs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaPlayer
import android.os.AsyncTask
import android.util.Log
import android.view.*
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MediaPrepare
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.battle.BDefinder
import com.mandarin.bcu.androidutil.battle.BattleView
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler.resetHandler
import com.mandarin.bcu.androidutil.enemy.EDefiner
import com.mandarin.bcu.androidutil.fakeandroid.AndroidKeys
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics.Companion.clear
import com.mandarin.bcu.androidutil.fakeandroid.FIBM
import com.mandarin.bcu.androidutil.io.AImageWriter
import com.mandarin.bcu.androidutil.io.DefferedLoader
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.MapDefiner
import com.mandarin.bcu.androidutil.unit.Definer
import com.mandarin.bcu.util.page.BBCtrl
import common.battle.BasisSet
import common.battle.SBCtrl
import common.system.P
import common.system.fake.FakeImage
import common.util.Data
import common.util.pack.Pack
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.math.ln

class BAdder(activity: Activity, private val mapcode: Int, private val stid: Int, private val stage: Int, private val star: Int, private val item: Int) : AsyncTask<Void?, String?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private var x = 0f
    private var y = 0f
    
    private val enemy = "0"
    private val map = "1"
    private val battle = "2"
    private val done = "3"
    private val pack = "4"
    private val image = "5"
    private val castle = "6"
    private val bg = "7"
    private val packext = "8"
    
    public override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val fab: FloatingActionButton = activity.findViewById(R.id.battlepause)
        val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
        val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)
        fab.hide()
        fast.hide()
        slow.hide()
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)

        publishProgress(enemy)

        EDefiner().define(activity)

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
        
        publishProgress(map)
        MapDefiner().define(activity)
        publishProgress(battle)
        BDefinder().define(activity)
        publishProgress(done)
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg results: String?) {
        val activity = weakReference.get() ?: return
        val loadt = activity.findViewById<TextView>(R.id.battleloadt)
        when (results[0]) {
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
            enemy -> loadt.setText(R.string.stg_info_enem)
            map -> loadt.setText(R.string.stg_list_stl)
            battle -> loadt.setText(R.string.battle_loading)
            done -> {
                val layout = activity.findViewById<LinearLayout>(R.id.battlelayout)

                val mc = StaticStore.map[mapcode] ?: return

                val stm = mc.maps[stid] ?: return

                val stg = stm.list[stage] ?: return

                val ctrl = SBCtrl(AndroidKeys(), stg, star, BasisSet.current.sele, intArrayOf(item), 0L)

                val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

                val axis = shared.getBoolean("Axis", true)

                val view = BattleView(activity, ctrl, 1, axis,activity, stid)

                view.initialized = false
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                view.id = R.id.battleView
                view.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                layout.addView(view)

                loadt.setText(R.string.battle_prepare)

                val battleView: BattleView = activity.findViewById(R.id.battleView)
                val detector = ScaleGestureDetector(activity, ScaleListener(battleView))
                val actionButton: FloatingActionButton = activity.findViewById(R.id.battlepause)
                val play: FloatingActionButton = activity.findViewById(R.id.battleplay)
                val skipframe: FloatingActionButton = activity.findViewById(R.id.battlenextframe)
                val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
                val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)

                skipframe.setOnClickListener {
                    battleView.painter.bf.update()
                    battleView.invalidate()
                }

                try {
                    actionButton.isExpanded = false
                } catch(e: Exception) {
                    activity.finish()
                }

                actionButton.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        actionButton.isExpanded = true
                        battleView.paused = true

                        fast.hide()
                        slow.hide()
                    }
                })

                play.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        actionButton.isExpanded = false
                        battleView.paused = false

                        fast.show()
                        slow.show()
                    }
                })
                battleView.setOnTouchListener(object : OnTouchListener {
                    var preid = -1
                    var preX = 0

                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        detector.onTouchEvent(event)

                        if (preid == -1)
                            preid = event.getPointerId(0)

                        val id = event.getPointerId(0)

                        val x2 = event.x.toInt()

                        val action = event.action

                        if (action == MotionEvent.ACTION_DOWN) {
                            x = event.x
                            y = event.y
                        } else if (action == MotionEvent.ACTION_UP) {
                            if (battleView.painter.bf.sb.ubase.health > 0 && battleView.painter.bf.sb.ebase.health > 0) {
                                battleView.getPainter().click(Point(event.x.toInt(), event.y.toInt()), action)
                            }
                        } else if (action == MotionEvent.ACTION_MOVE) {
                            if (event.pointerCount == 1 && id == preid) {
                                battleView.painter.pos += x2 - preX

                                if (battleView.paused) {
                                    battleView.invalidate()
                                }
                            }
                        }

                        preX = x2
                        preid = id

                        return false
                    }
                })

                battleView.isLongClickable = true

                battleView.setOnLongClickListener {
                    if (battleView.painter.bf.sb.ubase.health > 0 && battleView.painter.bf.sb.ebase.health > 0) {
                        battleView.getPainter().click(Point(x.toInt(), y.toInt()), BBCtrl.ACTION_LONG)
                    }

                    true
                }

                fast.setOnClickListener {
                    if (battleView.spd < (if (battleView.painter is BBCtrl) 5 else 7)) {
                        battleView.spd++
                        SoundHandler.speed++
                        battleView.painter.reset()
                    }
                }

                slow.setOnClickListener {
                    if (battleView.spd > -7) {
                        battleView.spd--
                        SoundHandler.speed--
                        battleView.painter.reset()
                    }
                }

                val exitbattle = activity.findViewById<Button>(R.id.battleexit)

                exitbattle.setOnClickListener {
                    if (battleView.painter.bf.sb.ebase.health > 0 && battleView.painter.bf.sb.ubase.health > 0 && shared.getBoolean("show", true)) {
                        val alert = AlertDialog.Builder(activity, R.style.AlertDialog)
                        val inflater = LayoutInflater.from(activity)
                        val layouts = inflater.inflate(R.layout.do_not_show_dialog, null)
                        val donotshow = layouts.findViewById<CheckBox>(R.id.donotshowcheck)
                        val cancel = layouts.findViewById<Button>(R.id.battlecancel)
                        val exit = layouts.findViewById<Button>(R.id.battledexit)

                        alert.setView(layouts)

                        donotshow.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                val editor = shared.edit()
                                editor.putBoolean("show", false)
                                editor.apply()
                            } else {
                                val editor = shared.edit()
                                editor.putBoolean("show", true)
                                editor.apply()
                            }
                        }

                        val dialog = alert.create()

                        cancel.setOnClickListener { dialog.cancel() }

                        exit.setOnClickListener {
                            P.stack.clear()

                            clear()

                            dialog.dismiss()

                            resetHandler()

                            if (SoundHandler.MUSIC.isInitialized) {
                                if (SoundHandler.MUSIC.isRunning) {
                                    SoundHandler.MUSIC.stop()
                                    SoundHandler.MUSIC.release()
                                } else {
                                    SoundHandler.MUSIC.release()
                                }
                            }

                            battleView.unload()

                            activity.finish()
                        }

                        dialog.show()
                    } else {
                        P.stack.clear()

                        clear()

                        battleView.unload()

                        resetHandler()

                        if (SoundHandler.MUSIC.isInitialized) {
                            if (SoundHandler.MUSIC.isRunning) {
                                SoundHandler.MUSIC.stop()
                                SoundHandler.MUSIC.release()
                            } else {
                                SoundHandler.MUSIC.release()
                            }
                        }
                        activity.finish()
                    }
                }
                val retry = activity.findViewById<Button>(R.id.battleretry)

                retry.setOnClickListener {
                    if (battleView.painter.bf.sb.ebase.health > 0 && battleView.painter.bf.sb.ubase.health > 0 && shared.getBoolean("retry_show", true)) {
                        val alert = AlertDialog.Builder(activity, R.style.AlertDialog)

                        val inflater = LayoutInflater.from(activity)

                        val layouts = inflater.inflate(R.layout.do_not_show_dialog, null)

                        val donotshow = layouts.findViewById<CheckBox>(R.id.donotshowcheck)
                        val content = layouts.findViewById<TextView>(R.id.donotshowcontent)
                        val cancel = layouts.findViewById<Button>(R.id.battlecancel)
                        val exit = layouts.findViewById<Button>(R.id.battledexit)

                        exit.text = activity.getString(R.string.battle_retry)

                        content.text = activity.getString(R.string.battle_sure_retry)

                        alert.setView(layouts)

                        donotshow.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                val editor = shared.edit()

                                editor.putBoolean("retry_show", false)
                                editor.apply()
                            } else {
                                val editor = shared.edit()

                                editor.putBoolean("retry_show", true)
                                editor.apply()
                            }
                        }
                        val dialog = alert.create()
                        cancel.setOnClickListener { dialog.cancel() }
                        exit.setOnClickListener {
                            battleView.retry()
                            P.stack.clear()
                            clear()
                            dialog.dismiss()
                        }
                        dialog.show()
                    } else {
                        battleView.retry()
                    }
                }

                val mus = activity.findViewById<Switch>(R.id.switchmus)
                val musvol = activity.findViewById<SeekBar>(R.id.seekmus)

                mus.isChecked = shared.getBoolean("music", true)
                musvol.isEnabled = shared.getBoolean("music", true)
                musvol.max = 99

                mus.setOnCheckedChangeListener { _, isChecked ->
                    mus.isClickable = false
                    mus.postDelayed({ mus.isClickable = true }, 1000)
                    if (isChecked) {
                        val editor = shared.edit()

                        editor.putBoolean("music", true)
                        editor.apply()

                        SoundHandler.musicPlay = true

                        musvol.isEnabled = true

                        if (!SoundHandler.MUSIC.isPlaying && SoundHandler.MUSIC.isInitialized) {
                            SoundHandler.MUSIC.start()
                        }
                    } else {
                        val editor = shared.edit()

                        editor.putBoolean("music", false)
                        editor.apply()

                        SoundHandler.musicPlay = false

                        musvol.isEnabled = false

                        if (SoundHandler.MUSIC.isInitialized && SoundHandler.MUSIC.isRunning) {
                            if (SoundHandler.MUSIC.isPlaying)
                                SoundHandler.MUSIC.pause()
                        }
                    }
                }

                musvol.progress = shared.getInt("mus_vol", 99)

                musvol.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("mus_vol", progress)
                            editor.apply()

                            val log1 = (1 - ln(100 - progress.toDouble()) / ln(100.0)).toFloat()

                            SoundHandler.MUSIC.setVolume(log1, log1)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

                val switchse = activity.findViewById<Switch>(R.id.switchse)
                val seekse = activity.findViewById<SeekBar>(R.id.seekse)

                switchse.isChecked = shared.getBoolean("SE", true)

                seekse.isEnabled = shared.getBoolean("SE", true)
                seekse.max = 99
                seekse.progress = shared.getInt("se_vol", 99)

                switchse.setOnCheckedChangeListener { _, isChecked ->
                    switchse.isClickable = false
                    switchse.postDelayed({ switchse.isClickable = true }, 1000)
                    if (isChecked) {
                        val editor = shared.edit()

                        editor.putBoolean("SE", true)
                        editor.apply()

                        SoundHandler.sePlay = true
                        SoundHandler.se_vol = StaticStore.getVolumScaler((shared.getInt("se_vol", 99) * 0.85).toInt())

                        seekse.isEnabled = true
                    } else {
                        val editor = shared.edit()

                        editor.putBoolean("SE", false)
                        editor.apply()

                        SoundHandler.sePlay = false
                        SoundHandler.se_vol = 0f

                        seekse.isEnabled = false
                    }
                }

                seekse.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("se_vol", progress)
                            editor.apply()

                            SoundHandler.se_vol = StaticStore.getVolumScaler((progress * 0.85).toInt())
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return

        val battleView: BattleView = activity.findViewById(R.id.battleView)
        val prog = activity.findViewById<ProgressBar>(R.id.battleprog)
        val loadt = activity.findViewById<TextView>(R.id.battleloadt)
        val fab: FloatingActionButton = activity.findViewById(R.id.battlepause)
        val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
        val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)

        setAppear(battleView)

        fab.show()

        fast.show()

        slow.show()

        (prog.parent as ViewManager).removeView(prog)

        (loadt.parent as ViewManager).removeView(loadt)

        battleView.initialized = true

        if (SoundHandler.MUSIC.isInitialized && !SoundHandler.MUSIC.isRunning) {
            SoundHandler.MUSIC.prepareAsync()

            SoundHandler.MUSIC.setOnPreparedListener(object : MediaPrepare() {
                override fun prepare(mp: MediaPlayer?) {
                    if (SoundHandler.musicPlay)
                        SoundHandler.MUSIC.start()
                }
            })
        }
    }

    private fun setAppear(vararg views: View) {
        for (v in views) v.visibility = View.VISIBLE
    }

    private inner class ScaleListener internal constructor(private val cView: BattleView) : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            cView.painter.siz *= detector.scaleFactor.toDouble()

            if (cView.paused) {
                cView.invalidate()
            }

            return true
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