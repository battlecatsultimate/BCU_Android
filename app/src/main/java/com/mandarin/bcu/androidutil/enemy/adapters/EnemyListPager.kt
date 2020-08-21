package com.mandarin.bcu.androidutil.enemy.adapters

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.EnemyInfo
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.filter.FilterEntity
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.enemy.asynchs.EAdder
import common.system.MultiLangCont
import common.util.Data
import common.util.pack.Pack

class EnemyListPager : Fragment() {

    private var pid = 0
    private var position = 0
    private var mode = EAdder.MODE_INFO

    companion object {
        fun newInstance(pid: Int, position: Int, mode: Int) : EnemyListPager {
            val elp = EnemyListPager()
            val bundle = Bundle()

            bundle.putInt("pid", pid)
            bundle.putInt("position", position)
            bundle.putInt("mode", mode)

            elp.arguments = bundle

            return elp
        }
    }

    private var destroyed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getInt("pid") ?: 0
        position = arguments?.getInt("position") ?: 0
        mode = arguments?.getInt("mode") ?: EAdder.MODE_INFO

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        validate(nores, list)

        val handler = Handler()

        val runnable = object : Runnable {
            override fun run() {
                if(position >= StaticStore.filterEntityList.size)
                    return

                if(StaticStore.filterEntityList[position]) {
                    validate(nores,list)
                    println(position)
                    StaticStore.filterEntityList[position] = false
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

        val filterEntity = FilterEntity(p.es.size(), StaticStore.entityname, pid)

        val numbers = filterEntity.eSetFilter()

        if(numbers.isNotEmpty()) {
            nores.visibility = View.GONE
            list.visibility = View.VISIBLE

            val names = ArrayList<String>()

            for(i in numbers) {
                if(p.es.list[i] == null)
                    continue

                val name = if(pid == 0) {
                    MultiLangCont.ENAME.getCont(p.es.list[i]) ?: ""
                } else {
                    p.es.list[i]?.name ?: ""
                }

                val id = if(pid != 0) {
                    StaticStore.getID(p.es.list[i].id)
                } else {
                    i
                }

                names.add(getName(id, name))
            }

            val cont = context ?: return

            val adapter = EnemyListAdapter(cont, names, numbers, pid)

            list.adapter = adapter

            val ac = activity ?: return

            when(mode) {
                EAdder.MODE_INFO -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.enemyinflistClick = SystemClock.elapsedRealtime()
                        val result = Intent(ac, EnemyInfo::class.java)
                        result.putExtra("ID", StaticStore.getID(p.es.list[numbers[position]].id))
                        result.putExtra("PID", pid)
                        ac.startActivity(result)
                    }
                }
                EAdder.MODE_SELECTION -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        val intent = Intent()
                        intent.putExtra("id", numbers[position])
                        intent.putExtra("pid",pid)
                        ac.setResult(Activity.RESULT_OK, intent)
                        ac.finish()
                    }
                }
                else -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.enemyinflistClick = SystemClock.elapsedRealtime()
                        val result = Intent(ac, EnemyInfo::class.java)
                        result.putExtra("ID", StaticStore.getID(p.es.list[numbers[position]].id))
                        result.putExtra("PID",pid)
                        ac.startActivity(result)
                    }
                }
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