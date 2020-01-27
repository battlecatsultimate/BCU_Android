package com.mandarin.bcu.androidutil.stage.asynchs

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.R
import com.mandarin.bcu.StageInfo
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.stage.adapters.StageListAdapter
import common.system.MultiLangCont
import java.lang.ref.WeakReference

class StageLoader(activity: Activity, private val mapcode: Int, private val stid: Int) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val stglist = activity.findViewById<ListView>(R.id.stglist)
        stglist.visibility = View.GONE
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        weakReference.get() ?: return null
        StaticStore.map[mapcode] ?: return null
        publishProgress(0)
        return null
    }

    override fun onProgressUpdate(vararg result: Int?) {
        val activity = weakReference.get() ?: return
        val mc = StaticStore.map[mapcode] ?: return
        val stm = mc.maps[stid]
        val stages = arrayOfNulls<String>(stm.list.size)
        for (i in stages.indices) {
            stages[i] = MultiLangCont.STNAME.getCont(stm.list[i])
        }
        val stageListAdapter = StageListAdapter(activity, stages, mapcode, stid)
        val stglist = activity.findViewById<ListView>(R.id.stglist)
        stglist.adapter = stageListAdapter
        stglist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (SystemClock.elapsedRealtime() - StaticStore.stglistClick < StaticStore.INTERVAL) return@OnItemClickListener
            StaticStore.stglistClick = SystemClock.elapsedRealtime()
            val intent = Intent(activity, StageInfo::class.java)
            intent.putExtra("mapcode", mapcode)
            intent.putExtra("stid", stid)
            intent.putExtra("posit", position)
            activity.startActivity(intent)
        }
        val bck: FloatingActionButton = activity.findViewById(R.id.stglistbck)
        bck.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                activity.finish()
            }
        })
    }

    override fun onPostExecute(results: Void?) {
        val activity = weakReference.get() ?: return
        val stglist = activity.findViewById<ListView>(R.id.stglist)
        val stgprog = activity.findViewById<ProgressBar>(R.id.stglistprog)
        val stgst = activity.findViewById<TextView>(R.id.stglistst)
        stglist.visibility = View.VISIBLE
        stgprog.visibility = View.GONE
        stgst.visibility = View.GONE
    }

}