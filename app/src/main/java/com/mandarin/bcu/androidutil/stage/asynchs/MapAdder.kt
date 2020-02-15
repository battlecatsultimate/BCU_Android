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
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.stage.MapDefiner
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import com.mandarin.bcu.androidutil.unit.Definer
import common.system.MultiLangCont
import common.system.files.VFile
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class MapAdder(activity: Activity) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val maplist = activity.findViewById<ListView>(R.id.maplist)
        maplist.visibility = View.GONE
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)
        publishProgress(0)
        MapDefiner().define(activity)
        publishProgress(1)
        EDefiner().define(activity)
        publishProgress(3)
        if (StaticStore.enames == null) {
            StaticStore.enames = arrayOfNulls(StaticStore.emnumber)
            for (i in 0 until StaticStore.emnumber) {
                StaticStore.enames[i] = withID(i, MultiLangCont.ENAME.getCont(StaticStore.enemies[i]) ?: "")
            }
        }
        publishProgress(4)
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
        publishProgress(5)
        return null
    }

    override fun onProgressUpdate(vararg results: Int?) {
        val activity = weakReference.get() ?: return
        val mapst = activity.findViewById<TextView>(R.id.mapst)
        when (results[0]) {
            0 -> mapst.setText(R.string.stg_info_stgd)
            1 -> mapst.text = activity.getString(R.string.stg_info_enem)
            2 -> mapst.text = activity.getString(R.string.stg_info_enemimg)
            3 -> mapst.text = activity.getString(R.string.stg_info_enemname)
            4 -> mapst.setText(R.string.stg_list_enemic)
            5 -> {
                mapst.text = activity.getString(R.string.stg_info_stgs)
                val stageset = activity.findViewById<Spinner>(R.id.stgspin)
                val maplist = activity.findViewById<ListView>(R.id.maplist)

                if(filter == null) {
                    stageset.onItemSelectedListener = object : OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            try {
                                val positions = ArrayList<Int>()

                                for (i in StaticStore.mapnames[position].indices) {
                                    positions.add(i)
                                }

                                val mapListAdapter = MapListAdapter(activity, StaticStore.mapnames[position], StaticStore.MAPCODE[position], positions)
                                maplist.adapter = mapListAdapter
                            } catch (e: NullPointerException) {
                                ErrorLogWriter.writeLog(e, StaticStore.upload)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    val positions = ArrayList<Int>()

                    for (i in StaticStore.mapnames[stageset.selectedItemPosition].indices) {
                        positions.add(i)
                    }

                    val mapListAdapter = MapListAdapter(activity, StaticStore.mapnames[stageset.selectedItemPosition], StaticStore.MAPCODE[stageset.selectedItemPosition], positions)
                    maplist.adapter = mapListAdapter
                    maplist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.maplistClick = SystemClock.elapsedRealtime()
                        val intent = Intent(activity, StageList::class.java)
                        intent.putExtra("mapcode", StaticStore.MAPCODE[stageset.selectedItemPosition])
                        intent.putExtra("stid", position)
                        activity.startActivity(intent)
                    }
                } else {
                    if(filter.isEmpty()) {
                        stageset.visibility = View.GONE
                        maplist.visibility = View.GONE
                    } else {
                        stageset.visibility = View.VISIBLE
                        maplist.visibility = View.VISIBLE

                        val mapcolcarray = activity.resources.getStringArray(R.array.set_stg)

                        val resmc = ArrayList<String>()
                        val resposition = ArrayList<Int>()

                        for (i in 0 until filter.size()) {
                            val index = StaticStore.MAPCODE.indexOf(filter.keyAt(i))

                            if (index != -1) {
                                resmc.add(mapcolcarray[index])
                            }
                        }

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
                                return v
                            }
                        }

                        stageset.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                try {
                                    var index = StaticStore.MAPCODE.indexOf(filter.keyAt(position))

                                    if (index == -1)
                                        index = 0

                                    val resmapname = ArrayList<String>()
                                    resposition.clear()

                                    val resmaplist = filter[filter.keyAt(position)]

                                    for(i in 0 until resmaplist.size()) {
                                        resmapname.add(StaticStore.mapnames[index][resmaplist.keyAt(i)])
                                        resposition.add(resmaplist.keyAt(i))
                                    }

                                    val mapListAdapter = MapListAdapter(activity, resmapname.toTypedArray(), filter.keyAt(position), resposition)
                                    maplist.adapter = mapListAdapter
                                } catch (e: NullPointerException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload)
                                } catch (e: IndexOutOfBoundsException) {
                                    ErrorLogWriter.writeLog(e, StaticStore.upload)
                                }
                            }

                        }

                        stageset.adapter = adapter

                        var index = StaticStore.MAPCODE.indexOf(filter.keyAt(stageset.selectedItemPosition))

                        if (index == -1)
                            index = 0

                        val resmapname = ArrayList<String>()

                        val resmaplist = filter[filter.keyAt(stageset.selectedItemPosition)]

                        for(i in 0 until resmaplist.size()) {
                            resmapname.add(StaticStore.mapnames[index][resmaplist.keyAt(i)])
                            resposition.add(resmaplist.keyAt(i))
                        }

                        val mapListAdapter = MapListAdapter(activity, resmapname.toTypedArray(), filter.keyAt(stageset.selectedItemPosition),resposition)
                        maplist.adapter = mapListAdapter

                        maplist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                            if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                            StaticStore.maplistClick = SystemClock.elapsedRealtime()
                            val intent = Intent(activity, StageList::class.java)

                            intent.putExtra("mapcode", filter.keyAt(stageset.selectedItemPosition))
                            intent.putExtra("stid", resposition[position])
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
        return if (num in 0..9) "00$num" else if (num in 10..99) "0$num" else "" + num
    }

    private fun withID(id: Int, name: String): String {
        return if (name == "") {
            number(id)
        } else {
            number(id) + " - " + name
        }
    }

}