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
import common.pack.Identifier
import common.pack.UserProfile
import common.util.stage.Music

class MusicListPager : Fragment() {
    private var pid = "000000"

    companion object {
        fun newIntance(pid: String) : MusicListPager {
            val mlp = MusicListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)

            mlp.arguments = bundle

            return mlp
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: "000000"
        val ac = activity ?: return view

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p = UserProfile.getPack(pid) ?: return view

        val ms = p.musics.list

        if(ms.isNotEmpty()) {
            nores.visibility = View.GONE

            val names = ArrayList<Identifier<Music>>()

            for(i in ms.indices) {
                names.add(ms[i].id)
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