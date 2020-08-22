package com.mandarin.bcu.androidutil.unit.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.JsonParser
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.PackData
import common.util.lang.MultiLangCont
import common.util.unit.Unit
import java.util.*

class DynamicExplanation : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(`val`: Int, data: PackData.Identifier<Unit>, titles: Array<String?>?): DynamicExplanation {
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

        val data = JsonDecoder.decode(JsonParser.parseString(arg.getString("Data")), PackData.Identifier::class.java) as PackData.Identifier<Unit>

        val `val` = arg.getInt("Number", 0)

        val u = data.get() ?: return view

        var explanation = MultiLangCont.getStatic().FEXP.getCont(u.forms[`val`])
        if (explanation == null) {
            explanation = arrayOf<String?>("", "", "")
        }
        val unitname = view.findViewById<TextView>(R.id.unitexname)
        val explains = arrayOfNulls<TextView>(3)
        val lineid = intArrayOf(R.id.unitex0, R.id.unitex1, R.id.unitex2)
        for (i in lineid.indices) explains[i] = view.findViewById(lineid[i])
        explains[2]!!.setPadding(0, 0, 0, StaticStore.dptopx(24f, Objects.requireNonNull(activity)))
        var name = MultiLangCont.get(u.forms[`val`]) ?: u.forms[`val`].name
        if (name == null) name = ""
        unitname.text = name
        for (i in explains.indices) {
            if (i >= explanation.size) {
                explains[i]!!.text = ""
            } else {
                if (explanation[i] != null) explains[i]!!.text = explanation[i]
            }
        }
        return view
    }
}