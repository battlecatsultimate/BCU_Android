package com.mandarin.bcu.androidutil.unit.asynchs

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.UnitInfo
import com.mandarin.bcu.androidutil.FilterEntity
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.unit.Definer
import com.mandarin.bcu.androidutil.unit.adapters.UnitListAdapter
import common.system.MultiLangCont
import common.util.pack.Pack
import java.lang.ref.WeakReference
import java.util.*

class Adder(context: Activity) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(context)
    private var numbers = ArrayList<Int>()
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val list = activity.findViewById<ListView>(R.id.unitinflist)
        val search: FloatingActionButton = activity.findViewById(R.id.animsch)
        val schname: TextInputEditText = activity.findViewById(R.id.animschname)
        val layout: TextInputLayout = activity.findViewById(R.id.animschnamel)
        list.visibility = View.GONE
        search.hide()
        schname.visibility = View.GONE
        layout.visibility = View.GONE
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)
        publishProgress(0)
        if (StaticStore.names == null) {
            StaticStore.names = arrayOfNulls(StaticStore.unitnumber)
            for (i in StaticStore.names.indices) {
                StaticStore.names[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist[i].forms[0]) ?: "")
            }
        }
        publishProgress(2)
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val activity = weakReference.get() ?: return
        val ulistst = activity.findViewById<TextView>(R.id.unitinfst)
        when (values[0]) {
            0 -> ulistst.setText(R.string.unit_list_unitname)
            1 -> ulistst.setText(R.string.unit_list_unitic)
            2 -> {
                val list = activity.findViewById<ListView>(R.id.unitinflist)
                val filterEntity: FilterEntity
                val schname: TextInputEditText = activity.findViewById(R.id.animschname)
                filterEntity = if (Objects.requireNonNull(schname.text).toString().isEmpty()) FilterEntity(StaticStore.unitnumber) else FilterEntity(StaticStore.unitnumber, schname.text.toString())
                numbers = filterEntity.setFilter()
                val names = ArrayList<String>()
                for (i in numbers) {
                    names.add(StaticStore.names[i])
                }
                val adap = UnitListAdapter(activity, names.toTypedArray(), numbers)
                list.adapter = adap
                list.onItemLongClickListener = OnItemLongClickListener { _, _, position, _ ->
                    StaticStore.showShortMessage(activity, showName(numbers[position]))
                    list.isClickable = false
                    true
                }
                list.onItemClickListener = OnItemClickListener { _, _, position, _ ->
                    if (SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                    StaticStore.unitinflistClick = SystemClock.elapsedRealtime()
                    val result = Intent(activity, UnitInfo::class.java)
                    result.putExtra("ID", numbers[position])
                    activity.startActivity(result)
                }
                schname.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        val filterEntity1 = FilterEntity(StaticStore.unitnumber, s.toString())
                        numbers = filterEntity1.setFilter()
                        val names1 = ArrayList<String>()
                        for (i in numbers) {
                            names1.add(StaticStore.names[i])
                        }
                        val adap1 = UnitListAdapter(activity, names1.toTypedArray(), numbers)
                        list.adapter = adap1
                    }
                })
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get()
        super.onPostExecute(result)
        if (activity == null) return
        val list = activity.findViewById<ListView>(R.id.unitinflist)
        val prog = activity.findViewById<ProgressBar>(R.id.unitinfprog)
        val ulistst = activity.findViewById<TextView>(R.id.unitinfst)
        val search: FloatingActionButton = activity.findViewById(R.id.animsch)
        val schname: TextInputEditText = activity.findViewById(R.id.animschname)
        val layout: TextInputLayout = activity.findViewById(R.id.animschnamel)
        list.visibility = View.VISIBLE
        prog.visibility = View.GONE
        ulistst.visibility = View.GONE
        search.show()
        schname.visibility = View.VISIBLE
        layout.visibility = View.VISIBLE
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
                num.toString()
            }
        }
    }

    private fun showName(location: Int): String {
        val names = ArrayList<String>()
        for (f in StaticStore.units[location].forms) {
            var name = MultiLangCont.FNAME.getCont(f)
            if (name == null) name = ""
            names.add(name)
        }
        val result = StringBuilder(withID(location, names[0]))
        for (i in 1 until names.size) {
            result.append(" - ").append(names[i])
        }
        return result.toString()
    }

    private fun withID(id: Int, name: String): String {
        return if (name == "") {
            number(id)
        } else {
            number(id) + " - " + name
        }
    }

}