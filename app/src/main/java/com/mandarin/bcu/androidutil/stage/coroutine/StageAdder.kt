package com.mandarin.bcu.androidutil.stage.coroutine

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.BattlePrepare
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.stage.adapters.EnemyListRecycle
import com.mandarin.bcu.androidutil.stage.adapters.StageRecycle
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import java.lang.ref.WeakReference

open class StageAdder(activity: Activity, private val data: Identifier<Stage>) : CoroutineTask<String>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)

    private val done = "done"

    override fun prepare() {
        val activity = weakReference.get() ?: return
        val scrollView = activity.findViewById<ScrollView>(R.id.stginfoscroll)
        scrollView.visibility = View.GONE
    }

    override fun doSomething() {
        val ac = weakReference.get() ?: return

        Definer.define(ac, this::updateProg, this::updateText)

        publishProgress(done)
    }

    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return
        val st = activity.findViewById<TextView>(R.id.status)
        when (data[0]) {
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
                st.setText(R.string.stg_info_loadfilt)

                val title = activity.findViewById<TextView>(R.id.stginfoname)
                val stage = Identifier.get(this.data) ?: return
                val battle = activity.findViewById<Button>(R.id.battlebtn)
                val stgrec: RecyclerView = activity.findViewById(R.id.stginforec)
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                prog.isIndeterminate = true

                stgrec.layoutManager = LinearLayoutManager(activity)
                ViewCompat.setNestedScrollingEnabled(stgrec, false)

                val stageRecycle = StageRecycle(activity, this.data)

                stgrec.adapter = stageRecycle

                battle.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val intent = Intent(activity, BattlePrepare::class.java)
                        intent.putExtra("Data", JsonEncoder.encode(this@StageAdder.data).toString())
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

    override fun finish() {
        val activity = weakReference.get() ?: return
        val scrollView = activity.findViewById<ScrollView>(R.id.stginfoscroll)
        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        val st = activity.findViewById<TextView>(R.id.status)
        scrollView.visibility = View.VISIBLE
        prog.visibility = View.GONE
        st.visibility = View.GONE
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
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