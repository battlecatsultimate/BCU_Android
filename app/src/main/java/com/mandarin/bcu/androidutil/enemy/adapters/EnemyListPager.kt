package com.mandarin.bcu.androidutil.enemy.adapters

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.EnemyInfo
import com.mandarin.bcu.EnemyList
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.filter.FilterEntity
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnemyListPager : Fragment() {

    private var pid = Identifier.DEF
    private var position = 0
    private var mode = EnemyList.Mode.INFO

    companion object {
        fun newInstance(pid: String, position: Int, mode: EnemyList.Mode) : EnemyListPager {
            val elp = EnemyListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)
            bundle.putInt("position", position)
            bundle.putString("mode", mode.name)

            elp.arguments = bundle

            return elp
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        val arg = arguments ?: return view

        pid = arg.getString("pid") ?: Identifier.DEF
        position = arg.getInt("position")
        mode = EnemyList.Mode.valueOf(arg.getString("mode", "INFO"))

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        UserProfile.getPack(pid) ?: return view

        val numbers = FilterEntity.setEnemyFilter(pid)

        if(numbers.isNotEmpty()) {
            nores.visibility = View.GONE
            list.visibility = View.VISIBLE

            val cont = context ?: return view

            val adapter = EnemyListAdapter(cont, numbers)

            list.adapter = adapter

            val ac = requireActivity()

            when(mode) {
                EnemyList.Mode.INFO -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.enemyinflistClick = SystemClock.elapsedRealtime()
                        val result = Intent(ac, EnemyInfo::class.java)
                        result.putExtra("Data", JsonEncoder.encode(numbers[position]).toString())
                        ac.startActivity(result)
                    }
                }
                EnemyList.Mode.SELECTION -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        val intent = Intent()
                        intent.putExtra("Data", JsonEncoder.encode(numbers[position]).toString())
                        ac.setResult(Activity.RESULT_OK, intent)
                        ac.finish()
                    }
                }
            }
        } else {
            nores.visibility = View.VISIBLE
            list.visibility = View.GONE
        }

        return view
    }

    suspend fun validate() {
        val view = view ?: return

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        UserProfile.getPack(pid) ?: return

        val numbers = FilterEntity.setEnemyFilter(pid)

        if(numbers.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                nores.visibility = View.GONE
                list.visibility = View.VISIBLE
            }

            val cont = context ?: return

            val adapter = EnemyListAdapter(cont, numbers)

            withContext(Dispatchers.Main) {
                list.adapter = adapter
            }

            val ac = activity ?: return

            when(mode) {
                EnemyList.Mode.INFO -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        if (SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL) return@OnItemClickListener
                        StaticStore.enemyinflistClick = SystemClock.elapsedRealtime()
                        val result = Intent(ac, EnemyInfo::class.java)
                        result.putExtra("Data", JsonEncoder.encode(numbers[position]).toString())
                        ac.startActivity(result)
                    }
                }
                EnemyList.Mode.SELECTION -> {
                    list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        val intent = Intent()
                        intent.putExtra("Data", JsonEncoder.encode(numbers[position]).toString())
                        ac.setResult(Activity.RESULT_OK, intent)
                        ac.finish()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                nores.visibility = View.VISIBLE
                list.visibility = View.GONE
            }
        }
    }
}