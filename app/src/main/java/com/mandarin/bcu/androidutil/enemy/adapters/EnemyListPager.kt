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
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.enemy.asynchs.EAdder
import com.mandarin.bcu.androidutil.filter.FilterEntity
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.unit.AbEnemy

class EnemyListPager : Fragment() {

    private var pid = Identifier.DEF
    private var position = 0
    private var mode = EAdder.MODE_INFO

    companion object {
        fun newInstance(pid: String, position: Int, mode: Int) : EnemyListPager {
            val elp = EnemyListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)
            bundle.putInt("position", position)
            bundle.putInt("mode", mode)

            elp.arguments = bundle

            return elp
        }
    }

    private var destroyed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: Identifier.DEF
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
        UserProfile.getPack(pid) ?: return

        val numbers = FilterEntity.setEnemyFilter(pid)

        if(numbers.isNotEmpty()) {
            nores.visibility = View.GONE
            list.visibility = View.VISIBLE

            val cont = context ?: return

            val adapter = EnemyListAdapter(cont, numbers, pid)

            list.adapter = adapter

            val ac = activity ?: return

            when(mode) {
                EAdder.MODE_INFO -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.enemyinflistClick = SystemClock.elapsedRealtime()
                        val result = Intent(ac, EnemyInfo::class.java)
                        result.putExtra("Data", JsonEncoder.encode(numbers[position]).toString())
                        ac.startActivity(result)
                    }
                }
                EAdder.MODE_SELECTION -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        val intent = Intent()
                        intent.putExtra("Data", JsonEncoder.encode(numbers[position]).toString())
                        ac.setResult(Activity.RESULT_OK, intent)
                        ac.finish()
                    }
                }
                else -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.enemyinflistClick = SystemClock.elapsedRealtime()
                        val result = Intent(ac, EnemyInfo::class.java)
                        result.putExtra("Data", JsonEncoder.encode(numbers[position]).toString())
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
}