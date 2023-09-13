package com.mandarin.bcu.androidutil.lineup.adapters

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.lineup.LineUpView
import common.battle.BasisSet
import common.pack.Identifier
import common.pack.UserProfile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.Combo


class LUCatCombo : Fragment() {
    private var posit = -1
    private lateinit var line: LineUpView
    private val schid = intArrayOf(R.string.combo_str, R.string.combo_abil, R.string.combo_bscan, R.string.combo_mon, R.string.combo_env)
    private val locater = arrayOf(intArrayOf(0, 1, 2), intArrayOf(14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), intArrayOf(3, 6, 7, 10), intArrayOf(5, 4, 8, 9), intArrayOf(11, 12, 13))
    private val locateid = arrayOf(intArrayOf(R.string.combo_atk, R.string.combo_hp, R.string.combo_spd), intArrayOf(R.string.combo_strag, R.string.combo_md, R.string.combo_res, R.string.combo_kbdis, R.string.combo_sl, R.string.combo_st, R.string.combo_wea, R.string.combo_inc, R.string.combo_wit, R.string.combo_eva, R.string.combo_crit), intArrayOf(R.string.combo_caninch, R.string.combo_canatk, R.string.combo_canchtime, R.string.combo_bsh), intArrayOf(R.string.combo_initmon, R.string.combo_work, R.string.combo_efficiency, R.string.combo_wal), intArrayOf(R.string.combo_cd, R.string.combo_ac, R.string.combo_xp))
    private val sch = arrayOfNulls<String>(schid.size)
    private var comboListAdapter: ComboListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.lineup_cat_combo, group, false)

        if (context == null)
            return view

        StaticStore.combos.clear()

        StaticStore.combos.addAll(UserProfile.getBCData().combos.list)

        for(userPack in UserProfile.getUserPacks()) {
            for(combo in userPack.combos.list) {
                combo ?: continue

                StaticStore.combos.add(combo)
            }
        }

        StaticStore.combos.sortWith(Comparator.comparingInt(Combo::type).thenComparingInt(Combo::lv))

        val names = Array<String>(StaticStore.combos.size) {
            if(StaticStore.combos[it].id.pack == Identifier.DEF) {
                MultiLangCont.getStatic().COMNAME.getCont(StaticStore.combos[it]) ?: Data.trio(StaticStore.combos[it].id.id)
            } else {
                StaticStore.combos[it].getName() ?: ""
            }
        }

        val combolist = view.findViewById<ListView>(R.id.combolist)
        val schlist = view.findViewById<ListView>(R.id.comschlist1)
        val schlist1 = view.findViewById<ListView>(R.id.comschlist2)
        val use = view.findViewById<Button>(R.id.combouse)

        for (i in schid.indices) {
            sch[i] = context?.getString(schid[i])
        }

        val subsch: MutableList<String> = ArrayList()
        val locates: MutableList<Int> = ArrayList()

        for (i in locater) {
            for (j in i) {
                locates.add(j)
            }
        }

        for (i in locater.indices) {
            for (element in locateid[i]) {
                subsch.add(context?.getString(element) ?: "")
            }
        }

        val adapter = ComboSchListAdapter(requireActivity(), sch, schlist1, combolist, comboListAdapter)
        val adapter1 = ComboSubSchListAdapter(requireActivity(), subsch, combolist, locates, comboListAdapter)

        schlist.adapter = adapter
        schlist1.adapter = adapter1

        comboListAdapter = ComboListAdapter(requireActivity(), names)

        combolist.adapter = comboListAdapter

        combolist.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            if (context == null)
                return@OnItemClickListener

            if (position >= StaticStore.combos.size)
                return@OnItemClickListener

            val c = StaticStore.combos[position]

            posit = position

            if (!BasisSet.current().sele.lu.contains(c)) {
                use.isClickable = true

                if (BasisSet.current().sele.lu.willRem(c)) {
                    use.setTextColor(Color.rgb(229, 57, 53))
                    use.setText(R.string.combo_rep)
                } else {
                    use.setTextColor(StaticStore.getAttributeColor(context, R.attr.TextPrimary))
                    use.setText(R.string.combo_use)
                }
            } else {
                use.setTextColor(StaticStore.getAttributeColor(context, R.attr.TextPrimary))
                use.setText(R.string.combo_using)

                use.isClickable = false
            }
        }

        use.setOnClickListener(View.OnClickListener {
            if (context == null)
                return@OnClickListener

            if (posit < 0)
                return@OnClickListener

            if (posit >= StaticStore.combos.size)
                return@OnClickListener

            val c = StaticStore.combos[posit]

            BasisSet.current().sele.lu.set(c.forms)

            line.updateLineUp()

            use.setText(R.string.combo_using)
            use.setTextColor(StaticStore.getAttributeColor(context, R.attr.TextPrimary))
            use.isClickable = false

            comboListAdapter?.notifyDataSetChanged()

            line.invalidate()
            line.updateUnitSetting()
            line.updateUnitOrb()
        })

        return view
    }

    fun setVariables(line: LineUpView) {
        this.line = line
    }

    companion object {
        @JvmStatic
        fun newInstance(line: LineUpView): LUCatCombo {
            val combo = LUCatCombo()

            combo.setVariables(line)

            return combo
        }
    }
}