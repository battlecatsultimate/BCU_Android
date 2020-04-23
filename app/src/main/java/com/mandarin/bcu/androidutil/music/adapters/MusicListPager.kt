package com.mandarin.bcu.androidutil.music.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.MusicPlayer
import com.mandarin.bcu.R
import common.util.Data
import common.util.pack.Pack

class MusicListPager : Fragment() {
    private var pid = 0

    companion object {
        fun newIntance(pid: Int) : MusicListPager {
            val mlp = MusicListPager()
            val bundle = Bundle()

            bundle.putInt("pid", pid)

            mlp.arguments = bundle

            return mlp
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getInt("pid") ?: 0
        val ac = activity ?: return view

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p = Pack.map[pid] ?: return view

        val ms = p.ms.list

        if(ms.isNotEmpty()) {
            nores.visibility = View.GONE

            val names = ArrayList<String>()
            val locate = ArrayList<Int>()

            for(i in ms.indices) {
                val name = ms[i].name

                names.add(Data.hex(pid) + " - " + name)
                locate.add(i)
            }

            val adapter = MusicListAdapter(ac, names, pid, false)

            list.adapter = adapter

            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                val intent = Intent(ac, MusicPlayer::class.java)

                intent.putExtra("PID", pid)
                intent.putExtra("Music", pos)

                ac.startActivity(intent)
            }
        } else {
            list.visibility = View.GONE
        }

        return view
    }
}