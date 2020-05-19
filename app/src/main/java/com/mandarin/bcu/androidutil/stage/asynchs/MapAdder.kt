package com.mandarin.bcu.androidutil.stage.asynchs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import androidx.core.util.isEmpty
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.MapList
import com.mandarin.bcu.R
import com.mandarin.bcu.StageList
import com.mandarin.bcu.StageSearchFilter
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.filter
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.EDefiner
import com.mandarin.bcu.androidutil.fakeandroid.FIBM
import com.mandarin.bcu.androidutil.pack.AImageWriter
import com.mandarin.bcu.androidutil.io.DefferedLoader
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.MapDefiner
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import com.mandarin.bcu.androidutil.unit.Definer
import common.system.MultiLangCont
import common.system.fake.FakeImage
import common.system.files.VFile
import common.util.Data
import common.util.pack.Pack
import common.util.stage.MapColc
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

class MapAdder(activity: Activity) : AsyncTask<Void?, String?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    
    private val unit = "0"
    private val map = "1"
    private val enemy = "2"
    private val pack = "3"
    private val image = "4"
    private val castle = "5"
    private val bg = "6"
    private val packext = "7"
    private val icon = "8"
    private val done = "9"
    
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val maplist = activity.findViewById<ListView>(R.id.maplist)
        maplist.visibility = View.GONE
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        publishProgress(unit)
        Definer().define(activity)
        publishProgress(enemy)
        EDefiner().define(activity)

        publishProgress(pack)

        if(!StaticStore.packread && Pack.map.size == 1) {

            checkValidPack()
            handlePack()
            removeIfDifferent()

            try {

                Pack.read()
                StaticStore.packread = true
                DefferedLoader.clearPending("Context", activity)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
            }

            for (path in DefineItf.packPath) {
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

                        val bpathList = java.util.ArrayList<String>()

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

                        val cpathList = java.util.ArrayList<String>()

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
        }

        publishProgress(map)
        MapDefiner().define(activity)

        publishProgress(icon)

        if (StaticStore.eicons == null) {
            StaticStore.eicons = arrayOfNulls(StaticStore.emnumber)
            for (i in 0 until StaticStore.emnumber) {
                val shortPath = "./org/enemy/" + number(i) + "/enemy_icon_" + number(i) + ".png"
                try {
                    val ratio = 32f / 32f
                    StaticStore.eicons[i] = StaticStore.getResizeb(Objects.requireNonNull(VFile.getFile(shortPath)).data.img.bimg() as Bitmap, activity, 36f * ratio)
                } catch (e: Exception) {
                    val ratio = 32f / 32f
                    StaticStore.eicons[i] = StaticStore.empty(activity, 18f * ratio, 18f * ratio)
                }
            }
        }
        publishProgress(done)
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        val activity = weakReference.get() ?: return
        val mapst = activity.findViewById<TextView>(R.id.mapst)
        when (values[0]) {
            unit -> mapst.setText(R.string.unit_list_unitload)
            
            map -> mapst.setText(R.string.stg_info_stgd)

            pack -> {
                mapst.setText(R.string.main_pack)
            }

            image -> {
                val name = activity.getString(R.string.main_pack_img)+ (values[1] ?: "")

                mapst.text = name
            }

            bg -> {
                val name = activity.getString(R.string.main_pack_bg) + (values[1] ?: "")

                mapst.text = name
            }

            castle -> {
                val name = activity.getString(R.string.main_pack_castle) + (values[1] ?: "")

                mapst.text = name
            }

            packext -> {
                val name = activity.getString(R.string.main_pack_ext)+ (values[1] ?: "")

                mapst.text = name
            }
            
            enemy -> mapst.text = activity.getString(R.string.stg_info_enem)
            icon -> mapst.setText(R.string.stg_list_enemic)
            done -> {
                mapst.text = activity.getString(R.string.stg_info_stgs)
                val stageset = activity.findViewById<Spinner>(R.id.stgspin)
                val maplist = activity.findViewById<ListView>(R.id.maplist)

                if(filter == null) {
                    var maxWidth = 0

                    val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(activity, R.layout.spinneradapter, StaticStore.mapcolcname) {
                        override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                            val v = super.getView(position, converView, parent)

                            (v as TextView).setTextColor(ContextCompat.getColor(activity, R.color.TextPrimary))

                            val eight = StaticStore.dptopx(8f, activity)

                            v.setPadding(eight, eight, eight, eight)

                            v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                            if(maxWidth < v.measuredWidth) {
                                maxWidth = v.measuredWidth
                            }

                            return v
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v = super.getDropDownView(position, convertView, parent)

                            (v as TextView).setTextColor(ContextCompat.getColor(activity, R.color.TextPrimary))

                            return v
                        }
                    }

                    stageset.adapter = adapter

                    val layout = stageset.layoutParams

                    layout.width = maxWidth

                    stageset.layoutParams = layout

                    stageset.onItemSelectedListener = object : OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            try {
                                val positions = ArrayList<Int>()

                                val mc = if(position < StaticStore.BCmaps) {
                                    MapColc.MAPS[StaticStore.mapcode[position]] ?: return
                                } else {
                                    val p = Pack.map[StaticStore.mapcode[position]] ?: return

                                    p.mc ?: return
                                }

                                try {
                                    for (i in mc.maps.indices) {
                                        positions.add(i)
                                    }
                                } catch (e : java.lang.IndexOutOfBoundsException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                    return
                                }
                                
                                val names = ArrayList<String>()
                                
                                for(i in mc.maps.indices) {
                                    val stm = mc.maps[i] 
                                    
                                    names.add(MultiLangCont.SMNAME.getCont(stm) ?: stm.name ?: "")
                                }

                                val mapListAdapter = MapListAdapter(activity, names, StaticStore.mapcode[position], positions, position >= StaticStore.BCmaps)
                                maplist.adapter = mapListAdapter
                            } catch (e: NullPointerException) {
                                ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    val positions = ArrayList<Int>()
                    val name = ArrayList<String>()

                    stageset.setSelection(0)
                    
                    val mc = if(stageset.selectedItemPosition < StaticStore.BCmaps) {
                        MapColc.MAPS[StaticStore.mapcode[stageset.selectedItemPosition]] ?: return
                    } else {
                        Pack.map[StaticStore.mapcode[stageset.selectedItemPosition]]?.mc ?: return
                    }
                    
                    for(i in mc.maps.indices) {
                        val stm = mc.maps[i] 
                        
                        positions.add(i)
                        name.add(MultiLangCont.SMNAME.getCont(stm) ?: stm.name ?: "")
                    }

                    val mapListAdapter = MapListAdapter(activity, name, StaticStore.mapcode[stageset.selectedItemPosition], positions, stageset.selectedItemPosition >= StaticStore.BCmaps)
                    
                    maplist.adapter = mapListAdapter
                    
                    maplist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.maplistClick = SystemClock.elapsedRealtime()
                        val intent = Intent(activity, StageList::class.java)
                        intent.putExtra("mapcode", StaticStore.mapcode[stageset.selectedItemPosition])
                        intent.putExtra("stid", position)
                        intent.putExtra("custom", stageset.selectedItemPosition >= StaticStore.BCmaps)
                        activity.startActivity(intent)
                    }
                } else {
                    if(filter.isEmpty()) {
                        stageset.visibility = View.GONE
                        maplist.visibility = View.GONE
                    } else {
                        stageset.visibility = View.VISIBLE
                        maplist.visibility = View.VISIBLE

                        val resmc = ArrayList<String>()
                        val resposition = ArrayList<Int>()

                        for (i in 0 until filter.size()) {
                            val index = StaticStore.mapcode.indexOf(filter.keyAt(i))

                            if (index != -1) {
                                resmc.add(StaticStore.mapcolcname[index])
                            }
                        }

                        var maxWidth = 0

                        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(activity, R.layout.spinneradapter, StaticStore.mapcolcname) {
                            override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                                val v = super.getView(position, converView, parent)

                                (v as TextView).setTextColor(ContextCompat.getColor(activity, R.color.TextPrimary))

                                val eight = StaticStore.dptopx(8f, activity)

                                v.setPadding(eight, eight, eight, eight)

                                v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                                if(maxWidth < v.measuredWidth) {
                                    maxWidth = v.measuredWidth
                                }

                                return v
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val v = super.getDropDownView(position, convertView, parent)

                                (v as TextView).setTextColor(ContextCompat.getColor(activity, R.color.TextPrimary))

                                return v
                            }
                        }

                        val layout = stageset.layoutParams

                        layout.width = maxWidth

                        stageset.layoutParams = layout

                        stageset.requestLayout()

                        stageset.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                try {
                                    var index = StaticStore.mapcode.indexOf(filter.keyAt(position))

                                    if (index == -1)
                                        index = 0

                                    val resmapname = ArrayList<String>()
                                    resposition.clear()

                                    val resmaplist = filter[filter.keyAt(position)]

                                    val mc = if(index < StaticStore.BCmaps) {
                                        MapColc.MAPS[index] ?: return
                                    } else {
                                        Pack.map[index]?.mc ?: return
                                    }

                                    for(i in 0 until resmaplist.size()) {
                                        val stm = mc.maps[resmaplist.keyAt(i)]
                                        
                                        resmapname.add(MultiLangCont.SMNAME.getCont(stm) ?: stm.name ?: "")
                                        resposition.add(resmaplist.keyAt(i))
                                    }

                                    val mapListAdapter = MapListAdapter(activity, resmapname, filter.keyAt(position), resposition, index >= StaticStore.BCmaps)
                                    maplist.adapter = mapListAdapter
                                } catch (e: NullPointerException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                } catch (e: IndexOutOfBoundsException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                }
                            }

                        }

                        stageset.adapter = adapter

                        val index = StaticStore.mapcode.indexOf(filter.keyAt(stageset.selectedItemPosition))

                        if (index == -1)
                            return

                        val mc = if(index < StaticStore.BCmaps) {
                            MapColc.MAPS[filter.keyAt(stageset.selectedItemPosition)] ?: return
                        } else {
                            Pack.map[filter.keyAt(stageset.selectedItemPosition)]?.mc ?: return
                        }

                        val resmapname = ArrayList<String>()

                        val resmaplist = filter[filter.keyAt(stageset.selectedItemPosition)]

                        for(i in 0 until resmaplist.size()) {
                            val stm = mc.maps[resmaplist.keyAt(i)]

                            resmapname.add(MultiLangCont.SMNAME.getCont(stm) ?: stm.name ?: "")
                            resposition.add(resmaplist.keyAt(i))
                        }

                        val mapListAdapter = MapListAdapter(activity, resmapname, filter.keyAt(stageset.selectedItemPosition),resposition, index >= StaticStore.BCmaps)
                        maplist.adapter = mapListAdapter

                        maplist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                            if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                            StaticStore.maplistClick = SystemClock.elapsedRealtime()
                            val intent = Intent(activity, StageList::class.java)

                            val rIndex = StaticStore.mapcode.indexOf(filter.keyAt(stageset.selectedItemPosition))

                            intent.putExtra("mapcode", filter.keyAt(stageset.selectedItemPosition))
                            intent.putExtra("stid", resposition[position])
                            intent.putExtra("custom", rIndex >= StaticStore.BCmaps)

                            activity.startActivity(intent)
                        }
                    }
                }


                val stgfilter = activity.findViewById<FloatingActionButton>(R.id.stgfilter)
                stgfilter.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(activity,StageSearchFilter::class.java)
                        activity.startActivityForResult(intent, MapList.REQUEST_CODE)
                    }
                })
            }
        }
    }

    override fun onPostExecute(results: Void?) {
        val activity = weakReference.get() ?: return
        val maplist = activity.findViewById<ListView>(R.id.maplist)
        val mapst = activity.findViewById<TextView>(R.id.mapst)
        val mapprog = activity.findViewById<ProgressBar>(R.id.mapprog)
        maplist.visibility = View.VISIBLE
        mapst.visibility = View.GONE
        mapprog.visibility = View.GONE
    }

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> "00$num"
            in 10..99 -> "0$num"
            else -> "" + num
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