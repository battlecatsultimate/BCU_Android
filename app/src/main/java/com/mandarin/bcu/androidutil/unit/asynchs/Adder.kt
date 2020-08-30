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
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.unit.adapters.UnitListPager
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import java.lang.ref.WeakReference

class Adder(context: Activity, private val fm : FragmentManager?) : AsyncTask<Void?, String?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(context)

    private val image = "3"
    private val castle = "4"
    private val bg = "5"
    private val packext = "6"

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

        Definer.define(activity)
        
        publishProgress("1")

        StaticStore.filterEntityList = BooleanArray(UserProfile.getAllPacks().size)

        publishProgress("2")
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        val activity = weakReference.get() ?: return
        val ulistst = activity.findViewById<TextView>(R.id.unitinfst)
        when (values[0]) {
            "1" -> ulistst.setText(R.string.main_pack)
            "2" -> {
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

                        for(i in StaticStore.filterEntityList.indices) {
                            StaticStore.filterEntityList[i] = true
                        }
                    }
                })

                fm ?: return

                pager.removeAllViewsInLayout()
                pager.adapter = UnitListTab(fm)
                pager.offscreenPageLimit = getExistingUnit()
                pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

                tab.setupWithViewPager(pager)
            }

            image -> {
                val name = activity.getString(R.string.main_pack_img)+ (values[1] ?: "")

                ulistst.text = name
            }

            bg -> {
                val name = activity.getString(R.string.main_pack_bg) + (values[1] ?: "")

                ulistst.text = name
            }

            castle -> {
                val name = activity.getString(R.string.main_pack_castle) + (values[1] ?: "")

                ulistst.text = name
            }

            packext -> {
                val name = activity.getString(R.string.main_pack_ext)+ (values[1] ?: "")

                ulistst.text = name
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
        if(UserProfile.getAllPacks().size != 1) {
            tab.visibility = View.VISIBLE
        }
        pager.visibility = View.VISIBLE
        prog.visibility = View.GONE
        search.show()
        schname.visibility = View.VISIBLE
        layout.visibility = View.VISIBLE
    }

    private fun getExistingUnit() : Int {
        var i = 0

        for(p in UserProfile.getAllPacks()) {
            if(p.units.list.isNotEmpty())
                i++
        }

        return i
    }

    inner class UnitListTab internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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
            return UnitListPager.newInstance(keys[position], position)
        }

        override fun getCount(): Int {
            return keys.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if(position == 0) {
                "Default"
            } else {
                val pack = PackData.getPack(keys[position])

                if(pack == null) {
                    keys[position]
                }

                val name = when (pack) {
                    is PackData.DefPack -> {
                        weakReference.get()?.getString(R.string.pack_default) ?: ""
                    }
                    is PackData.UserPack -> {
                        StaticStore.getPackName(pack.desc.id)
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
                if(p is PackData.DefPack) {
                    res.add(Identifier.DEF)
                } else if(p is PackData.UserPack) {
                    res.add(p.desc.id)
                }
            }

            return res
        }
    }
}