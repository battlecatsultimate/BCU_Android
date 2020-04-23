package com.mandarin.bcu.androidutil

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.ImageViewer
import com.mandarin.bcu.R
import common.util.Data
import common.util.pack.Pack

class BGListPager : Fragment() {
    private var pid = 0

    companion object {
        fun newInstance(pid: Int) : BGListPager {
            val blp = BGListPager()
            val bundle = Bundle()

            bundle.putInt("pid", pid)

            blp.arguments = bundle

            return blp
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getInt("pid") ?: 0

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p = Pack.map[pid] ?: return view

        if(p.bg.size() != 0) {
            nores.visibility = View.GONE

            val names = ArrayList<String>()

            for(i in p.bg.list.indices) {
                names.add(Data.hex(pid) + " - " + Data.trio(p.bg.list[i].id))
            }

            val c = activity ?: return view

            val adapter = ArrayAdapter(c, R.layout.list_layout_text, names.toTypedArray())

            list.adapter = adapter

            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (SystemClock.elapsedRealtime() - StaticStore.bglistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener

                StaticStore.bglistClick = SystemClock.elapsedRealtime()

                val intent = Intent(c, ImageViewer::class.java)

                if(pid == 0) {
                    intent.putExtra("Path", StaticStore.getExternalPath(c)+"org/img/bg/bg" + number(position) + ".png")
                    intent.putExtra("Img", 0)
                    intent.putExtra("BGNum", position)
                } else {
                    intent.putExtra("PID", pid)
                    intent.putExtra("Img", 0)
                    intent.putExtra("BGNum", StaticStore.getID(p.bg.list[position].id))
                }

                c.startActivity(intent)
            }
        } else {
            list.visibility = View.GONE
        }

        return view
    }

    private fun number(n: Int): String {
        return when (n) {
            in 0..9 -> {
                "00$n"
            }
            in 10..99 -> {
                "0$n"
            }
            else -> {
                n.toString()
            }
        }
    }
}