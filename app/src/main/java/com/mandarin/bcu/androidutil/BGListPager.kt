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
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile

class BGListPager : Fragment() {
    private var pid = Identifier.DEF

    companion object {
        fun newInstance(pid: String) : BGListPager {
            val blp = BGListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)

            blp.arguments = bundle

            return blp
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: Identifier.DEF

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p = UserProfile.getPack(pid) ?: return view

        if(p.bgs.size() != 0) {
            nores.visibility = View.GONE

            val names = ArrayList<String>()

            for(i in p.bgs.list.indices) {
                names.add(StaticStore.trio(p.bgs.list[i].id.id))
            }

            val c = activity ?: return view

            val adapter = ArrayAdapter(c, R.layout.list_layout_text, names.toTypedArray())

            list.adapter = adapter

            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                if (SystemClock.elapsedRealtime() - StaticStore.bglistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener

                StaticStore.bglistClick = SystemClock.elapsedRealtime()

                val intent = Intent(c, ImageViewer::class.java)

                if(pid == Identifier.DEF) {
                    intent.putExtra("BGNum", position)
                }

                intent.putExtra("Data", JsonEncoder.encode(p.bgs.list[position].id).toString())
                intent.putExtra("Img", 0)

                c.startActivity(intent)
            }
        } else {
            list.visibility = View.GONE
        }

        return view
    }
}