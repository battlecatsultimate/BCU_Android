package com.mandarin.bcu.androidutil.unit.adapters

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.UnitInfo
import com.mandarin.bcu.androidutil.FilterEntity
import com.mandarin.bcu.androidutil.StaticStore
import common.system.MultiLangCont
import common.util.Data
import common.util.pack.Pack
import java.util.*
import kotlin.collections.ArrayList

class UnitListPager : Fragment() {
    private var pid = 0
    private var position = 0

    companion object {
        fun newInstance(pid: Int, position: Int) : UnitListPager {
            val ulp = UnitListPager()
            val bundle = Bundle()

            bundle.putInt("pid", pid)
            bundle.putInt("position", position)

            ulp.arguments = bundle

            return ulp
        }
    }

    private var destroyed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getInt("pid") ?: 0
        position = arguments?.getInt("position") ?: 0

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        validate(nores, list)

        val handler = Handler()

        val runnable = object : Runnable {
            override fun run() {
                if(position >= StaticStore.filterUnitList.size)
                    return

                if(StaticStore.filterUnitList[position]) {
                    validate(nores,list)
                    println(position)
                    StaticStore.filterUnitList[position] = false
                }

                if(!destroyed) {
                    handler.postDelayed(this, 100)
                }
            }
        }

        runnable.run()

        return view
    }

    private fun validate(nores: TextView, list: ListView) {
        val p = Pack.map[pid] ?: return

        val filterEntity = FilterEntity(p.us.ulist.size(), StaticStore.entityname, pid)

        val numbers = filterEntity.setFilter()

        if(numbers.isNotEmpty()) {
            nores.visibility = View.GONE
            list.visibility = View.VISIBLE

            val names = ArrayList<String>()

            for(i in numbers) {
                if(p.us.ulist.list[i] == null)
                    continue

                val name = if(pid == 0) {
                    MultiLangCont.FNAME.getCont(p.us.ulist.list[i].forms[0]) ?: ""
                } else {
                    p.us.ulist.list[i]?.forms?.get(0)?.name ?: ""
                }

                val id = if(pid != 0) {
                    StaticStore.getID(p.us.ulist.list[i].id)
                } else {
                    i
                }

                names.add(getName(id, name))
            }

            val cont = context ?: return

            val adapter = UnitListAdapter(cont, names, numbers, pid)

            list.adapter = adapter

            list.setOnItemClickListener { _, _, position, _ ->
                val intent = Intent(activity, UnitInfo::class.java)

                if(position < 0 || position >= numbers.size)
                    return@setOnItemClickListener

                intent.putExtra("PID",pid)

                val id = if(pid != 0) {
                    StaticStore.getID(p.us.ulist.list[numbers[position]].id)
                } else {
                    numbers[position]
                }

                intent.putExtra("ID",id)

                activity?.startActivity(intent)
            }
        } else {
            nores.visibility = View.VISIBLE
            list.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        destroyed = true
        super.onDestroy()
    }

    private fun getName(i: Int, name: String) : String {
        return if(name == "") {
            Data.hex(pid)+" - "+number(i) + "/"
        } else {
            Data.hex(pid)+" - "+number(i) + "/" + name
        }
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
}