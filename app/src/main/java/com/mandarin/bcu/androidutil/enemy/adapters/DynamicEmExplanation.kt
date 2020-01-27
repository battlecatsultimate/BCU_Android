package com.mandarin.bcu.androidutil.enemy.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.system.MultiLangCont

class DynamicEmExplanation(private val activity: Activity, private val id: Int) : PagerAdapter() {
    private val txid = intArrayOf(R.id.enemyex0, R.id.enemyex1, R.id.enemyex2, R.id.enemyex3)
    override fun instantiateItem(group: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(activity)
        val layout = inflater.inflate(R.layout.enemy_explanation, group, false) as ViewGroup
        val title = layout.findViewById<TextView>(R.id.enemyexname)
        var name = MultiLangCont.ENAME.getCont(StaticStore.enemies[id])
        if (name == null) name = ""
        title.text = name
        val exps = arrayOfNulls<TextView>(4)
        for (i in txid.indices) exps[i] = layout.findViewById(txid[i])
        var explanation = MultiLangCont.EEXP.getCont(StaticStore.enemies[id])
        if (explanation == null) explanation = arrayOf("", "", "", "")
        for (i in exps.indices) {
            if (i >= explanation.size) exps[i]!!.text = "" else exps[i]!!.text = explanation[i]
        }
        exps[3]!!.setPadding(0, 0, 0, StaticStore.dptopx(24f, activity))
        group.addView(layout)
        return layout
    }

    override fun getCount(): Int {
        return 1
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }

}