package com.mandarin.bcu.androidutil.unit.coroutine

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import com.mandarin.bcu.androidutil.unit.adapters.UnitListPager
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import java.lang.ref.WeakReference

class Adder(context: Activity, private val fm : FragmentManager, private val lc: Lifecycle) : CoroutineTask<String>() {
    private val weakReference: WeakReference<Activity> = WeakReference(context)

    private val done = "done"

    override fun prepare() {
        val activity = weakReference.get() ?: return
        val tab = activity.findViewById<TabLayout>(R.id.unittab)
        val pager = activity.findViewById<ViewPager2>(R.id.unitpager)
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
                val pager = activity.findViewById<ViewPager2>(R.id.unitpager)
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

                pager.isSaveEnabled = false
                pager.isSaveFromParentEnabled = false

                pager.adapter = UnitListTab()
                pager.offscreenPageLimit = getExistingUnit()

                val keys = getExistingPack()

                TabLayoutMediator(tab, pager) { t, position ->
                    t.text = if(position == 0) {
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

                        name.ifEmpty {
                            keys[position]
                        }
                    }
                }.attach()
            }
        }
    }

    override fun finish() {
        val activity = weakReference.get() ?: return

        val tab = activity.findViewById<TabLayout>(R.id.unittab)
        val pager = activity.findViewById<ViewPager2>(R.id.unitpager)
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

    inner class UnitListTab : FragmentStateAdapter(fm, lc) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return UnitListPager.newInstance(keys[position], position)
        }
    }
}