package com.mandarin.bcu.androidutil

import android.content.Context
import android.content.ContextWrapper
import com.mandarin.bcu.androidutil.unit.Definer
import common.system.MultiLangCont
import common.util.pack.Pack
import common.util.stage.MapColc

class Revalidater(context: Context?) : ContextWrapper(context) {

    private val unitnumber: Int = StaticStore.unitnumber
    fun validate(lang: String, context: Context) {
        Definer().redefine(context, lang)
        if (StaticStore.names != null) {
            StaticStore.names = arrayOfNulls(unitnumber)
            for (i in 0 until unitnumber) {
                StaticStore.names[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist[i].forms[0]))
            }
        }
        if (StaticStore.enames != null) {
            StaticStore.enames = arrayOfNulls(StaticStore.emnumber)
            for (i in 0 until StaticStore.emnumber) {
                StaticStore.enames[i] = withID(i, MultiLangCont.ENAME.getCont(Pack.def.es[i]))
            }
        }
        if (StaticStore.mapnames != null) {
            for (i in 0 until MapColc.MAPS.size) {
                val mc = MapColc.MAPS[StaticStore.MAPCODE[i]] ?: continue
                for (k in mc.maps.indices) {
                    StaticStore.mapnames[i][k] = MultiLangCont.SMNAME.getCont(mc.maps[k])
                }
            }
        }
    }

    private fun withID(id: Int, name: String?): String {
        val result: String
        var names = name
        if (name == null) names = ""
        result = if (names == "") {
            number(id)
        } else {
            number(id) + " - " + names
        }
        return result
    }

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }


}