package com.mandarin.bcu.androidutil.stage.asynchs

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.SystemClock
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import com.mandarin.bcu.R
import com.mandarin.bcu.StageList
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.enemy.EDefiner
import com.mandarin.bcu.androidutil.stage.MapDefiner
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter
import com.mandarin.bcu.androidutil.unit.Definer
import common.system.MultiLangCont
import common.system.files.VFile
import java.lang.ref.WeakReference
import java.util.*

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
                stageset.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val mapListAdapter = MapListAdapter(activity, StaticStore.mapnames[position], StaticStore.MAPCODE[position])
                        maplist.adapter = mapListAdapter
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                val mapListAdapter = MapListAdapter(activity, StaticStore.mapnames[stageset.selectedItemPosition], StaticStore.MAPCODE[stageset.selectedItemPosition])
                maplist.adapter = mapListAdapter
                maplist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL) return@OnItemClickListener
                    StaticStore.maplistClick = SystemClock.elapsedRealtime()
                    val intent = Intent(activity, StageList::class.java)
                    intent.putExtra("mapcode", StaticStore.MAPCODE[stageset.selectedItemPosition])
                    intent.putExtra("stid", position)
                    activity.startActivity(intent)
                }
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