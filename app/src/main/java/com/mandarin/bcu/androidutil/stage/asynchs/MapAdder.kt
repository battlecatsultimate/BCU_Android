package com.mandarin.bcu.androidutil.stage.asynchs

import android.app.Activity
import android.content.Intent
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
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.filter
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.stage.MapColc
import common.util.stage.StageMap
import java.lang.ref.WeakReference

class MapAdder(activity: Activity) : AsyncTask<Void, String, Void>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)

    private val done = "9"
    
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val maplist = activity.findViewById<ListView>(R.id.maplist)
        maplist.visibility = View.GONE
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null

        Definer.define(activity, this::updateProg, this::updateText)

        publishProgress(done)

        return null
    }

    override fun onProgressUpdate(vararg values: String) {
        val activity = weakReference.get() ?: return
        val mapst = activity.findViewById<TextView>(R.id.status)
        when (values[0]) {
            StaticStore.TEXT -> mapst.text = values[1]
            StaticStore.PROG -> {
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                if(values[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = values[1].toInt()
            }
            done -> {
                mapst.text = activity.getString(R.string.stg_info_stgs)
                val stageset = activity.findViewById<Spinner>(R.id.stgspin)
                val maplist = activity.findViewById<ListView>(R.id.maplist)
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                prog.isIndeterminate = true

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

                        if(maplist.adapter !is MapListAdapter)
                            return@OnItemClickListener

                        val stm = Identifier.get((maplist.adapter as MapListAdapter).getItem(position)) ?: return@OnItemClickListener

                        val intent = Intent(activity, StageList::class.java)

                        intent.putExtra("Data",JsonEncoder.encode(stm.id).toString())
                        intent.putExtra("custom", stm.id.pack != Identifier.DEF)

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

                        val keys = f.keys.toMutableList()

                        keys.sort()

                        for (i in keys) {
                            println(i)

                            val index = StaticStore.mapcode.indexOf(i)

                            if (index != -1) {
                                resmc.add(StaticStore.mapcolcname[index])
                            }
                        }

                        var maxWidth = 0

                        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(activity, R.layout.spinneradapter, resmc) {
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
                                    val resmapname = ArrayList<Identifier<StageMap>>()

                                    val resmaplist = f[keys[position]] ?: return

                                    val mc = MapColc.get(keys[position]) ?: return

                                    for(i in 0 until resmaplist.size()) {
                                        val stm = mc.maps.list[resmaplist.keyAt(i)]
                                        
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

                        val mc = MapColc.get(keys[stageset.selectedItemPosition]) ?: return

                        val resmapname = ArrayList<Identifier<StageMap>>()

                        val resmaplist = f[keys[stageset.selectedItemPosition]] ?: return

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

                            intent.putExtra("Data", JsonEncoder.encode(stm.id).toString())
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
        val mapst = activity.findViewById<TextView>(R.id.status)
        val mapprog = activity.findViewById<ProgressBar>(R.id.prog)
        maplist.visibility = View.VISIBLE
        mapst.visibility = View.GONE
        mapprog.visibility = View.GONE
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }
}