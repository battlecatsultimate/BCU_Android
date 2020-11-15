package com.mandarin.bcu.androidutil.stage.coroutine

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.R
import com.mandarin.bcu.StageInfo
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.stage.adapters.CStageListAdapter
import com.mandarin.bcu.androidutil.stage.adapters.StageListAdapter
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import common.util.stage.StageMap
import java.lang.ref.WeakReference

class StageLoader(activity: Activity, private val data: Identifier<StageMap>, private val custom: Boolean) : CoroutineTask<String>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)

    private val done = "done"

    override fun prepare() {
        val activity = weakReference.get() ?: return

        val stglist = activity.findViewById<ListView>(R.id.stglist)

        stglist.visibility = View.GONE
    }

    override fun doSomething() {
        val ac = weakReference.get() ?: return

        Definer.define(ac, this::updateProg, this::updateText)

        Identifier.get(data) ?: return

        publishProgress(done)
    }

    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return

        val st = activity.findViewById<TextView>(R.id.status)

        when(data[0]) {
            StaticStore.TEXT -> st.text = data[1]
            StaticStore.PROG -> {
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                if(data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
            done -> {
                val stm = Identifier.get(this.data) ?: return

                val name = activity.findViewById<TextView>(R.id.stglistname)

                val stname = MultiLangCont.get(stm) ?: stm.name ?: Data.trio(this.data.id)

                name.text = stname

                val stageListAdapter: Any

                val stages = if(StaticStore.filter != null) {
                    val f = StaticStore.filter ?: return

                    val stmList = f[stm.cont.sid] ?: return

                    val stList = stmList[stm.id.id] ?: return

                    Array<Identifier<Stage>>(stList.size) {
                        stm.list.list[stList[it]].id
                    }
                } else {
                    Array<Identifier<Stage>>(stm.list.list.size) { i ->
                        stm.list.list[i].id
                    }
                }

                stageListAdapter = if(custom) {
                    CStageListAdapter(activity, stages)
                } else {
                    StageListAdapter(activity, stages)
                }

                val stglist = activity.findViewById<ListView>(R.id.stglist)
                stglist.adapter = stageListAdapter
                stglist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.stglistClick < StaticStore.INTERVAL) return@OnItemClickListener
                    StaticStore.stglistClick = SystemClock.elapsedRealtime()

                    val d: Identifier<Stage> = when (stglist.adapter) {
                        is CStageListAdapter -> {
                            (stglist.adapter as CStageListAdapter).getItem(position) ?: return@OnItemClickListener
                        }
                        is StageListAdapter -> {
                            (stglist.adapter as StageListAdapter).getItem(position) ?: return@OnItemClickListener
                        }
                        else -> {
                            return@OnItemClickListener
                        }
                    }

                    val intent = Intent(activity, StageInfo::class.java)
                    intent.putExtra("Data", JsonEncoder.encode(d).toString())
                    intent.putExtra("custom", custom)
                    activity.startActivity(intent)
                }
                val bck: FloatingActionButton = activity.findViewById(R.id.stglistbck)
                bck.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        activity.finish()
                    }
                })
            }
        }
    }

    override fun finish() {
        val activity = weakReference.get() ?: return
        val stglist = activity.findViewById<ListView>(R.id.stglist)
        val stgprog = activity.findViewById<ProgressBar>(R.id.prog)
        val stgst = activity.findViewById<TextView>(R.id.status)
        stglist.visibility = View.VISIBLE
        stgprog.visibility = View.GONE
        stgst.visibility = View.GONE
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }
}