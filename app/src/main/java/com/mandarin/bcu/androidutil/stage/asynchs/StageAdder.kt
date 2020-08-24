package com.mandarin.bcu.androidutil.stage.asynchs

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.BattlePrepare
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.stage.adapters.EnemyListRecycle
import com.mandarin.bcu.androidutil.stage.adapters.StageRecycle
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.pack.Background
import common.util.stage.Stage
import java.lang.ref.WeakReference

open class StageAdder(activity: Activity, private val data: Identifier<Stage>, private val custom: Boolean) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val scrollView = activity.findViewById<ScrollView>(R.id.stginfoscroll)
        scrollView.visibility = View.GONE
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val ac = weakReference.get() ?: return null

        if (StaticStore.treasure == null)
            StaticStore.readTreasureIcon(ac)

        publishProgress(0)

        if (StaticStore.bgread == 0) {
            Background.read()
            StaticStore.bgread = 1
        }

        publishProgress(1)

        return null
    }

    override fun onProgressUpdate(vararg results: Int?) {
        val activity = weakReference.get() ?: return
        val st = activity.findViewById<TextView>(R.id.stginfost)
        when (results[0]) {
            0 -> {
                st.setText(R.string.stg_info_loadbg)
            }
            1 -> {
                st.setText(R.string.stg_info_loadfilt)
                val title = activity.findViewById<TextView>(R.id.stginfoname)
                val stage = Identifier.get(data) ?: return
                val battle = activity.findViewById<Button>(R.id.battlebtn)
                val stgrec: RecyclerView = activity.findViewById(R.id.stginforec)
                stgrec.layoutManager = LinearLayoutManager(activity)
                ViewCompat.setNestedScrollingEnabled(stgrec, false)
                val stageRecycle = StageRecycle(activity, data)
                stgrec.adapter = stageRecycle
                battle.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(activity, BattlePrepare::class.java)
                        intent.putExtra("Data", JsonEncoder.encode(data).toString())
                        val manager = stgrec.layoutManager
                        if (manager != null) {
                            val row = manager.findViewByPosition(0)
                            if (row != null) {
                                val star = row.findViewById<Spinner>(R.id.stginfostarr)
                                if (star != null) intent.putExtra("selection", star.selectedItemPosition)
                            }
                        }
                        activity.startActivity(intent)
                    }
                })
                title.text = MultiLangCont.get(stage) ?: stage.name ?: getStageName(stage.id.id)
                val stgscroll = activity.findViewById<ScrollView>(R.id.stginfoscroll)
                stgscroll.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
                stgscroll.isFocusable = false
                stgscroll.isFocusableInTouchMode = true
                val stgen: RecyclerView = activity.findViewById(R.id.stginfoenrec)
                stgen.layoutManager = LinearLayoutManager(activity)
                ViewCompat.setNestedScrollingEnabled(stgen, false)
                val enemyListRecycle = EnemyListRecycle(activity, stage)
                stgen.adapter = enemyListRecycle
                if(stage.data.allEnemy.isEmpty()) {
                    stgen.visibility = View.GONE
                }
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        val scrollView = activity.findViewById<ScrollView>(R.id.stginfoscroll)
        val prog = activity.findViewById<ProgressBar>(R.id.stginfoprog)
        val st = activity.findViewById<TextView>(R.id.stginfost)
        scrollView.visibility = View.VISIBLE
        prog.visibility = View.GONE
        st.visibility = View.GONE
    }

    private fun getStageName(posit: Int) : String {
        return "Stage"+number(posit)
    }

    private fun number(n: Int) : String {
        return when (n) {
            in 0..9 -> {
                "00$n"
            }
            in 10..99 -> {
                "0$n"
            }
            else -> {
                "$n"
            }
        }
    }

}