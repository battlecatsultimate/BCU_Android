package com.mandarin.bcu.androidutil.unit.asynchs

import android.app.Activity
import android.os.AsyncTask
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager
import com.mandarin.bcu.androidutil.unit.Definer
import com.mandarin.bcu.androidutil.unit.adapters.UnitListPager
import common.system.MultiLangCont
import common.util.pack.Pack
import java.lang.ref.WeakReference

class Adder(context: Activity, private val fm : FragmentManager?) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(context)

    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val tab = activity.findViewById<TabLayout>(R.id.unittab)
        val pager = activity.findViewById<MeasureViewPager>(R.id.unitpager)
        val search: FloatingActionButton = activity.findViewById(R.id.animsch)
        val schname: TextInputEditText = activity.findViewById(R.id.animschname)
        val layout: TextInputLayout = activity.findViewById(R.id.animschnamel)
        tab.visibility = View.GONE
        pager.visibility = View.GONE
        search.hide()
        schname.visibility = View.GONE
        layout.visibility = View.GONE
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)

        publishProgress(2)
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val activity = weakReference.get() ?: return
        val ulistst = activity.findViewById<TextView>(R.id.unitinfst)
        when (values[0]) {
            1 -> ulistst.setText(R.string.unit_list_unitic)
            2 -> {
                val schname: TextInputEditText = activity.findViewById(R.id.animschname)
                val tab = activity.findViewById<TabLayout>(R.id.unittab)
                val pager = activity.findViewById<MeasureViewPager>(R.id.unitpager)

                if(StaticStore.entityname != "") {
                    schname.setText(StaticStore.entityname)
                }

                schname.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        StaticStore.entityname = s.toString()

                        for(i in StaticStore.filterUnitList.indices) {
                            StaticStore.filterUnitList[i] = true
                        }
                    }
                })

                fm ?: return

                pager.removeAllViewsInLayout()
                pager.adapter = UnitListTab(fm)
                pager.offscreenPageLimit = Pack.map.keys.size
                pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

                tab.setupWithViewPager(pager)
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get()
        super.onPostExecute(result)
        if (activity == null) return
        val tab = activity.findViewById<TabLayout>(R.id.unittab)
        val pager = activity.findViewById<MeasureViewPager>(R.id.unitpager)
        val prog = activity.findViewById<ProgressBar>(R.id.unitinfprog)
        val search: FloatingActionButton = activity.findViewById(R.id.animsch)
        val schname: TextInputEditText = activity.findViewById(R.id.animschname)
        val layout: TextInputLayout = activity.findViewById(R.id.animschnamel)
        val loadt = activity.findViewById<TextView>(R.id.unitinfst)
        loadt.visibility = View.GONE
        tab.visibility = View.VISIBLE
        pager.visibility = View.VISIBLE
        prog.visibility = View.GONE
        search.show()
        schname.visibility = View.VISIBLE
        layout.visibility = View.VISIBLE
    }

    inner class UnitListTab internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        init {
            val lit = fm.fragments
            val trans = fm.beginTransaction()

            for(f in lit) {
                trans.remove(f)
            }

            trans.commitAllowingStateLoss()
        }

        private val keys = Pack.map.keys.toMutableList()

        override fun getItem(position: Int): Fragment {
            return UnitListPager.newInstance(keys[position], position)
        }

        override fun getCount(): Int {
            return Pack.map.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val keys = Pack.map.keys.toMutableList()

            return if(position == 0) {
                "Default"
            } else {
                val pack = Pack.map[keys[position]]

                if(pack == null) {
                    keys[position].toString()
                }

                val name = pack?.name ?: ""

                if(name.isEmpty()) {
                    keys[position].toString()
                } else {
                    name
                }
            }
        }

        override fun saveState(): Parcelable? {
            return null
        }
    }

}