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
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyListPager
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import java.lang.ref.WeakReference

class EAdder(activity: Activity, private val mode: Int, private val fm: FragmentManager?) : AsyncTask<Void, String, Void>() {
    companion object {
        const val MODE_INFO = 0
        const val MODE_SELECTION = 1
    }

    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    
    private val done = "2"

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

        Definer.define(activity, this::updateProg, this::updateText)
        
        publishProgress(done)

        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg results: String) {
        val activity = weakReference.get() ?: return
        val enlistst = activity.findViewById<TextView>(R.id.status)
        when (results[0]) {
            StaticStore.TEXT -> enlistst.text = results[1]
            StaticStore.PROG -> {
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                if(results[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = results[1].toInt()
            }
            done -> {
                val tab = activity.findViewById<TabLayout>(R.id.enlisttab)
                val pager = activity.findViewById<MeasureViewPager>(R.id.enlistpager)
                val schname: TextInputEditText = activity.findViewById(R.id.enemlistschname)
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                prog.isIndeterminate = true

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
                pager.offscreenPageLimit = UserProfile.getAllPacks().size
                pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

                tab.setupWithViewPager(pager)
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        super.onPostExecute(result)
        if(UserProfile.getAllPacks().size != 1) {
            val tab = activity.findViewById<TabLayout>(R.id.enlisttab)
            tab.visibility = View.VISIBLE
        }
        val pager = activity.findViewById<MeasureViewPager>(R.id.enlistpager)
        pager.visibility = View.VISIBLE
        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        prog.visibility = View.GONE
        val search: FloatingActionButton = activity.findViewById(R.id.enlistsch)
        search.show()
        val schname: TextInputEditText = activity.findViewById(R.id.enemlistschname)
        schname.visibility = View.VISIBLE
        val schnamel: TextInputLayout = activity.findViewById(R.id.enemlistschnamel)
        schnamel.visibility = View.VISIBLE
        val enlistst: TextView = activity.findViewById(R.id.status)
        enlistst.visibility = View.GONE
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }

    inner class EnemyListTab internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val keys: ArrayList<String>

        init {
            val lit = fm.fragments
            val trans = fm.beginTransaction()

            for(f in lit) {
                trans.remove(f)
            }

            trans.commitAllowingStateLoss()

            keys = getExistingPack()
        }

        override fun getItem(position: Int): Fragment {
            return EnemyListPager.newInstance(keys[position], position, mode)
        }

        override fun getCount(): Int {
            return keys.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if(position == 0) {
                "Default"
            } else {
                val pack = UserProfile.getPack(keys[position])

                if(pack == null) {
                    keys[position]
                }

                val name = when (pack) {
                    is PackData.DefPack -> {
                        weakReference.get()?.getString(R.string.pack_default) ?: ""
                    }
                    is PackData.UserPack -> {
                        pack.desc.name
                    }
                    else -> {
                        ""
                    }
                }

                if(name.isEmpty()) {
                    keys[position]
                } else {
                    name
                }
            }
        }

        override fun saveState(): Parcelable? {
            return null
        }

        private fun getExistingPack() : ArrayList<String> {
            val packs = UserProfile.getAllPacks()

            val res = ArrayList<String>()

            for(p in packs) {
                if(p.enemies.list.isNotEmpty()) {
                    if(p is PackData.DefPack) {
                        res.add(Identifier.DEF)
                    } else if(p is PackData.UserPack) {
                        res.add(p.desc.id)
                    }
                }
            }

            return res
        }
    }
}