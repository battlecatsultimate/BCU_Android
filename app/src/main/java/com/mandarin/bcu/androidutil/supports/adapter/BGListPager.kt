package com.mandarin.bcu.androidutil.supports.adapter

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
import com.mandarin.bcu.androidutil.StaticStore
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.Data
import common.util.pack.Background

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
            val data = ArrayList<Identifier<Background>>()

            for(i in p.bgs.list.indices) {
                names.add(generateName(p.bgs.list[i].id))
                data.add(p.bgs.list[i].id)
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

                intent.putExtra("Data", JsonEncoder.encode(data[position]).toString())
                intent.putExtra("Img", ImageViewer.ViewerType.BACKGROUND.name)

                c.startActivity(intent)
            }
        } else {
            list.visibility = View.GONE
        }

        return view
    }

    private fun generateName(id: Identifier<Background>) : String {
        return if(id.pack == Identifier.DEF) {
            "${context?.getString(R.string.pack_default) ?: "Default"} - ${Data.trio(id.id)}"
        } else {
            "${StaticStore.getPackName(id.pack)} - ${Data.trio(id.id)}"
        }
    }
}