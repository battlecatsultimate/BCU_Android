package com.mandarin.bcu.androidutil.unit.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.unit.Unit

class DynamicExplanation : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(`val`: Int, data: Identifier<Unit>, titles: Array<String?>?): DynamicExplanation {
            val explanation = DynamicExplanation()
            val bundle = Bundle()

            bundle.putInt("Number", `val`)
            bundle.putStringArray("Title", titles)
            bundle.putString("Data", JsonEncoder.encode(data).toString())

            explanation.arguments = bundle

            return explanation
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View? {
        val view = inflater.inflate(R.layout.unit_info_tab, container, false)

        val arg = arguments ?: return view

        val data = StaticStore.transformIdentifier<Unit>(arg.getString("Data")) ?: return view

        val fid = arg.getInt("Number", 0)

        val u = data.get() ?: return view

        var explanation = if(u.id.pack == Identifier.DEF) {
            MultiLangCont.getStatic().FEXP.getCont(u.forms[fid])
        } else {
            u.forms[fid].explanation.split("<br>").toTypedArray()
        }

        println(explanation.contentToString())

        if(explanation == null) {
            explanation = arrayOf("", "", "", "")
        }

        val unitname = view.findViewById<TextView>(R.id.unitexname)

        val lineid = intArrayOf(R.id.unitex0, R.id.unitex1, R.id.unitex2, R.id.unitex3)

        val explains = Array<TextView>(lineid.size) {
            view.findViewById(lineid[it])
        }

        explains[3].setPadding(0, 0, 0, StaticStore.dptopx(24f,requireActivity()))

        var name = MultiLangCont.get(u.forms[fid]) ?: u.forms[fid].name

        if (name == null)
            name = ""

        unitname.text = name

        for (i in explains.indices) {
            if (i >= explanation.size) {
                explains[i].visibility = View.GONE

                if(i > 1) {
                    explains[i - 1].setPadding(0, 0, 0, StaticStore.dptopx(24f, requireActivity()))
                }
            } else {
                if (explanation[i] != null)
                    explains[i].text = explanation[i]
            }
        }
        return view
    }
}