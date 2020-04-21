package com.mandarin.bcu.androidutil.enemy.asynchs

import android.annotation.SuppressLint
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
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.EDefiner
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyListPager
import common.util.pack.Pack
import java.lang.ref.WeakReference

class EAdder(activity: Activity, private val mode: Int, private val fm: FragmentManager?) : AsyncTask<Void?, Int?, Void?>() {
    companion object {
        const val MODE_INFO = 0
        const val MODE_SELECTION = 1
    }

    private val weakReference: WeakReference<Activity> = WeakReference(activity)

    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val tab = activity.findViewById<TabLayout>(R.id.enlisttab)
        tab.visibility = View.GONE
        val pager = activity.findViewById<MeasureViewPager>(R.id.enlistpager)
        pager.visibility = View.GONE
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
                val tab = activity.findViewById<TabLayout>(R.id.enlisttab)
                val pager = activity.findViewById<MeasureViewPager>(R.id.enlistpager)
                val schname: TextInputEditText = activity.findViewById(R.id.enemlistschname)

                if(StaticStore.entityname != "") {
                    schname.setText(StaticStore.entityname)
                }

                schname.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable) {
                        StaticStore.entityname = s.toString()

                        for(i in StaticStore.filterEntityList.indices) {
                            StaticStore.filterEntityList[i] = true
                        }
                    }
                })

                fm ?: return

                pager.removeAllViewsInLayout()
                pager.adapter = EnemyListTab(fm)
                pager.offscreenPageLimit = Pack.map.size
                pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

                tab.setupWithViewPager(pager)
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        super.onPostExecute(result)
        if(Pack.map.size != 1) {
            val tab = activity.findViewById<TabLayout>(R.id.enlisttab)
            tab.visibility = View.VISIBLE
        }
        val pager = activity.findViewById<MeasureViewPager>(R.id.enlistpager)
        pager.visibility = View.VISIBLE
        val prog = activity.findViewById<ProgressBar>(R.id.enlistprog)
        prog.visibility = View.GONE
        val search: FloatingActionButton = activity.findViewById(R.id.enlistsch)
        search.show()
        val schname: TextInputEditText = activity.findViewById(R.id.enemlistschname)
        schname.visibility = View.VISIBLE
        val schnamel: TextInputLayout = activity.findViewById(R.id.enemlistschnamel)
        schnamel.visibility = View.VISIBLE
        val enlistst: TextView = activity.findViewById(R.id.enlistst)
        enlistst.visibility = View.GONE
    }

    inner class EnemyListTab internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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
            return EnemyListPager.newInstance(keys[position], position, mode)
        }

        override fun getCount(): Int {
            return Pack.map.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
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