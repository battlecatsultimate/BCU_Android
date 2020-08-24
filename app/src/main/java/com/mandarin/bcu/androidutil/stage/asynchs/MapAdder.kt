package com.mandarin.bcu.androidutil.stage.asynchs

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.MapList
import com.mandarin.bcu.R
import com.mandarin.bcu.StageList
import com.mandarin.bcu.StageSearchFilter
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.filter
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.EDefiner
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.MapDefiner
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import com.mandarin.bcu.androidutil.unit.Definer
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.system.files.VFile
import common.util.Data
import common.util.stage.MapColc
import common.util.stage.StageMap
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

class MapAdder(activity: Activity) : AsyncTask<Void?, String?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    
    private val unit = "0"
    private val map = "1"
    private val enemy = "2"
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
        Definer.define(activity)
        publishProgress(enemy)
        EDefiner.define(activity)

        publishProgress(map)
        MapDefiner().define(activity)

        publishProgress(icon)

        if (StaticStore.eicons == null) {
            StaticStore.eicons = Array(UserProfile.getBCData().enemies.list.size) { i ->
                val shortPath = "./org/enemy/" + Data.trio(i) + "/enemy_icon_" + Data.trio(i) + ".png"
                val vf = VFile.getFile(shortPath)

                if(vf == null) {
                    StaticStore.empty(activity, 18f, 18f)
                }

                val icon = vf.data.img.bimg()

                if(icon == null) {
                    StaticStore.empty(activity, 18f, 18f)
                }

                StaticStore.getResizeb(icon as Bitmap, activity, 36f)
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

                            return v
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v = super.getDropDownView(position, convertView, parent)

                            (v as TextView).setTextColor(ContextCompat.getColor(activity, R.color.TextPrimary))

                            v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                            if(maxWidth < v.measuredWidth) {
                                println(maxWidth)

                                maxWidth = v.measuredWidth

                                val layout = stageset.layoutParams

                                layout.width = maxWidth

                                stageset.layoutParams = layout
                            }

                            return v
                        }
                    }

                    stageset.adapter = adapter

                    stageset.onItemSelectedListener = object : OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            try {
                                val positions = ArrayList<Int>()

                                val mc = MapColc.get(StaticStore.mapcode[position])

                                try {
                                    for (i in mc.maps.list.indices) {
                                        positions.add(i)
                                    }
                                } catch (e : java.lang.IndexOutOfBoundsException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                    return
                                }
                                
                                val names = ArrayList<Identifier<StageMap>>()
                                
                                for(i in mc.maps.list.indices) {
                                    val stm = mc.maps.list[i]

                                    names.add(stm.id)
                                }

                                val mapListAdapter = MapListAdapter(activity, names)
                                maplist.adapter = mapListAdapter
                            } catch (e: NullPointerException) {
                                ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    val name = ArrayList<Identifier<StageMap>>()

                    stageset.setSelection(0)
                    
                    val mc = MapColc.get(StaticStore.mapcode[stageset.selectedItemPosition]) ?: return
                    
                    for(i in mc.maps.list.indices) {
                        val stm = mc.maps[i] 

                        name.add(stm.id)
                    }

                    val mapListAdapter = MapListAdapter(activity, name)
                    
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
                    val f = filter ?: return

                    if(f.isEmpty()) {
                        stageset.visibility = View.GONE
                        maplist.visibility = View.GONE
                    } else {
                        stageset.visibility = View.VISIBLE
                        maplist.visibility = View.VISIBLE

                        val resmc = ArrayList<String>()

                        val keys = f.keys

                        for (i in keys) {
                            val index = StaticStore.mapcode.indexOf(i)

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

                                return v
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val v = super.getDropDownView(position, convertView, parent)

                                (v as TextView).setTextColor(ContextCompat.getColor(activity, R.color.TextPrimary))

                                v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                                if(maxWidth < v.measuredWidth) {
                                    println(maxWidth)

                                    maxWidth = v.measuredWidth

                                    val layout = stageset.layoutParams

                                    layout.width = maxWidth

                                    stageset.layoutParams = layout
                                }

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
                                    val fi = filter ?: return
                                    val key = f.keys.toMutableList()

                                    val resmapname = ArrayList<Identifier<StageMap>>()

                                    val resmaplist = fi[key[position]] ?: return

                                    val mc = MapColc.get(key[position]) ?: return

                                    for(i in 0 until resmaplist.size()) {
                                        val stm = mc.maps[resmaplist.keyAt(i)]
                                        
                                        resmapname.add(stm.id)
                                    }

                                    val mapListAdapter = MapListAdapter(activity, resmapname)

                                    maplist.adapter = mapListAdapter
                                } catch (e: NullPointerException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                } catch (e: IndexOutOfBoundsException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload, activity)
                                }
                            }

                        }

                        stageset.adapter = adapter

                        val fi = filter ?: return
                        val key = fi.keys.toMutableList()

                        val mc = MapColc.get(key[stageset.selectedItemPosition]) ?: return

                        val resmapname = ArrayList<Identifier<StageMap>>()

                        val resmaplist = fi[key[stageset.selectedItemPosition]] ?: return

                        for(i in 0 until resmaplist.size()) {
                            val stm = mc.maps[resmaplist.keyAt(i)]

                            resmapname.add(stm.id)
                        }

                        val mapListAdapter = MapListAdapter(activity, resmapname)
                        maplist.adapter = mapListAdapter

                        maplist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                            if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                            StaticStore.maplistClick = SystemClock.elapsedRealtime()
                            val intent = Intent(activity, StageList::class.java)

                            if(maplist.adapter !is MapListAdapter)
                                return@OnItemClickListener

                            val stm = Identifier.get((maplist.adapter as MapListAdapter).getItem(position)) ?: return@OnItemClickListener

                            intent.putExtra("Data", JsonEncoder.encode(stm).toString())
                            intent.putExtra("custom", stm.id.pack != Identifier.DEF)

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
}