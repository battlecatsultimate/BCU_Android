package com.mandarin.bcu.androidutil.unit.coroutine

import android.app.Activity
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.MeasureViewPager
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import com.mandarin.bcu.androidutil.unit.adapters.UnitListPager
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import java.lang.ref.WeakReference

class Adder(context: Activity, private val fm : FragmentManager?) : CoroutineTask<String>() {
    private val weakReference: WeakReference<Activity> = WeakReference(context)

    private val done = "done"

    override fun prepare() {
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

    override fun doSomething() {
        val activity = weakReference.get() ?: return

        Definer.define(activity, this::updateProg, this::updateText)
        
        publishProgress("1")

        StaticStore.filterEntityList = BooleanArray(UserProfile.getAllPacks().size)

        publishProgress(done)
    }

    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return

        val ulistst = activity.findViewById<TextView>(R.id.status)

        when (data[0]) {
            StaticStore.TEXT -> ulistst.text = data[1]
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
                val schname: TextInputEditText = activity.findViewById(R.id.animschname)
                val tab = activity.findViewById<TabLayout>(R.id.unittab)
                val pager = activity.findViewById<MeasureViewPager>(R.id.unitpager)
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
                pager.adapter = UnitListTab(fm)
                pager.offscreenPageLimit = getExistingUnit()
                pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

                tab.setupWithViewPager(pager)
            }
        }
    }

    override fun finish() {
        val activity = weakReference.get() ?: return
        val tab = activity.findViewById<TabLayout>(R.id.unittab)
        val pager = activity.findViewById<MeasureViewPager>(R.id.unitpager)
        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        val search: FloatingActionButton = activity.findViewById(R.id.animsch)
        val schname: TextInputEditText = activity.findViewById(R.id.animschname)
        val layout: TextInputLayout = activity.findViewById(R.id.animschnamel)
        val loadt = activity.findViewById<TextView>(R.id.status)
        loadt.visibility = View.GONE
        if(getExistingUnit() != 1) {
            tab.visibility = View.VISIBLE
        } else {
            val collapse = activity.findViewById<CollapsingToolbarLayout>(R.id.animcollapse)

            val param = collapse.layoutParams as AppBarLayout.LayoutParams

            param.scrollFlags = 0

            collapse.layoutParams = param
        }
        pager.visibility = View.VISIBLE
        prog.visibility = View.GONE
        search.show()
        schname.visibility = View.VISIBLE
        layout.visibility = View.VISIBLE
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
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

        override fun getPageTitle(position: Int): CharSequence {
            return if(position == 0) {
                weakReference.get()?.getString(R.string.pack_default) ?: "Default"
            } else {
                val pack = PackData.getPack(keys[position])

                if(pack == null) {
                    keys[position]
                }

                val name = when (pack) {
                    is PackData.DefPack -> {
                        weakReference.get()?.getString(R.string.pack_default) ?: "Default"
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
                if(p.units.list.isNotEmpty()) {
                    if(p is PackData.DefPack) {
                        res.add(Identifier.DEF)
                    } else if(p is PackData.UserPack) {
                        res.add(p.sid)
                    }
                }
            }

            return res
        }
    }
}