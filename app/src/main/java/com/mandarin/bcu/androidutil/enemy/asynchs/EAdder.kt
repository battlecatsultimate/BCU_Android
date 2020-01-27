package com.mandarin.bcu.androidutil.enemy.asynchs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.EnemyInfo
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.FilterEntity
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.EDefiner
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyListAdapter
import common.system.MultiLangCont
import java.lang.ref.WeakReference
import java.util.*

class EAdder(activity: Activity, private val enemnumber: Int) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private var numbers = ArrayList<Int>()
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val listView = activity.findViewById<ListView>(R.id.enlist)
        listView.visibility = View.GONE
        val search: FloatingActionButton = activity.findViewById(R.id.enlistsch)
        search.hide()
        val schname: TextInputEditText = activity.findViewById(R.id.enemlistschname)
        schname.visibility = View.GONE
        val schnamel: TextInputLayout = activity.findViewById(R.id.enemlistschnamel)
        schnamel.visibility = View.GONE
        val back: FloatingActionButton = activity.findViewById(R.id.enlistbck)
        back.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                StaticStore.filterReset()
                activity.finish()
            }
        })
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        EDefiner().define(activity)
        publishProgress(0)
        if (StaticStore.enames == null) {
            StaticStore.enames = arrayOfNulls(StaticStore.emnumber)
            for (i in 0 until StaticStore.emnumber) {
                StaticStore.enames[i] = withID(i, MultiLangCont.ENAME.getCont(StaticStore.enemies[i]) ?: "")
            }
        }
        publishProgress(2)
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg results: Int?) {
        val activity = weakReference.get() ?: return
        val enlistst = activity.findViewById<TextView>(R.id.enlistst)
        when (results[0]) {
            0 -> enlistst.setText(R.string.stg_info_enemname)
            1 -> enlistst.setText(R.string.stg_info_enemimg)
            2 -> {
                val list = activity.findViewById<ListView>(R.id.enlist)
                val filterEntity: FilterEntity
                val schname: TextInputEditText = activity.findViewById(R.id.enemlistschname)
                filterEntity = if (Objects.requireNonNull(schname.text).toString().isEmpty()) FilterEntity(enemnumber) else FilterEntity(enemnumber, schname.text.toString())
                numbers = filterEntity.eSetFilter()
                val names = ArrayList<String>()
                for (i in numbers) names.add(StaticStore.enames[i])
                val enemy = EnemyListAdapter(activity, names.toTypedArray(), numbers)
                list.adapter = enemy
                list.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                    StaticStore.enemyinflistClick = SystemClock.elapsedRealtime()
                    val result = Intent(activity, EnemyInfo::class.java)
                    result.putExtra("ID", numbers[position])
                    activity.startActivity(result)
                }
                schname.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        val filterEntity1 = FilterEntity(enemnumber, s.toString())
                        numbers = filterEntity1.eSetFilter()
                        val names1 = ArrayList<String>()
                        for (i in numbers) names1.add(StaticStore.enames[i])
                        val enemy1 = EnemyListAdapter(activity, names1.toTypedArray(), numbers)
                        list.adapter = enemy1
                        if (s.toString().isEmpty()) {
                            schname.setCompoundDrawablesWithIntrinsicBounds(null, null, activity.getDrawable(R.drawable.search), null)
                        } else {
                            schname.setCompoundDrawablesWithIntrinsicBounds(null, null, activity.getDrawable(R.drawable.ic_close_black_24dp), null)
                        }
                    }
                })
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        super.onPostExecute(result)
        val enlistst = activity.findViewById<TextView>(R.id.enlistst)
        enlistst.visibility = View.GONE
        val list = activity.findViewById<ListView>(R.id.enlist)
        list.visibility = View.VISIBLE
        val prog = activity.findViewById<ProgressBar>(R.id.enlistprog)
        prog.visibility = View.GONE
        val search: FloatingActionButton = activity.findViewById(R.id.enlistsch)
        search.show()
        val schname: TextInputEditText = activity.findViewById(R.id.enemlistschname)
        schname.visibility = View.VISIBLE
        val schnamel: TextInputLayout = activity.findViewById(R.id.enemlistschnamel)
        schnamel.visibility = View.VISIBLE
    }

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                "" + num
            }
        }
    }

    private fun withID(id: Int, name: String): String {
        return if (name == "") {
            number(id)
        } else {
            number(id) + " - " + name
        }
    }

}