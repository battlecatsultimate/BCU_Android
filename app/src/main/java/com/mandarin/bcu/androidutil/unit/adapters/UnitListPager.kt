package com.mandarin.bcu.androidutil.unit.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.UnitInfo
import com.mandarin.bcu.androidutil.filter.FilterEntity
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.unit.Unit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnitListPager : Fragment() {
    private var pid = "000000"
    private var position = 0

    companion object {
        fun newInstance(pid: String, position: Int) : UnitListPager {
            val ulp = UnitListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)
            bundle.putInt("position", position)

            ulp.arguments = bundle

            return ulp
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: "000000"
        position = arguments?.getInt("position") ?: 0

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val numbers = FilterEntity.setUnitFilter(pid)

        if(numbers.isNotEmpty()) {
            nores.visibility = View.GONE
            list.visibility = View.VISIBLE

            val names = ArrayList<Identifier<Unit>>()

            for(i in numbers) {
                val u = Identifier.get(i) ?: return view

                names.add(u.id)
            }

            val cont = context ?: return view

            val adapter = UnitListAdapter(cont, names)

            list.adapter = adapter

            list.setOnItemClickListener { _, _, position, _ ->
                val intent = Intent(activity, UnitInfo::class.java)

                val u = if(list.adapter is UnitListAdapter) {
                    (list.adapter as UnitListAdapter).getItem(position) ?: return@setOnItemClickListener
                } else {
                    return@setOnItemClickListener
                }

                intent.putExtra("Data", JsonEncoder.encode(u).toString())

                activity?.startActivity(intent)
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

        val numbers = FilterEntity.setUnitFilter(pid)

        if(numbers.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                nores.visibility = View.GONE
                list.visibility = View.VISIBLE
            }

            val names = ArrayList<Identifier<Unit>>()

            for(i in numbers) {
                val u = Identifier.get(i) ?: return

                names.add(u.id)
            }

            val cont = context ?: return

            val adapter = UnitListAdapter(cont, names)

            withContext(Dispatchers.Main) {
                list.adapter = adapter
            }

            list.setOnItemClickListener { _, _, position, _ ->
                val intent = Intent(activity, UnitInfo::class.java)

                val u = if(list.adapter is UnitListAdapter) {
                    (list.adapter as UnitListAdapter).getItem(position) ?: return@setOnItemClickListener
                } else {
                    return@setOnItemClickListener
                }

                intent.putExtra("Data", JsonEncoder.encode(u).toString())

                activity?.startActivity(intent)
            }
        } else {
            withContext(Dispatchers.Main) {
                nores.visibility = View.VISIBLE
                list.visibility = View.GONE
            }
        }
    }
}