package com.mandarin.bcu.androidutil.castle

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
import common.pack.PackData
import common.pack.UserProfile
import common.util.stage.CastleImg
import common.util.stage.CastleList

class CsListPager : Fragment() {
    companion object {
        fun newInstance(pid: String) : CsListPager {
            val cs = CsListPager()

            val bundle = Bundle()

            bundle.putString("pid", pid)
            cs.arguments = bundle

            return cs
        }
    }

    private var pid = Identifier.DEF

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val c = context ?: return null

        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: Identifier.DEF

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p: PackData
        var index = -1

        if(pid.startsWith(Identifier.DEF)) {
            val d = pid.split("-")

            p = UserProfile.getPack(d[0])

            index = if(d.size == 1)
                0
            else
                d[1].toInt()
        } else {
            p = UserProfile.getPack(pid)
        }

        if(p is PackData.DefPack) {
            nores.visibility = View.GONE

            val csList = CastleList.defset().toList()[if(index == -1) 0 else index]

            val names = ArrayList<String>()
            val data = ArrayList<Identifier<CastleImg>>()

            for(i in csList.list.indices) {
                names.add(StaticStore.generateIdName(csList.list[i].id, c))
                data.add(csList.list[i].id)
            }

            val adapter = ArrayAdapter(c, R.layout.list_layout_text, names.toTypedArray())

            list.adapter = adapter

            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, posit, _ ->
                if(SystemClock.elapsedRealtime() - StaticStore.cslistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener

                StaticStore.cslistClick = SystemClock.elapsedRealtime()

                val intent = Intent(c, ImageViewer::class.java)

                intent.putExtra("Data", JsonEncoder.encode(data[posit]).toString())
                intent.putExtra("Img", ImageViewer.CASTLE)

                c.startActivity(intent)
            }
        } else if(p is PackData.UserPack && p.castles.list.isNotEmpty()) {
            nores.visibility = View.GONE

            val csList = p.castles

            val names = ArrayList<String>()
            val data = ArrayList<Identifier<CastleImg>>()

            for(i in csList.list.indices) {
                names.add(StaticStore.generateIdName(csList.list[i].id, c))
                data.add(csList.list[i].id)
            }

            val adapter = ArrayAdapter(c, R.layout.list_layout_text, names.toTypedArray())

            list.adapter = adapter

            list.onItemClickListener = AdapterView.OnItemClickListener { _, _, posit, _ ->
                if(SystemClock.elapsedRealtime() - StaticStore.cslistClick < StaticStore.INTERVAL)
                    return@OnItemClickListener

                StaticStore.cslistClick = SystemClock.elapsedRealtime()

                val intent = Intent(c, ImageViewer::class.java)

                intent.putExtra("Data", JsonEncoder.encode(data[posit]).toString())
                intent.putExtra("Img", ImageViewer.CASTLE)

                c.startActivity(intent)
            }
        }

        return view
    }
}